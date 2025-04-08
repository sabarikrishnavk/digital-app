package com.sellaway.cartservice.controller;

import com.sellaway.cartservice.model.Cart;
import com.sellaway.cartservice.model.CartItem;
import com.sellaway.cartservice.service.CartService;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
@AutoConfigureMockMvc
class CartGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private CartService cartService;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void cart() {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        when(cartService.getCartByUserId(userId)).thenReturn(cart);

        String query = """
                query getCart($userId: String!) {
                    cart(userId: $userId) {
                        userId
                    }
                }
                """;

        graphQlTester.document(query)
                .variable("userId", userId)
                .execute()
                .path("cart.userId")
                .entity(String.class)
                .isEqualTo(userId);
    }

    @Test
    void addCartItem() {
        String userId = faker.internet().uuid();
        Long productId = faker.number().randomNumber();
        int quantity = faker.number().numberBetween(1, 5);
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        cart.addItem(item);

        when(cartService.addItemToCart(any(String.class), any(CartItem.class))).thenReturn(cart);

        String mutation = """
                mutation addCartItem($userId: String!, $productId: ID!, $quantity: Int!) {
                    addCartItem(userId: $userId, productId: $productId, quantity: $quantity) {
                        userId
                        items {
                            productId
                            quantity
                        }
                    }
                }
                """;

        graphQlTester.document(mutation)
                .variable("userId", userId)
                .variable("productId", productId)
                .variable("quantity", quantity)
                .execute()
                .path("addCartItem.userId")
                .entity(String.class)
                .isEqualTo(userId);
    }

    @Test
    void updateCartItem() {
        String userId = faker.internet().uuid();
        Long itemId = faker.number().randomNumber();
        int quantity = faker.number().numberBetween(1, 5);
        Cart cart = new Cart();
        cart.setUserId(userId);
        CartItem item = new CartItem();
        item.setId(itemId);
        item.setProductId(faker.number().randomNumber());
        item.setQuantity(quantity);
        cart.addItem(item);

        when(cartService.updateCartItem(userId, itemId, quantity)).thenReturn(cart);

        String mutation = """
                mutation updateCartItem($userId: String!, $itemId: ID!, $quantity: Int!) {
                    updateCartItem(userId: $userId, itemId: $itemId, quantity: $quantity) {
                        userId
                        items {
                            id
                            quantity
                        }
                    }
                }
                """;

        graphQlTester.document(mutation)
                .variable("userId", userId)
                .variable("itemId", itemId)
                .variable("quantity", quantity)
                .execute()
                .path("updateCartItem.userId")
                .entity(String.class)
                .isEqualTo(userId);
    }

    @Test
    void removeCartItem() {
        String userId = faker.internet().uuid();
        Long itemId = faker.number().randomNumber();
        Cart cart = new Cart();
        cart.setUserId(userId);
        CartItem item = new CartItem();
        item.setId(itemId);
        item.setProductId(faker.number().randomNumber());
        item.setQuantity(faker.number().numberBetween(1, 5));
        cart.addItem(item);

        when(cartService.removeItemFromCart(userId, itemId)).thenReturn(cart);

        String mutation = """
                mutation removeCartItem($userId: String!, $itemId: ID!) {
                    removeCartItem(userId: $userId, itemId: $itemId) {
                        userId
                    }
                }
                """;

        graphQlTester.document(mutation)
                .variable("userId", userId)
                .variable("itemId", itemId)
                .execute()
                .path("removeCartItem.userId")
                .entity(String.class)
                .isEqualTo(userId);
    }

    @Test
    void clearCart() {
        String userId = faker.internet().uuid();

        String mutation = """
                mutation clearCart($userId: String!) {
                    clearCart(userId: $userId)
                }
                """;

        graphQlTester.document(mutation)
                .variable("userId", userId)
                .execute()
                .path("clearCart")
                .entity(String.class)
                .isEqualTo(null);
    }
}
