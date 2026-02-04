package com.company.Bill_Bridge.repository;

import com.company.Bill_Bridge.model.PaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDetailsRepository extends JpaRepository<PaymentDetails , Long> {
}
