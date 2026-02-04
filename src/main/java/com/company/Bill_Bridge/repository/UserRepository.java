package com.company.Bill_Bridge.repository;

import com.company.Bill_Bridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository< User , Long> {
    boolean existsByEmail(String email);

}
