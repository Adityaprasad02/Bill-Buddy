package com.company.Bill_Bridge.model;


import com.company.Bill_Bridge.model.enums.MerchantType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "app_merchants")
@Builder
public class Merchant {


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn( name = "user_id" , referencedColumnName = "id" , unique = true , nullable = false)
    @ToString.Exclude
    private User user ;

    @Id
    @GeneratedValue
    private UUID merchantId ;

    @NotBlank
    @NotNull
    @Column(name = "business" , nullable = false )
    private String businessName ;

    @NotNull
    @Column(name = "merchant_type" ,nullable = false)
    @Enumerated(EnumType.STRING)
    private MerchantType type ;

    @NotBlank
    @Column(name = "address"  , nullable = false)
    private String address ;


    @NotNull
    @Column(name = "gst_number" , unique = true , nullable = false)
    private Long gstNumber ;
}
