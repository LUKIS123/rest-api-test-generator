package pl.edu.pwr.exampleapi.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.exampleapi.dao.entity.Order;
import pl.edu.pwr.exampleapi.dao.entity.OrderItem;
import pl.edu.pwr.exampleapi.models.CreateOrderDto;
import pl.edu.pwr.exampleapi.models.QueryResult;

import java.util.List;

@Repository
public class OrderRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    @Autowired
    public OrderRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void save(CreateOrderDto dto) {
        entityManager.getTransaction().begin();

        List<OrderItem> orderItems = dto.getItems().stream()
                .map(item -> OrderItem.builder()
                        .price(item.getPrice())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        Order order = Order.builder()
                .customerName(dto.getCustomerName())
                .orderDate(dto.getOrderDate())
                .items(orderItems)
                .build();

        entityManager.persist(order);

        entityManager.getTransaction().commit();
    }

    public Order findById(Long id) {
        return entityManager.createQuery("""
                                SELECT o
                                FROM Order o
                                LEFT JOIN FETCH o.items
                                WHERE o.id = :id""",
                        Order.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public QueryResult<Order> findAll(int skip, int take) {
        List<Order> resultList = entityManager.createQuery("""
                                SELECT o
                                FROM Order o
                                LEFT JOIN FETCH o.items""",
                        Order.class)
                .setFirstResult(skip)
                .setMaxResults(take)
                .getResultList();

        Long totalCount = entityManager.createQuery("""
                                SELECT COUNT(o) 
                                FROM Order o""",
                        Long.class)
                .getSingleResult();

        return new QueryResult<>(resultList, totalCount);
    }

    public QueryResult<Order> findOrdersByCustomerName(String customerName, int skip, int take) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        String customerNameField = "customerName";

        CriteriaQuery<Order> query = cb.createQuery(Order.class);
        Root<Order> root = query.from(Order.class);
        Predicate condition = cb.equal(root.get(customerNameField), customerName);
        query.where(condition);

        List<Order> orders = entityManager.createQuery(query)
                .setFirstResult(skip)
                .setMaxResults(take)
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Order> countRoot = countQuery.from(Order.class);
        countQuery.select(cb.count(countRoot))
                .where(cb.equal(countRoot.get(customerNameField), customerName));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new QueryResult<>(orders, totalCount);
    }

    public void deleteById(Long id) {
        entityManager.getTransaction().begin();

        Order order = entityManager.find(Order.class, id);
        if (order != null) {
            entityManager.remove(order);
        }

        entityManager.getTransaction().commit();
    }
}
