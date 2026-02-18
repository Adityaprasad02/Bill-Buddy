package com.company.Bill_Bridge.model.enums;

public enum PaymentStatus {
    PAID ,
    PENDING ,
    DECLINED,
    FAILED,
    UNKONWN;

    public static PaymentStatus fromGateway(String status){
        
        if(status.equals("TXN_SUCCESS")){
            return PaymentStatus.PAID ;
        } else if (status.equals("TXN_FAILURE")) {
            return PaymentStatus.FAILED ;
        } else if (status.equals("PENDING")) {
            return PaymentStatus.PENDING ;
        } else  {
            return PaymentStatus.UNKONWN ;
        }
    }

}
