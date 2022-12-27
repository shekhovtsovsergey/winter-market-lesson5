package ru.geekbrains.winter.market.core.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.geekbrains.winter.market.api.CartDto;
import ru.geekbrains.winter.market.api.CartItemDto;
import ru.geekbrains.winter.market.api.ResourceNotFoundException;
import ru.geekbrains.winter.market.core.entities.Order;
import ru.geekbrains.winter.market.core.entities.OrderItem;
import ru.geekbrains.winter.market.core.entities.Product;
import ru.geekbrains.winter.market.core.entities.User;
import ru.geekbrains.winter.market.core.integrations.CartServiceIntegration;
import ru.geekbrains.winter.market.core.repositories.OrderRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final CartServiceIntegration cartServiceIntegration;

    @Transactional
    public void createOrder(User user) {
        CartDto cartDto = cartServiceIntegration.getCurrentCart()
                .orElseThrow(()-> new ResourceNotFoundException("Корзина не найдена!"));


        Order order = new Order();
        List<OrderItem> orderItemList = new LinkedList<>();

        order.setUser(user);
        order.setTotalPrice(cartDto.getTotalPrice());


        for(CartItemDto cartItem : cartDto.getItems()){
            if(productService.findById(cartItem.getProductId()).isPresent()){
                Optional<Product> product = productService.findById(cartItem.getProductId());

                OrderItem orderItem = new OrderItem(
                        product.get(),
                        order,
                        cartItem.getQuantity(),
                        cartItem.getPricePerProduct(),
                        cartItem.getPrice() );

                orderItemList.add(orderItem);
            }
        }
        order.setItems(orderItemList);
        orderRepository.save(order);

        cartServiceIntegration.clearCart();
    }
}
