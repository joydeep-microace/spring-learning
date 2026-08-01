package com.example.spring_learning.repository;

import com.example.spring_learning.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
