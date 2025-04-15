package com.sellaway.cartservice.controller;

import com.sellaway.cartservice.model.Cart;
import com.sellaway.cartservice.model.CartItem;
import com.sellaway.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartRestController {

    private final CartService cartService;

    // Helper method to extract customerId from Authentication Principal
    private String getCustomerId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            // This should ideally not happen if security is configured correctly
            throw new IllegalStateException("User is not authenticated");
        }
        // The principal name was set to customerId in JwtAuthenticationFilter
        return authentication.getName();
         // Or if you stored UserDetails:
         // Object principal = authentication.getPrincipal();
         // if (principal instanceof UserDetails) {
         //     return ((UserDetails) principal).getUsername(); // Assuming username holds customerId
         // } else if (principal instanceof String) {
         //     return (String) principal;
         // }
         // throw new IllegalStateException("Cannot extract customerId from principal");
    }

    @GetMapping // Path variable {userId} removed
    public ResponseEntity<Cart> getCart(Authentication authentication) { // Inject Authentication
        String customerId = getCustomerId(authentication);
        return ResponseEntity.ok(cartService.getCartByUserId(customerId)); // Use customerId
    }

    @PostMapping("/items") // Path variable {userId} removed
    public ResponseEntity<Cart> addItemToCart(Authentication authentication, @RequestBody CartItem item) { // Inject Authentication
        String customerId = getCustomerId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItemToCart(customerId, item)); // Use customerId
    }

    @PutMapping("/items/{itemId}") // Path variable {userId} removed
    public ResponseEntity<Cart> updateCartItem(Authentication authentication, @PathVariable Long itemId, @RequestParam int quantity) { // Inject Authentication
        String customerId = getCustomerId(authentication);
        Cart updatedCart = cartService.updateCartItem(customerId, itemId, quantity); // Use customerId
        if (updatedCart != null) {
            return ResponseEntity.ok(updatedCart);
        } else {
            // Consider returning 403 Forbidden if the item doesn't belong to the user,
            // or 404 if the item/cart doesn't exist. Service layer should handle this.
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/items/{itemId}") // Path variable {userId} removed
    public ResponseEntity<Cart> removeItemFromCart(Authentication authentication, @PathVariable Long itemId) { // Inject Authentication
        String customerId = getCustomerId(authentication);
        Cart updatedCart = cartService.removeItemFromCart(customerId, itemId); // Use customerId
        if (updatedCart != null) {
            return ResponseEntity.ok(updatedCart);
        } else {
             // Consider returning 403 Forbidden or 404
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping // Path variable {userId} removed
    public ResponseEntity<Void> clearCart(Authentication authentication) { // Inject Authentication
        String customerId = getCustomerId(authentication);
        cartService.clearCart(customerId); // Use customerId
        return ResponseEntity.noContent().build();
    }
}
