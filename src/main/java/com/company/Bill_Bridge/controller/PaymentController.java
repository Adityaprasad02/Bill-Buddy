package com.company.Bill_Bridge.controller;


import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RefundAPI;
import com.company.Bill_Bridge.service.PaytmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
public class PaymentController {

    @Autowired
    private PaytmService paytmService ;

    @PostMapping("/initiate/payment/{billId}")
    public ResponseEntity<JsonNode> initiatePayment(@PathVariable Long billId , @AuthenticationPrincipal User customerUser)
            throws Exception {

        JsonNode response = paytmService.initiate(billId , customerUser) ;

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify/payment/{orderId}")
    public  ResponseEntity<JsonNode> verifyPayment(@PathVariable String orderId){
        JsonNode response =paytmService.verifyPayment(orderId) ;
        return ResponseEntity.ok(response);
    }

    // refund API
    @PostMapping("/paytm/refund")
    public ResponseEntity<Map<?,?>> makeRefund(@RequestBody RefundAPI refundAPI , @AuthenticationPrincipal User user)
            throws Exception {
        Map<?,?> response = paytmService.makeRefund(refundAPI , user) ;
        return ResponseEntity.ok(response) ;
    }

    // refund status API
    @GetMapping("/status/refund/{orderId}/{refId}")
        public ResponseEntity<JsonNode> getRefundStatus
            (@PathVariable String orderId , @PathVariable String refId) throws Exception {

           JsonNode response = paytmService.checkRefundStatus(orderId,refId) ;
           return ResponseEntity.ok(response) ;
        }




}
