package com.company.Bill_Bridge.model;


import com.company.Bill_Bridge.model.enums.PaymentMode;
import com.company.Bill_Bridge.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "app_bill")
@Builder
public class Bill {


    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    private Long billId ;

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id" , referencedColumnName = "id", nullable = false )
    private  User user ;

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id" , referencedColumnName = "merchantId", nullable = false)
    private Merchant merchant ;

    @Column(name = "bill_amount" , nullable = false , precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "bill_title" , nullable = false)
    private String title ;

    @Column(name = "bill_path" , nullable = true)
    private String billLocation ;

    @Enumerated(EnumType.STRING)
    @Column(name = "bill_status" , nullable = true)
    private PaymentStatus status  ;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode" , nullable = false)
    private PaymentMode mode ;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt ;



}
