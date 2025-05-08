package api

import (
	"net/http"

	"github.com/gorilla/mux"
)

// NewRouter creates and configures a new Gorilla Mux router
func NewRouter(handler *APIHandler) *mux.Router {
	r := mux.NewRouter()

	// Product routes
	r.HandleFunc("/products", handler.CreateProductHandler).Methods(http.MethodPost)
	r.HandleFunc("/products", handler.GetProductsHandler).Methods(http.MethodGet)
	r.HandleFunc("/products/{id:[0-9]+}", handler.GetProductHandler).Methods(http.MethodGet)

	// Promotion routes
	r.HandleFunc("/promotions", handler.CreatePromotionHandler).Methods(http.MethodPost)
	r.HandleFunc("/promotions", handler.GetPromotionsHandler).Methods(http.MethodGet)

	// Cart routes
	r.HandleFunc("/cart/calculate", handler.CalculateCartHandler).Methods(http.MethodPost)
	r.HandleFunc("/cart/save", handler.SaveCartHandler).Methods(http.MethodPost)
	r.HandleFunc("/cart/{id:[0-9]+}", handler.GetSavedCartHandler).Methods(http.MethodGet)

	return r
}