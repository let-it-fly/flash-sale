package io.github.letitfly.backend.repository;

import io.github.letitfly.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
