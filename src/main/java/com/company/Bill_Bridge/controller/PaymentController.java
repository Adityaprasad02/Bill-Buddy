package com.company.Bill_Bridge.controller;


import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.service.PaytmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
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


}
