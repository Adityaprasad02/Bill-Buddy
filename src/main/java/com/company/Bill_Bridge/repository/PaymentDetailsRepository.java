package com.company.Bill_Bridge.repository;

import com.company.Bill_Bridge.model.Bill;
import com.company.Bill_Bridge.model.PaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentDetailsRepository extends JpaRepository<PaymentDetails , Long> {

    Optional<PaymentDetails> findByBill_BillId(Long id) ;
}
