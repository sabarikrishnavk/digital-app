package com.sellaway.cartservice.service;

import com.sellaway.cartservice.event.CartEvent;
import com.sellaway.cartservice.model.Cart;
import com.sellaway.cartservice.model.CartItem;
import com.sellaway.cartservice.repository.CartRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private KafkaTemplate<String, CartEvent> kafkaTemplate;

    @InjectMocks
    private CartService cartService;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void getCartByUserId_ExistingCart() {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        Cart result = cartService.getCartByUserId(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getCartByUserId_NewCart() {
        String userId = faker.internet().uuid();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArguments()[0]);

        Cart result = cartService.getCartByUserId(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addItemToCart() {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        CartItem item = new CartItem();
        item.setProductId(faker.number().randomNumber());
        item.setQuantity(faker.number().numberBetween(1, 5));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArguments()[0]);

        Cart result = cartService.addItemToCart(userId, item);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(item.getProductId(), result.getItems().get(0).getProductId());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(kafkaTemplate, times(1)).send(anyString(), any(CartEvent.class));
    }

    @Test
    void updateCartItem_ExistingItem() {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        CartItem item = new CartItem();
        item.setId(faker.number().randomNumber());
        item.setProductId(faker.number().randomNumber());
        item.setQuantity(1);
        cart.addItem(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArguments()[0]);

        Cart result = cartService.updateCartItem(userId, item.getId(), 5);

        assertNotNull(result);
        assertEquals(5, result.getItems().get(0).getQuantity());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(kafkaTemplate, times(1)).send(anyString(), any(CartEvent.class));
    }

    @Test
    void updateCartItem_NonExistingItem() {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        Cart result = cartService.updateCartItem(userId, faker.number().randomNumber(), 5);

        assertNull(result);
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, never()).save(any(Cart.class));
        verify(kafkaTemplate, never()).send(anyString(), any(CartEvent.class));
    }

    @Test
    void removeItemFromCart_ExistingItem() {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        CartItem item = new CartItem();
        item.setId(faker.number().randomNumber());
        item.setProductId(faker.number().randomNumber());
        item.setQuantity(1);
        cart.addItem(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArguments()[0]);

        Cart result = cartService.removeItemFromCart(userId, item.getId());

        assertNotNull(result);
        assertEquals(0, result.getItems().size());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(kafkaTemplate, times(1)).send(anyString(), any(CartEvent.class));
    }

    @Test
    void removeItemFromCart_NonExistingItem() {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        Cart result = cartService.removeItemFromCart(userId, faker.number().randomNumber());

        assertNull(result);
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, never()).save(any(Cart.class));
        verify(kafkaTemplate, never()).send(anyString(), any(CartEvent.class));
    }

    @Test
    void clearCart() {
        String userId = faker.internet().uuid();
        Cart cart = new Cart();
        cart.setUserId(userId);
        CartItem item = new CartItem();
        item.setId(faker.number().randomNumber());
        item.setProductId(faker.number().randomNumber());
        item.setQuantity(1);
        cart.addItem(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArguments()[0]);

        cartService.clearCart(userId);

        assertEquals(0, cart.getItems().size());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(kafkaTemplate, times(1)).send(anyString(), any(CartEvent.class));
    }
}
