package main

import (
	"log"
	"net/http"

	"github.com/example/pricing-api/api"       // <-- Adjust to your module name
	"github.com/example/pricing-api/services" // <-- Adjust to your module name
	"github.com/example/pricing-api/store"    // <-- Adjust to your module name
)

func main() {
	// Initialize database store
	dbStore, err := store.NewDBStore()
	if err != nil {
		log.Fatalf("Failed to initialize database: %v", err)
	}
	defer dbStore.DB.Close()

	log.Println("Database initialized successfully.")

	// Initialize services
	pricingService := services.NewPricingService(dbStore)

	// Initialize API handlers
	apiHandler := api.NewAPIHandler(dbStore, pricingService)

	// Setup router
	router := api.NewRouter(apiHandler)

	port := ":8080"
	log.Printf("Starting pricing-api server on port %s", port)
	if err := http.ListenAndServe(port, router); err != nil {
		log.Fatalf("Failed to start server: %v", err)
	}
}