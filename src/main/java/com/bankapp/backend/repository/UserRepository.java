package com.bankapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bankapp.backend.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmailAndPassword(String email, String password);
}