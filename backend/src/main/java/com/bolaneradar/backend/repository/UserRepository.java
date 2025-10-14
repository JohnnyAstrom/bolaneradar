package com.bolaneradar.backend.repository;

import com.bolaneradar.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}