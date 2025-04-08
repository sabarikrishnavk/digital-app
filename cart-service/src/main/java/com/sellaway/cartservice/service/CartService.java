package com.sellaway.cartservice.service;

import com.sellaway.cartservice.event.CartEvent;
import com.sellaway.cartservice.model.Cart;
import com.sellaway.cartservice.model.CartItem;
import com.sellaway.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final KafkaTemplate<String, CartEvent> kafkaTemplate;
    private static final String CART_TOPIC = "cart-events";

    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            return cartRepository.save(newCart);
        });
    }

    public Cart addItemToCart(String userId, CartItem item) {
        Cart cart = getCartByUserId(userId);
        cart.addItem(item);
        Cart savedCart = cartRepository.save(cart);
        kafkaTemplate.send(CART_TOPIC, new CartEvent("CartItemAdded", savedCart.getId(), item.getProductId()));
        return savedCart;
    }

    public Cart updateCartItem(String userId, Long itemId, int quantity) {
        Cart cart = getCartByUserId(userId);
        Optional<CartItem> cartItem = cart.getItems().stream().filter(item -> item.getId().equals(itemId)).findFirst();
        if (cartItem.isPresent()) {
            cartItem.get().setQuantity(quantity);
            Cart savedCart = cartRepository.save(cart);
            kafkaTemplate.send(CART_TOPIC, new CartEvent("CartItemUpdated", savedCart.getId(), cartItem.get().getProductId()));
            return savedCart;
        }
        return null;
    }

    public Cart removeItemFromCart(String userId, Long itemId) {
        Cart cart = getCartByUserId(userId);
        Optional<CartItem> cartItem = cart.getItems().stream().filter(item -> item.getId().equals(itemId)).findFirst();
        if (cartItem.isPresent()) {
            cart.removeItem(cartItem.get());
            Cart savedCart = cartRepository.save(cart);
            kafkaTemplate.send(CART_TOPIC, new CartEvent("CartItemRemoved", savedCart.getId(), cartItem.get().getProductId()));
            return savedCart;
        }
        return null;
    }

    public void clearCart(String userId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        kafkaTemplate.send(CART_TOPIC, new CartEvent("CartCleared", cart.getId(), null));
    }
}
