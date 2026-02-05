package com.company.Bill_Bridge.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "payment_details")
public class PaymentDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    @OneToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_bill"  , referencedColumnName = "billId" , nullable = false )
    private Bill bill ;

    @CreationTimestamp
    @Column(name = "payment_paidAt")
    private LocalDateTime paidAt ;

    @Column( name="payment_gateway" )
    private String gateway ;

    @Column(name = "gateway_txn_id", unique = true)
    private String gatewayTxnId;

    @CreationTimestamp
    @Column(name = "payment_created")
    private LocalDateTime createdAt;
}
