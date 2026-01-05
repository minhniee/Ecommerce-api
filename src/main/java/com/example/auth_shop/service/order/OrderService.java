package com.example.auth_shop.service.order;

import com.example.auth_shop.dto.OrderDto;
import com.example.auth_shop.dto.OrderStatusHistoryDto;
import com.example.auth_shop.enums.OrderStatus;
import com.example.auth_shop.exceptions.ResourceNotFoundException;
import com.example.auth_shop.model.Cart;
import com.example.auth_shop.model.Order;
import com.example.auth_shop.model.OrderItem;
import com.example.auth_shop.model.OrderStatusHistory;
import com.example.auth_shop.model.Product;
import com.example.auth_shop.repository.OrderRepository;
import com.example.auth_shop.repository.OrderStatusHistoryRepository;
import com.example.auth_shop.repository.ProductRepository;
import com.example.auth_shop.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final OrderStatusStateMachine stateMachine;

    @Override
    @Transactional
    public Order placeOrder(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        Order order = createOrder(cart);

        List<OrderItem> orderItems = createOrderItems(order, cart);

        order.setOrderItems(new HashSet<>(orderItems));
        order.setOrderAmount(calculateTotalAmount(orderItems));
        Order savedOrder = orderRepository.save(order);
        
        // Create initial status history entry for PENDING status
        createStatusHistory(savedOrder, null, OrderStatus.PENDING, "SYSTEM", "Order placed");
        
        cartService.clearCart(cart.getId());

        return savedOrder;
    }

    private Order createOrder(Cart cart) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }
    
    private OrderStatusHistory createStatusHistory(Order order, OrderStatus fromStatus, 
                                                   OrderStatus toStatus, String changedBy, String notes) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(changedBy != null ? changedBy : "SYSTEM");
        history.setNotes(notes);
        
        OrderStatusHistory savedHistory = statusHistoryRepository.save(history);
        order.getStatusHistory().add(savedHistory);
        return savedHistory;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart) { // when user have ordered system will calculate inventory in warehouse
        return cart.getItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            product.setInventory(product.getInventory() - cartItem.getQuantity()); // set inventory in wave house
            productRepository.save(product);
            return new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice()
            );
        }).toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {  // calculate total amount
        return orderItems
                .stream()
                .map(item -> item.getPrice()
                        .multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public OrderDto getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream().
                map(this::convertToDto)
                .toList();
    }

    @Override
    public Page<OrderDto> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus, String changedBy, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        OrderStatus currentStatus = order.getOrderStatus();
        
        // Validate transition using state machine
        stateMachine.validateTransition(currentStatus, newStatus);
        
        // Update order status
        order.setOrderStatus(newStatus);
        orderRepository.save(order);
        
        // Create status history entry
        createStatusHistory(order, currentStatus, newStatus, changedBy, notes);
        
        return convertToDto(order);
    }

    @Override
    public List<OrderStatusHistoryDto> getOrderStatusHistory(Long orderId) {
        // Verify order exists
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        
        List<OrderStatusHistory> history = statusHistoryRepository.findByOrderOrderIdOrderByChangedAtAsc(orderId);
        return history.stream()
                .map(this::convertToHistoryDto)
                .collect(Collectors.toList());
    }
    
    private OrderStatusHistoryDto convertToHistoryDto(OrderStatusHistory history) {
        return OrderStatusHistoryDto.builder()
                .id(history.getId())
                .orderId(history.getOrder().getOrderId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedAt(history.getChangedAt())
                .changedBy(history.getChangedBy())
                .notes(history.getNotes())
                .build();
    }

    @Override
    public OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }
}
