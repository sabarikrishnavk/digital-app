pricing-api/
├── cmd/
│ └── api/
│ └── main.go # Application entry point
├── internal/
│ ├── api/
│ │ ├── handlers.go # HTTP request handlers
│ │ └── routes.go # API route definitions
│ ├── domain/
│ │ ├── cart.go # Cart related structs
│ │ ├── product.go # Product related structs & repository interface
│ │ └── promotion.go # Promotion interface and implementations
│ ├── platform/ # (Optional) For external integrations like DB
│ │ └── storage/
│ │ └── memory/
│ │ ├── product_repo.go # In-memory product storage
│ │ └── promotion_repo.go # In-memory promotion storage
│ └── service/
│ └── pricing/
│ └── service.go # Core pricing & promotion logic
├── go.mod
├── go.sum
└── README.md
└── .gitignore

go mod init github.com/digital-app/pricing-api

# For example: go mod init github.com/sabari/pricing-api

# Then, get the dependencies:

go get github.com/gorilla/mux
go get github.com/mattn/go-sqlite3

go mod tidy

go run main.go


curl -X POST -H "Content-Type: application/json" -d '{"name": "Apple", "base_price": 1.50}' http://localhost:8080/products
curl -X POST -H "Content-Type: application/json" -d '{"name": "Banana", "base_price": 0.75}' http://localhost:8080/products

curl -X POST -H "Content-Type: application/json" -d '{"name": "10% off Apples", "type": "PERCENTAGE_DISCOUNT", "value": 10, "target_product_id": 1}' http://localhost:8080/promotions


curl -X POST -H "Content-Type: application/json" -d '{"items": [{"product_id": 1, "quantity": 3}, {"product_id": 2, "quantity": 2}]}' http://localhost:8080/cart/calculate

curl -X POST -H "Content-Type: application/json" -d '{"items": [{"product_id": 1, "quantity": 3}, {"product_id": 2, "quantity": 2}]}' http://localhost:8080/cart/save

curl http://localhost:8080/cart/1
