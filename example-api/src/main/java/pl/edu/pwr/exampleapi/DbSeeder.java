package pl.edu.pwr.exampleapi;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.exampleapi.dao.entity.Order;
import pl.edu.pwr.exampleapi.dao.entity.OrderItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

@Component
public class DbSeeder {
    private static final Class<Order> ORDER_ENTITY = Order.class;

    @PersistenceContext
    private final EntityManager entityManager;
    private final DataSource dataSource;

    @Autowired
    public DbSeeder(EntityManager entityManager, DataSource dataSource) {
        this.entityManager = entityManager;
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (!canConnectToDatabase()) {
            System.out.println("❌ Cannot connect to DB. Seeding aborted.");
            return;
        }

        if (!isTableEmpty(ORDER_ENTITY)) {
            System.out.println("✅ Table already contains records. Skipping seeding.");
            return;
        }

        System.out.println("🌱 Seeding data...");
        seedOrders();
    }

    private boolean canConnectToDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private <T> boolean isTableEmpty(Class<T> entityClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entityClass);
        cq.select(cb.count(root));
        Long count = entityManager.createQuery(cq).getSingleResult();
        return count == 0;
    }

    @Transactional
    public void seedOrders() {
        Order order1 = new Order();
        order1.setOrderDate(new Date());
        order1.setCustomerName("Jan Kowalski");
        entityManager.persist(order1);

        OrderItem item1 = new OrderItem();
        item1.setProductName("Laptop");
        item1.setQuantity(1);
        item1.setPrice(4500.0);
        item1.setOrder(order1);
        entityManager.persist(item1);

        OrderItem item2 = new OrderItem();
        item2.setProductName("Myszka");
        item2.setQuantity(2);
        item2.setPrice(150.0);
        item2.setOrder(order1);
        entityManager.persist(item2);

        order1.setItems(Arrays.asList(item1, item2));

        Order order2 = new Order();
        order2.setOrderDate(new Date());
        order2.setCustomerName("Anna Nowak");
        entityManager.persist(order2);

        OrderItem item3 = new OrderItem();
        item3.setProductName("Monitor");
        item3.setQuantity(1);
        item3.setPrice(1200.0);
        item3.setOrder(order2);
        entityManager.persist(item3);

        OrderItem item4 = new OrderItem();
        item4.setProductName("Klawiatura");
        item4.setQuantity(1);
        item4.setPrice(300.0);
        item4.setOrder(order2);
        entityManager.persist(item4);

        order2.setItems(Arrays.asList(item3, item4));

        entityManager.flush(); // Force persist
    }
}
