package com.company.Bill_Bridge.repository;

import com.company.Bill_Bridge.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BillRepository extends JpaRepository<Bill , Long> {
    Bill findByBillId(Long billId);
}
