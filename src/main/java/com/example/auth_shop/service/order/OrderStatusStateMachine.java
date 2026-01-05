package com.example.auth_shop.service.order;

import com.example.auth_shop.enums.OrderStatus;
import com.example.auth_shop.exceptions.OrderStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * State Machine for Order Status Transitions
 * Valid transitions:
 * PENDING → CONFIRMED
 * CONFIRMED → SHIPPING
 * SHIPPING → DELIVERED
 * DELIVERED → COMPLETED
 * Any status → CANCELLED (can cancel from any state except COMPLETED)
 */
@Component
public class OrderStatusStateMachine {
    
    private final Map<OrderStatus, Set<OrderStatus>> validTransitions;

    public OrderStatusStateMachine() {
        this.validTransitions = new EnumMap<>(OrderStatus.class);
        
        // PENDING can transition to CONFIRMED or CANCELLED
        validTransitions.put(OrderStatus.PENDING, EnumSet.of(
            OrderStatus.CONFIRMED, 
            OrderStatus.CANCELLED
        ));
        
        // CONFIRMED can transition to SHIPPING or CANCELLED
        validTransitions.put(OrderStatus.CONFIRMED, EnumSet.of(
            OrderStatus.SHIPPING, 
            OrderStatus.CANCELLED
        ));
        
        // SHIPPING can transition to DELIVERED
        validTransitions.put(OrderStatus.SHIPPING, EnumSet.of(
            OrderStatus.DELIVERED
        ));
        
        // DELIVERED can transition to COMPLETED
        validTransitions.put(OrderStatus.DELIVERED, EnumSet.of(
            OrderStatus.COMPLETED
        ));
        
        // COMPLETED is a final state - no transitions allowed
        validTransitions.put(OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class));
        
        // CANCELLED is a final state - no transitions allowed
        validTransitions.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    /**
     * Validate if a status transition is allowed
     * @param fromStatus Current status
     * @param toStatus Target status
     * @throws OrderStatusTransitionException if transition is not allowed
     */
    public void validateTransition(OrderStatus fromStatus, OrderStatus toStatus) {
        if (fromStatus == null || toStatus == null) {
            throw new OrderStatusTransitionException(
                "Order status cannot be null"
            );
        }
        
        if (fromStatus == toStatus) {
            throw new OrderStatusTransitionException(
                String.format("Order is already in status: %s", fromStatus)
            );
        }
        
        Set<OrderStatus> allowedTransitions = validTransitions.get(fromStatus);
        
        if (allowedTransitions == null || !allowedTransitions.contains(toStatus)) {
            throw new OrderStatusTransitionException(
                String.format("Cannot transition from %s to %s. Allowed transitions from %s: %s",
                    fromStatus, toStatus, fromStatus, allowedTransitions)
            );
        }
    }

    /**
     * Check if a status is a final state (no further transitions allowed)
     */
    public boolean isFinalState(OrderStatus status) {
        Set<OrderStatus> transitions = validTransitions.get(status);
        return transitions == null || transitions.isEmpty();
    }

    /**
     * Get allowed transitions from a given status
     */
    public Set<OrderStatus> getAllowedTransitions(OrderStatus fromStatus) {
        return validTransitions.getOrDefault(fromStatus, EnumSet.noneOf(OrderStatus.class));
    }
}

