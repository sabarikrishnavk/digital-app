cart-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── sellaway/
│   │   │           └── cartservice/
│   │   │               ├── CartServiceApplication.java
│   │   │               ├── controller/
│   │   │               │   ├── CartGraphQLController.java
│   │   │               │   └── CartRestController.java
│   │   │               ├── model/
│   │   │               │   ├── Cart.java
│   │   │               │   └── CartItem.java
│   │   │               ├── repository/
│   │   │               │   └── CartRepository.java
│   │   │               ├── service/
│   │   │               │   └── CartService.java
│   │   │               └── event/
│   │   │                   └── CartEvent.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── graphql/
│   │           └── cart.graphqls
│   └── test/
        ├── java/
        │   └── com/
        │       └── sellaway/
        │           └── cartservice/
        │               ├── CartServiceApplicationTests.java
        │               ├── controller/
        │               │   ├── CartGraphQLControllerTest.java
        │               │   └── CartRestControllerTest.java
        │               ├── service/
        │               │   └── CartServiceTest.java
        │               └── integration/
        │                   └── CartServiceIntegrationTest.java
        └── resources/
            └── application-test.yml
└── pom.xml
