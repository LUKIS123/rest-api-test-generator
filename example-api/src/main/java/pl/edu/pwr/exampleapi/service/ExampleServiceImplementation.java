package pl.edu.pwr.exampleapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.pwr.exampleapi.dao.OrderRepository;
import pl.edu.pwr.exampleapi.dao.entity.Order;
import pl.edu.pwr.exampleapi.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExampleServiceImplementation implements ExampleService {
    private final OrderRepository orderRepository;

    @Autowired
    public ExampleServiceImplementation(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Long save(CreateOrderDto createOrderDto) {
        return orderRepository.save(createOrderDto);
    }

    @Override
    public Optional<OrderDto> findById(Long id) {
        return orderRepository.findById(id)
                .map(ExampleServiceImplementation::mapOrderToOrderDto)
                .or(Optional::empty);
    }

    @Override
    public PageResult<OrderDto> getAll(int pageNumber, int pageSize) {
        QueryResult<Order> result = orderRepository.findAll(pageSize * (pageNumber - 1), pageSize);
        List<OrderDto> orderDtos = mapOrderDtoList(result);
        return new PageResult<>(orderDtos, result.totalItemCount().intValue(), pageSize, pageNumber);
    }

    @Override
    public PageResult<OrderDto> getByCustomer(int pageNumber, int pageSize, String customerName) {
        QueryResult<Order> result = orderRepository.findOrdersByCustomerName(customerName, pageSize * (pageNumber - 1), pageSize);
        List<OrderDto> orderDtos = mapOrderDtoList(result);
        return new PageResult<>(orderDtos, result.totalItemCount().intValue(), pageSize, pageNumber);
    }

    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    private static List<OrderDto> mapOrderDtoList(QueryResult<Order> result) {
        return result.items().stream()
                .map(ExampleServiceImplementation::mapOrderToOrderDto)
                .toList();
    }

    private static OrderDto mapOrderToOrderDto(Order order) {
        OrderDto orderDto = OrderDto.builder()
                .customerName(order.getCustomerName())
                .orderDate(order.getOrderDate())
                .id(order.getId())
                .items(new ArrayList<>())
                .build();

        List<OrderItemDto> itemsList = order.getItems().stream().map(item ->
                OrderItemDto.builder()
                        .price(item.getPrice())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .id(item.getId())
                        .build()).toList();

        orderDto.setItems(itemsList);

        return orderDto;
    }
}
