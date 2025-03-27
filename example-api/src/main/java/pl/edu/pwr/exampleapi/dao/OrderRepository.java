package pl.edu.pwr.exampleapi.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.exampleapi.dao.entity.Order;
import pl.edu.pwr.exampleapi.dao.entity.OrderItem;
import pl.edu.pwr.exampleapi.models.CreateOrderDto;
import pl.edu.pwr.exampleapi.models.QueryResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    @Autowired
    public OrderRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public Long save(CreateOrderDto dto) {
        Order order = Order.builder()
                .customerName(dto.getCustomerName())
                .orderDate(dto.getOrderDate())
                .build();

        List<OrderItem> orderItems = dto.getItems().stream()
                .map(item -> OrderItem.builder()
                        .price(item.getPrice())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .order(order)
                        .build())
                .toList();

        order.setItems(orderItems);
        entityManager.persist(order);
        return order.getId();
    }

    public Optional<Order> findById(Long id) {
        List<Order> resultList = entityManager.createQuery("""
                                SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id""",
                        Order.class)
                .setParameter("id", id)
                .getResultList();

        return resultList.stream().findFirst();
    }

    public QueryResult<Order> findAll(int skip, int take) {
        List<Long> orderIds = entityManager.createQuery("""
                                SELECT o.id FROM Order o""",
                        Long.class)
                .setFirstResult(skip)
                .setMaxResults(take)
                .getResultList();

        if (orderIds.isEmpty()) {
            return new QueryResult<>(Collections.emptyList(), 0L);
        }

        List<Order> orders = entityManager.createQuery("""
                                SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id IN :ids""",
                        Order.class)
                .setParameter("ids", orderIds)
                .getResultList();

        Long totalCount = entityManager.createQuery("""
                                SELECT COUNT(o) FROM Order o""",
                        Long.class)
                .getSingleResult();

        return new QueryResult<>(orders, totalCount);
    }

    public QueryResult<Order> findOrdersByCustomerName(String customerName, int skip, int take) {
        List<Long> orderIds = entityManager.createQuery("""
                                SELECT o.id FROM Order o WHERE o.customerName = :customerName""",
                        Long.class)
                .setParameter("customerName", customerName)
                .setFirstResult(skip)
                .setMaxResults(take)
                .getResultList();

        if (orderIds.isEmpty()) {
            return new QueryResult<>(Collections.emptyList(), 0L);
        }

        List<Order> orders = entityManager.createQuery("""
                                SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id IN :ids""",
                        Order.class)
                .setParameter("ids", orderIds)
                .getResultList();

        Long totalCount = entityManager.createQuery("""
                                SELECT COUNT(o) FROM Order o WHERE o.customerName = :customerName""",
                        Long.class)
                .setParameter("customerName", customerName)
                .getSingleResult();

        return new QueryResult<>(orders, totalCount);
    }

    @Transactional
    public void deleteById(Long id) {
        Order order = entityManager.find(Order.class, id);
        if (order != null) {
            entityManager.remove(order);
        }
    }
}
