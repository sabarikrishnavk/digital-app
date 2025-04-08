package com.sellaway.cartservice.integration;

import com.sellaway.cartservice.CartServiceApplication;
import com.sellaway.cartservice.model.Cart;
import com.sellaway.cartservice.model.CartItem;
import com.sellaway.cartservice.repository.CartRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CartServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        cartRepository.deleteAll();
    }

    @Test
    void testGetCartByUserId() throws Exception {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        cartRepository.save(cart);

        MvcResult result = mockMvc.perform(get("/carts/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains(userId));
    }

    @Test
    void testAddItemToCart() throws Exception {
        String userId = faker.internet().uuid();
        CartItem item = new CartItem();
        item.setProductId(faker.number().randomNumber());
        item.setQuantity(faker.number().numberBetween(1, 5));

        mockMvc.perform(post("/carts/{userId}/items", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"productId\": %d, \"quantity\": %d}", item.getProductId(), item.getQuantity())))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void testUpdateCartItem() throws Exception {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        CartItem item = new CartItem();
        item.setProductId(faker.number().randomNumber());
        item.setQuantity(1);
        cart.addItem(item);
        cartRepository.save(cart);
        List<CartItem> cartItems = cartRepository.findByUserId(userId).get().getItems();
        Long itemId = cartItems.get(0).getId();
        int quantity = faker.number().numberBetween(2, 5);

        mockMvc.perform(put("/carts/{userId}/items/{itemId}", userId, itemId)
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Cart updatedCart = cartRepository.findByUserId(userId).orElse(null);
        assertNotNull(updatedCart);
        assertEquals(quantity, updatedCart.getItems().get(0).getQuantity());
    }

    @Test
    void testRemoveItemFromCart() throws Exception {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        CartItem item = new CartItem();
        item.setProductId(faker.number().randomNumber());
        item.setQuantity(1);
        cart.addItem(item);
        cartRepository.save(cart);
        List<CartItem> cartItems = cartRepository.findByUserId(userId).get().getItems();
        Long itemId = cartItems.get(0).getId();

        mockMvc.perform(delete("/carts/{userId}/items/{itemId}", userId, itemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Cart updatedCart = cartRepository.findByUserId(userId).orElse(null);
        assertNotNull(updatedCart);
        assertEquals(0, updatedCart.getItems().size());
    }

    @Test
    void testClearCart() throws Exception {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        CartItem item = new CartItem();
        item.setProductId(faker.number().randomNumber());
        item.setQuantity(1);
        cart.addItem(item);
        cartRepository.save(cart);

        mockMvc.perform(delete("/carts/{userId}", userId))
                .andExpect(status().isNoContent());

        Cart updatedCart = cartRepository.findByUserId(userId).orElse(null);
        assertNotNull(updatedCart);
        assertEquals(0, updatedCart.getItems().size());
    }
}
