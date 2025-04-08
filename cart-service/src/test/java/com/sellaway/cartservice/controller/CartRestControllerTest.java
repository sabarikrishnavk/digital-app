package com.sellaway.cartservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellaway.cartservice.model.Cart;
import com.sellaway.cartservice.model.CartItem;
import com.sellaway.cartservice.service.CartService;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartRestController.class)
class CartRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void getCart() throws Exception {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        when(cartService.getCartByUserId(userId)).thenReturn(cart);

        mockMvc.perform(get("/carts/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void addItemToCart() throws Exception {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        CartItem item = new CartItem();
        item.setProductId(faker.number().randomNumber());
        item.setQuantity(faker.number().numberBetween(1, 5));

        when(cartService.addItemToCart(any(String.class), any(CartItem.class))).thenReturn(cart);

        mockMvc.perform(post("/carts/{userId}/items", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void updateCartItem() throws Exception {
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

        mockMvc.perform(put("/carts/{userId}/items/{itemId}", userId, itemId)
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void updateCartItem_NotFound() throws Exception {
        String userId = faker.internet().uuid();
        Long itemId = faker.number().randomNumber();
        int quantity = faker.number().numberBetween(1, 5);

        when(cartService.updateCartItem(userId, itemId, quantity)).thenReturn(null);

        mockMvc.perform(put("/carts/{userId}/items/{itemId}", userId, itemId)
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeItemFromCart() throws Exception {
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

        mockMvc.perform(delete("/carts/{userId}/items/{itemId}", userId, itemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void removeItemFromCart_NotFound() throws Exception {
        String userId = faker.internet().uuid();
        Long itemId = faker.number().randomNumber();

        when(cartService.removeItemFromCart(userId, itemId)).thenReturn(null);

        mockMvc.perform(delete("/carts/{userId}/items/{itemId}", userId, itemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void clearCart() throws Exception {
        String userId = faker.internet().uuid();

        mockMvc.perform(delete("/carts/{userId}", userId))
                .andExpect(status().isNoContent());
    }
} 
