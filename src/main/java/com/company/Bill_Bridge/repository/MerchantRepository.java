package com.company.Bill_Bridge.repository;

import com.company.Bill_Bridge.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MerchantRepository extends JpaRepository< Merchant , UUID> {
}
