package com.company.Bill_Bridge.repository;

import com.company.Bill_Bridge.model.RefundDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundDetailsRepository extends JpaRepository<RefundDetails, Long> {

    Optional<RefundDetails> findByPaymentDetails_id(Long id ) ;
}
