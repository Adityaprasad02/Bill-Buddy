package com.company.Bill_Bridge.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refund_details" , indexes = {
        @Index(name = "check_refund_details" , columnList = "payment_id" , unique = true)
})
@Builder
public class RefundDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id  ;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id",referencedColumnName = "id", nullable = false,updatable = false)
    private PaymentDetails paymentDetails ;

    private String orderId ;

    private String refId ;

    private String resultStatus ;

    private String resultCode ;

    private String resultMsg ;

    private String refundId ;

    private BigDecimal refundAmount ;

}

