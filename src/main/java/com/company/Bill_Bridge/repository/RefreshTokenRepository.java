package com.company.Bill_Bridge.repository;

import com.company.Bill_Bridge.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken , UUID> {

    Optional<RefreshToken> findByJti(String jti) ;
}
