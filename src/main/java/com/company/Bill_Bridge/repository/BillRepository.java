package com.company.Bill_Bridge.repository;

import com.company.Bill_Bridge.model.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface BillRepository extends JpaRepository<Bill , Long> {
    Optional<Bill> findByBillId(Long billId);

    List<Bill> findAllByUser_IdOrderByCreatedAtDesc(Long id);

    List<Bill> findAllByMerchant_MerchantIdOrderByCreatedAtDesc(UUID ID) ;

    Page<Bill> findAllByMerchant_MerchantIdOrderByCreatedAtDesc(UUID merchantId, Pageable pageable);

    void deleteByBillId(Long billId);
}
