package com.company.Bill_Bridge.repository;

import com.company.Bill_Bridge.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface BillRepository extends JpaRepository<Bill , Long> {
    Bill findByBillId(Long billId);

    List<Bill> findAllByUser_IdOrderByCreatedAtDesc(Long id);

    List<Bill> findAllByMerchant_MerchantIdOrderByCreatedAtDesc(UUID ID) ;
}
