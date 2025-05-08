package api

import (
	"encoding/json"
	"log"
	"net/http"
	"strconv"

	"github.com/example/pricing-api/services" // <-- Adjust to your module name
	"github.com/example/pricing-api/store"    // <-- Adjust to your module name
	"github.com/gorilla/mux"
)

// APIHandler holds dependencies for API handlers
type APIHandler struct {
	DBStore        *store.DBStore
	PricingService *services.PricingService
}

// NewAPIHandler creates a new APIHandler
func NewAPIHandler(dbStore *store.DBStore, pricingService *services.PricingService) *APIHandler {
	return &APIHandler{DBStore: dbStore, PricingService: pricingService}
}

func (h *APIHandler) respondJSON(w http.ResponseWriter, status int, payload interface{}) {
	response, err := json.Marshal(payload)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(err.Error()))
		return
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	w.Write(response)
}

func (h *APIHandler) respondError(w http.ResponseWriter, code int, message string) {
	h.respondJSON(w, code, map[string]string{"error": message})
}

// Product Handlers
func (h *APIHandler) CreateProductHandler(w http.ResponseWriter, r *http.Request) {
	var p store.Product
	if err := json.NewDecoder(r.Body).Decode(&p); err != nil {
		h.respondError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}
	defer r.Body.Close()

	id, err := h.DBStore.CreateProduct(&p)
	if err != nil {
		h.respondError(w, http.StatusInternalServerError, "Failed to create product")
		return
	}
	p.ID = id
	h.respondJSON(w, http.StatusCreated, p)
}

func (h *APIHandler) GetProductHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	id, err := strconv.Atoi(vars["id"])
	if err != nil {
		h.respondError(w, http.StatusBadRequest, "Invalid product ID")
		return
	}

	product, err := h.DBStore.GetProduct(id)
	if err != nil {
		h.respondError(w, http.StatusInternalServerError, "Failed to retrieve product")
		return
	}
	if product == nil {
		h.respondError(w, http.StatusNotFound, "Product not found")
		return
	}
	h.respondJSON(w, http.StatusOK, product)
}

func (h *APIHandler) GetProductsHandler(w http.ResponseWriter, r *http.Request) {
	products, err := h.DBStore.GetProducts()
	if err != nil {
		h.respondError(w, http.StatusInternalServerError, "Failed to retrieve products")
		return
	}
	h.respondJSON(w, http.StatusOK, products)
}

// Promotion Handlers
func (h *APIHandler) CreatePromotionHandler(w http.ResponseWriter, r *http.Request) {
	var p store.Promotion
	if err := json.NewDecoder(r.Body).Decode(&p); err != nil {
		h.respondError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}
	defer r.Body.Close()

	// Basic validation
	if p.Type != "PERCENTAGE_DISCOUNT" { // Extend this for more types
		h.respondError(w, http.StatusBadRequest, "Invalid promotion type. Supported: PERCENTAGE_DISCOUNT")
		return
	}

	id, err := h.DBStore.CreatePromotion(&p)
	if err != nil {
		h.respondError(w, http.StatusInternalServerError, "Failed to create promotion")
		return
	}
	p.ID = id
	h.respondJSON(w, http.StatusCreated, p)
}

func (h *APIHandler) GetPromotionsHandler(w http.ResponseWriter, r *http.Request) {
	promotions, err := h.DBStore.GetPromotions()
	if err != nil {
		h.respondError(w, http.StatusInternalServerError, "Failed to retrieve promotions")
		return
	}
	h.respondJSON(w, http.StatusOK, promotions)
}

// Cart Handlers
func (h *APIHandler) CalculateCartHandler(w http.ResponseWriter, r *http.Request) {
	var req store.CartCalculationRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		h.respondError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}
	defer r.Body.Close()

	calculatedCart, err := h.PricingService.CalculateCart(req.Items)
	if err != nil {
		h.respondError(w, http.StatusInternalServerError, err.Error()) // Error might contain specific info
		return
	}
	h.respondJSON(w, http.StatusOK, calculatedCart)
}

func (h *APIHandler) SaveCartHandler(w http.ResponseWriter, r *http.Request) {
	var req store.CartCalculationRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		h.respondError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}
	defer r.Body.Close()

	calculatedCart, err := h.PricingService.CalculateCart(req.Items)
	if err != nil {
		h.respondError(w, http.StatusInternalServerError, "Failed to calculate cart: "+err.Error())
		return
	}

	cartID, createdAt, err := h.DBStore.SaveCart(calculatedCart)
	if err != nil {
		h.respondError(w, http.StatusInternalServerError, "Failed to save cart: "+err.Error())
		return
	}

	savedCartResponse := store.SavedCart{
		ID:                     cartID,
		CreatedAt:              createdAt,
		CalculatedCartResponse: *calculatedCart,
	}
	h.respondJSON(w, http.StatusCreated, savedCartResponse)
}

func (h *APIHandler) GetSavedCartHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	id, err := strconv.Atoi(vars["id"])
	if err != nil {
		h.respondError(w, http.StatusBadRequest, "Invalid cart ID")
		return
	}

	cart, err := h.DBStore.GetSavedCart(id)
	if err != nil {
		log.Printf("Error getting saved cart from DB: %v", err)
		h.respondError(w, http.StatusInternalServerError, "Failed to retrieve cart")
		return
	}
	if cart == nil {
		h.respondError(w, http.StatusNotFound, "Cart not found")
		return
	}
	h.respondJSON(w, http.StatusOK, cart)
}