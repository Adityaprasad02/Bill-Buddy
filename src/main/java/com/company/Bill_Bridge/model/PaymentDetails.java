package com.company.Bill_Bridge.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "payment_details")
public class PaymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "txnId")
    private String TxnId ;

    @OneToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_bill"  , referencedColumnName = "billId" , nullable = false )
    private Bill bill ;

    private String bankTxnId ;

    private String orderId ;

    private BigDecimal txnAmount ;

    private String txnType ;

    private String gatewayName ;

    private String bankName ;

    private BigDecimal refundAmt ;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.S")
    private LocalDateTime txnDate;

}
