package pl.edu.pwr.exampleapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.pwr.exampleapi.dao.OrderRepository;
import pl.edu.pwr.exampleapi.dao.entity.Order;
import pl.edu.pwr.exampleapi.models.CreateOrderDto;
import pl.edu.pwr.exampleapi.models.OrderDto;
import pl.edu.pwr.exampleapi.models.PageResult;
import pl.edu.pwr.exampleapi.models.QueryResult;

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


        return 0L;
    }

    @Override
    public Optional<OrderDto> findById(Long id) {
        Order byId = orderRepository.findById(id);
        return Optional.ofNullable(byId).map(o -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCustomerName(o.getCustomerName());
            orderDto.setOrderDate(o.getOrderDate());
            orderDto.setId(o.getId());
            return orderDto;
        });
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

    private List<OrderDto> mapOrderDtoList(QueryResult<Order> result) {
        return result.items().stream().map(order -> {
            OrderDto orderDto = new OrderDto();
            orderDto.setCustomerName(order.getCustomerName());
            orderDto.setOrderDate(order.getOrderDate());
            orderDto.setId(order.getId());
            return orderDto;
        }).toList();
    }

    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }
}
