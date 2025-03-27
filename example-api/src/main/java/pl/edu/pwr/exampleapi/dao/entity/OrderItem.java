package pl.edu.pwr.exampleapi.dao.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_items", schema = "order_schema")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;

    private Integer quantity;

    private Double price;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
