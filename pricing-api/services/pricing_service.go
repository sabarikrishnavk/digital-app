package services

import (
	"fmt"
	"log"

	"github.com/example/pricing-api/store" // <-- Adjust to your module name
)

// PricingService handles business logic related to pricing and promotions
type PricingService struct {
	DBStore *store.DBStore
}

// NewPricingService creates a new PricingService
func NewPricingService(dbStore *store.DBStore) *PricingService {
	return &PricingService{DBStore: dbStore}
}

// CalculateCart processes cart items, applies promotions, and calculates totals
func (s *PricingService) CalculateCart(itemsInput []store.CartItemInput) (*store.CalculatedCartResponse, error) {
	var calculatedItems []store.CalculatedItem
	var subtotal float64
	var totalDiscount float64

	for _, itemInput := range itemsInput {
		product, err := s.DBStore.GetProduct(itemInput.ProductID)
		if err != nil {
			return nil, fmt.Errorf("error fetching product %d: %w", itemInput.ProductID, err)
		}
		if product == nil {
			return nil, fmt.Errorf("product with ID %d not found", itemInput.ProductID)
		}

		calculatedItem := store.CalculatedItem{
			ProductID:         product.ID,
			ProductName:       product.Name,
			Quantity:          itemInput.Quantity,
			OriginalUnitPrice: product.BasePrice,
			EffectiveUnitPrice: product.BasePrice, // Default to base price
		}

		// Apply promotions
		// For simplicity, apply the first matching "PERCENTAGE_DISCOUNT" promotion
		promos, err := s.DBStore.GetPromotionsForProduct(product.ID)
		if err != nil {
			log.Printf("Error fetching promotions for product %d: %v", product.ID, err)
			// Continue without promotions for this item if an error occurs
		}

		for _, promo := range promos {
			if promo.Type == "PERCENTAGE_DISCOUNT" && promo.TargetProductID == product.ID {
				discountPercentage := promo.Value / 100.0
				discountForItem := product.BasePrice * discountPercentage
				calculatedItem.EffectiveUnitPrice = product.BasePrice - discountForItem
				calculatedItem.AppliedPromotionID = &promo.ID
				calculatedItem.DiscountAmount = discountForItem * float64(itemInput.Quantity)
				break // Apply only one promotion per item for now
			}
		}

		calculatedItem.ItemTotalPrice = calculatedItem.EffectiveUnitPrice * float64(itemInput.Quantity)
		calculatedItems = append(calculatedItems, calculatedItem)

		subtotal += product.BasePrice * float64(itemInput.Quantity)
		totalDiscount += calculatedItem.DiscountAmount
	}

	finalTotal := subtotal - totalDiscount

	return &store.CalculatedCartResponse{
		Items:         calculatedItems,
		Subtotal:      subtotal,
		TotalDiscount: totalDiscount,
		FinalTotal:    finalTotal,
	}, nil
}