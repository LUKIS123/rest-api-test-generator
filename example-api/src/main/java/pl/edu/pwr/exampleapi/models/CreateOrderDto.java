package pl.edu.pwr.exampleapi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateOrderDto {
    private Date orderDate;
    private String customerName;
    private Collection<CreateOrderItemDto> items = new ArrayList<>();
}
