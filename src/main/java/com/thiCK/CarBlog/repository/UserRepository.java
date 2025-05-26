package com.thiCK.CarBlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiCK.CarBlog.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
