package pl.edu.pwr.exampleapi.service;

import pl.edu.pwr.exampleapi.models.CreateOrderDto;
import pl.edu.pwr.exampleapi.models.PageResult;
import pl.edu.pwr.exampleapi.models.OrderDto;

import java.util.Optional;

public interface ExampleService {
    Optional<OrderDto> findById(Long id);

    PageResult<OrderDto> getAll(int pageNumber, int pageSize);

    PageResult<OrderDto> getByCustomer(int pageNumber, int pageSize, String customerName);

    Long save(CreateOrderDto createOrderDto);

    void deleteById(Long id);
}
