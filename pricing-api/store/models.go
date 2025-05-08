package store

import "time"

// Product defines the structure for a product
type Product struct {
	ID        int     `json:"id"`
	Name      string  `json:"name"`
	BasePrice float64 `json:"base_price"`
}

// Promotion defines the structure for a promotion
// For simplicity, Type can be "PERCENTAGE_DISCOUNT"
// Value is the percentage (e.g., 10 for 10%)
// TargetProductID is the ID of the product this promotion applies to.
type Promotion struct {
	ID              int     `json:"id"`
	Name            string  `json:"name"`
	Type            string  `json:"type"` // e.g., "PERCENTAGE_DISCOUNT"
	Value           float64 `json:"value"` // e.g., 10 for 10%
	TargetProductID int     `json:"target_product_id"`
}

// CartItemInput is used for the request body when calculating/saving a cart
type CartItemInput struct {
	ProductID int `json:"product_id"`
	Quantity  int `json:"quantity"`
}

// CalculatedItem represents an item in a cart after calculations
type CalculatedItem struct {
	ProductID          int     `json:"product_id"`
	ProductName        string  `json:"product_name"`
	Quantity           int     `json:"quantity"`
	OriginalUnitPrice  float64 `json:"original_unit_price"`
	EffectiveUnitPrice float64 `json:"effective_unit_price"` // Price after discount
	ItemTotalPrice     float64 `json:"item_total_price"`    // Quantity * EffectiveUnitPrice
	AppliedPromotionID *int    `json:"applied_promotion_id,omitempty"`
	DiscountAmount     float64 `json:"discount_amount"` // Total discount for this item line
}

// CartCalculationRequest is the structure for the cart calculation endpoint
type CartCalculationRequest struct {
	Items []CartItemInput `json:"items"`
}

// CalculatedCartResponse is the structure returned after cart calculation
type CalculatedCartResponse struct {
	Items         []CalculatedItem `json:"items"`
	Subtotal      float64          `json:"subtotal"`       // Sum of (OriginalUnitPrice * Quantity)
	TotalDiscount float64          `json:"total_discount"` // Sum of all discount amounts
	FinalTotal    float64          `json:"final_total"`    // Subtotal - TotalDiscount
}

// SavedCart represents a cart stored in the database
type SavedCart struct {
	ID        int       `json:"id"`
	CreatedAt time.Time `json:"created_at"`
	CalculatedCartResponse
}