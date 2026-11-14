package io.github.letitfly.backend.repository;

import io.github.letitfly.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
