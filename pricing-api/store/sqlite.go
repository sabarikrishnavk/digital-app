package store

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"time"

	_ "github.com/mattn/go-sqlite3" // SQLite driver
)

const dbFileName = "pricing.db"
const dataDir = "data"

// DBStore holds the database connection
type DBStore struct {
	DB *sql.DB
}

// NewDBStore creates a new DBStore and initializes the database
func NewDBStore() (*DBStore, error) {
	dbPath := filepath.Join(dataDir, dbFileName)

	// Ensure data directory exists
	if err := os.MkdirAll(dataDir, 0755); err != nil {
		return nil, fmt.Errorf("failed to create data directory: %w", err)
	}

	db, err := sql.Open("sqlite3", dbPath)
	if err != nil {
		return nil, fmt.Errorf("failed to open database: %w", err)
	}

	store := &DBStore{DB: db}
	if err := store.initDB(); err != nil {
		return nil, fmt.Errorf("failed to initialize database: %w", err)
	}

	return store, nil
}

// initDB creates tables if they don't exist
func (s *DBStore) initDB() error {
	schema := `
    CREATE TABLE IF NOT EXISTS products (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        base_price REAL NOT NULL
    );

    CREATE TABLE IF NOT EXISTS promotions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        type TEXT NOT NULL,
        value REAL NOT NULL,
        target_product_id INTEGER,
        FOREIGN KEY(target_product_id) REFERENCES products(id)
    );

    CREATE TABLE IF NOT EXISTS saved_carts (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        items_json TEXT NOT NULL,
        subtotal REAL NOT NULL,
        total_discount REAL NOT NULL,
        final_total REAL NOT NULL
    );
    `
	_, err := s.DB.Exec(schema)
	return err
}

// Product CRUD
func (s *DBStore) CreateProduct(product *Product) (int, error) {
	res, err := s.DB.Exec("INSERT INTO products (name, base_price) VALUES (?, ?)", product.Name, product.BasePrice)
	if err != nil {
		return 0, err
	}
	id, err := res.LastInsertId()
	return int(id), err
}

func (s *DBStore) GetProduct(id int) (*Product, error) {
	row := s.DB.QueryRow("SELECT id, name, base_price FROM products WHERE id = ?", id)
	p := &Product{}
	err := row.Scan(&p.ID, &p.Name, &p.BasePrice)
	if err == sql.ErrNoRows {
		return nil, nil // Not found
	}
	return p, err
}

func (s *DBStore) GetProducts() ([]Product, error) {
	rows, err := s.DB.Query("SELECT id, name, base_price FROM products")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var products []Product
	for rows.Next() {
		var p Product
		if err := rows.Scan(&p.ID, &p.Name, &p.BasePrice); err != nil {
			return nil, err
		}
		products = append(products, p)
	}
	return products, nil
}

// Promotion CRUD
func (s *DBStore) CreatePromotion(promo *Promotion) (int, error) {
	res, err := s.DB.Exec("INSERT INTO promotions (name, type, value, target_product_id) VALUES (?, ?, ?, ?)",
		promo.Name, promo.Type, promo.Value, promo.TargetProductID)
	if err != nil {
		return 0, err
	}
	id, err := res.LastInsertId()
	return int(id), err
}

func (s *DBStore) GetPromotions() ([]Promotion, error) {
	rows, err := s.DB.Query("SELECT id, name, type, value, target_product_id FROM promotions")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var promotions []Promotion
	for rows.Next() {
		var p Promotion
		if err := rows.Scan(&p.ID, &p.Name, &p.Type, &p.Value, &p.TargetProductID); err != nil {
			return nil, err
		}
		promotions = append(promotions, p)
	}
	return promotions, nil
}

func (s *DBStore) GetPromotionsForProduct(productID int) ([]Promotion, error) {
	rows, err := s.DB.Query("SELECT id, name, type, value, target_product_id FROM promotions WHERE target_product_id = ?", productID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var promotions []Promotion
	for rows.Next() {
		var p Promotion
		if err := rows.Scan(&p.ID, &p.Name, &p.Type, &p.Value, &p.TargetProductID); err != nil {
			return nil, err
		}
		promotions = append(promotions, p)
	}
	return promotions, nil
}

// SavedCart CRUD
func (s *DBStore) SaveCart(cart *CalculatedCartResponse) (int, time.Time, error) {
	itemsJSON, err := json.Marshal(cart.Items)
	if err != nil {
		return 0, time.Time{}, fmt.Errorf("failed to marshal cart items: %w", err)
	}

	createdAt := time.Now()
	res, err := s.DB.Exec(
		"INSERT INTO saved_carts (created_at, items_json, subtotal, total_discount, final_total) VALUES (?, ?, ?, ?, ?)",
		createdAt, string(itemsJSON), cart.Subtotal, cart.TotalDiscount, cart.FinalTotal,
	)
	if err != nil {
		return 0, time.Time{}, err
	}
	id, err := res.LastInsertId()
	return int(id), createdAt, err
}

func (s *DBStore) GetSavedCart(id int) (*SavedCart, error) {
	row := s.DB.QueryRow("SELECT id, created_at, items_json, subtotal, total_discount, final_total FROM saved_carts WHERE id = ?", id)

	var sc SavedCart
	var itemsJSON string
	var createdAtStr string

	err := row.Scan(&sc.ID, &createdAtStr, &itemsJSON, &sc.Subtotal, &sc.TotalDiscount, &sc.FinalTotal)
	if err == sql.ErrNoRows {
		return nil, nil // Not found
	}
	if err != nil {
		log.Printf("Error scanning saved cart: %v", err)
		return nil, err
	}

	// Parse the timestamp
	// SQLite stores it as TEXT by default with CURRENT_TIMESTAMP, format is "YYYY-MM-DD HH:MM:SS"
	// Adjust parsing if your SQLite version/config stores it differently
	sc.CreatedAt, err = time.Parse("2006-01-02 15:04:05", createdAtStr)
    if err != nil {
        // Fallback for different potential timestamp formats or if it includes timezone info
        sc.CreatedAt, err = time.Parse(time.RFC3339, createdAtStr)
        if err != nil {
            log.Printf("Error parsing cart created_at timestamp '%s': %v", createdAtStr, err)
            // Continue with a zero time or handle as critical error
        }
    }

	if err := json.Unmarshal([]byte(itemsJSON), &sc.Items); err != nil {
		return nil, fmt.Errorf("failed to unmarshal cart items: %w", err)
	}

	return &sc, nil
}