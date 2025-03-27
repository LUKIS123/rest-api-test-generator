package pl.edu.pwr.exampleapi.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.exampleapi.models.CreateOrderDto;
import pl.edu.pwr.exampleapi.models.OrderDto;
import pl.edu.pwr.exampleapi.models.PageResult;
import pl.edu.pwr.exampleapi.service.ExampleServiceImplementation;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/example")
@CrossOrigin
public class ExampleController {
    private final ExampleServiceImplementation exampleServiceImplementation;

    @Autowired
    public ExampleController(ExampleServiceImplementation manager) {
        this.exampleServiceImplementation = manager;
    }

    @PostMapping("/add")
    public ResponseEntity<Long> create(
            @RequestBody CreateOrderDto createOrderDto
    ) {
        Long id = exampleServiceImplementation.save(createOrderDto);
        URI location;
        try {
            location = new URI("/api/example/" + id);
        } catch (URISyntaxException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.created(location).body(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(
            @PathVariable(value = "id") Long id
    ) {
        return exampleServiceImplementation.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(OrderDto.emptyOrder()));
    }

    @GetMapping("/customer")
    public ResponseEntity<PageResult<OrderDto>> getByCustomer(
            @RequestParam() String customerName,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(exampleServiceImplementation.getByCustomer(page, size, customerName));
    }

    @GetMapping("/all")
    public ResponseEntity<PageResult<OrderDto>> getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(exampleServiceImplementation.getAll(page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable(value = "id") Long id
    ) {
        exampleServiceImplementation.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
