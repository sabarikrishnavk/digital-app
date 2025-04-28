package com.sellaway.cartservice.controller;

import com.sellaway.cartservice.model.Cart;
import com.sellaway.cartservice.model.CartItem;
import com.sellaway.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.security.core.context.SecurityContextHolder; // Alternative way
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CartGraphQLController {

    private final CartService cartService;

    // Helper method to extract customerId from Authentication Principal
    // Can reuse the one from RestController or define locally/in a utility class
    private String getCustomerId(Authentication authentication) {
         if (authentication == null || !authentication.isAuthenticated()) {
             throw new IllegalStateException("User is not authenticated");
         }
         return authentication.getName(); // Principal name is customerId
    }

    // Alternative: Get authentication from SecurityContextHolder if injection isn't preferred/working
    private String getCustomerIdFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getCustomerId(authentication);
    }


    // @QueryMapping
    // public Cart cart(Authentication authentication) { // Inject Authentication, remove @Argument userId
    //     String customerId = getCustomerId(authentication);
    //     return cartService.getCartByUserId(customerId);
    // }
    @QueryMapping
    public Cart cart(Authentication authentication) { // Inject Authentication
        if (authentication == null || !authentication.isAuthenticated()) {
             // Handle appropriately - throw exception, return null based on schema nullability
             throw new RuntimeException("User not authenticated.");
        }
        String userId = authentication.getName(); // Or extract from principal details
        return cartService.getCartByUserId(userId);
    }


    @MutationMapping
    public Cart addCartItem(/*@Argument String userId,*/ @Argument Long productId, @Argument int quantity,Authentication authentication) { // Inject Authentication, remove @Argument userId
        String customerId = getCustomerId(authentication);
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        // Important: Ensure CartItem doesn't require itemId to be set here.
        // The service layer (addItemToCart) should handle assigning an ID if needed.
        return cartService.addItemToCart(customerId, item);
    }

    // @MutationMapping
    // public Cart updateCartItem(/*@Argument String userId,*/ @Argument Long itemId, @Argument int quantity,Authentication authentication) { // Inject Authentication, remove @Argument userId
    //     String customerId = getCustomerId(authentication);
    //     // Add null check or error handling if updateCartItem returns null
    //     return cartService.updateCartItem(customerId, itemId, quantity);
    // }

    // @MutationMapping
    // public Cart removeCartItem(/*@Argument String userId,*/ @Argument Long itemId,Authentication authentication) { // Inject Authentication, remove @Argument userId
    //     String customerId = getCustomerId(authentication);
    //      // Add null check or error handling if removeItemFromCart returns null
    //     return cartService.removeItemFromCart(customerId, itemId);
    // }

    // // Mutation return type should be something meaningful or follow GraphQL conventions.
    // // Returning the cleared cart (often null or empty) or a status might be better.
    // // For simplicity, matching the original void return from service.
    // // GraphQL typically expects a return type for mutations. Let's return a Boolean status.
    // @MutationMapping
    // public boolean clearCart(Authentication authentication /*@Argument String userId*/) { // Inject Authentication, remove @Argument userId
    //     String customerId = getCustomerId(authentication);
    //     cartService.clearCart(customerId);
    //     // Check if cart is actually empty or just return true for success
    //     Cart cart = cartService.getCartByUserId(customerId); // Verify if needed
    //     return cart == null || cart.getItems() == null || cart.getItems().isEmpty();
    // }
}

