package com.sellaway.cartservice.controller;

import com.sellaway.cartservice.model.Cart;
import com.sellaway.cartservice.model.CartItem;
import com.sellaway.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CartGraphQLController {

    private final CartService cartService;

    @QueryMapping
    public Cart cart(@Argument String userId) {
        return cartService.getCartByUserId(userId);
    }

    @MutationMapping
    public Cart addCartItem(@Argument String userId, @Argument Long productId, @Argument int quantity) {
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return cartService.addItemToCart(userId, item);
    }

    @MutationMapping
    public Cart updateCartItem(@Argument String userId, @Argument Long itemId, @Argument int quantity) {
        return cartService.updateCartItem(userId, itemId, quantity);
    }

    @MutationMapping
    public Cart removeCartItem(@Argument String userId, @Argument Long itemId) {
        return cartService.removeItemFromCart(userId, itemId);
    }

    @MutationMapping
    public void clearCart(@Argument String userId) {
        cartService.clearCart(userId);
    }
}
