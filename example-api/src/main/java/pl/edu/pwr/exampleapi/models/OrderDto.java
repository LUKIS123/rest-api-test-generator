package pl.edu.pwr.exampleapi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto {
    private Long id;
    private Date orderDate;
    private String customerName;
    private Collection<OrderItemDto> items = new ArrayList<>();

    public OrderDto(Collection<OrderItemDto> items) {
        this.items = items;
    }

    public static OrderDto emptyOrder() {
        return new OrderDto(null);
    }
}
