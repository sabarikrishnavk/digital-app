package com.sellaway.cartservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartEvent {
    private String eventType;
    private Long cartId;
    private Long productId;
}
