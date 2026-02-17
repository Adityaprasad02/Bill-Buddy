package com.company.Bill_Bridge.controller;


import com.company.Bill_Bridge.exceptions.DBException;
import com.company.Bill_Bridge.exceptions.DenialException;
import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestBillCreation;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestMerchantRegister;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestUserRegister;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.ResponseBillGenerated;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.ResponseGetMerchantDetails;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.ResponseMerchantRegister;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.ResponseUserRegistration;
import com.company.Bill_Bridge.model.enums.Role;
import com.company.Bill_Bridge.service.UserService;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
public class ControllerBill {

    @Autowired
    private  UserService userService ;

    @PostMapping("/user/create")
    public ResponseEntity<ResponseUserRegistration> createUser(@Valid @RequestBody RequestUserRegister register){
        ResponseUserRegistration responseDetails = null ;
        try {
             responseDetails = userService.createUser(register);
        } catch (DBException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(responseDetails);
    }

    @PostMapping("/merchant/register")
    public ResponseEntity<ResponseMerchantRegister> registerMerchant(@AuthenticationPrincipal User merchantUser , @Validated @RequestBody RequestMerchantRegister register) throws DBException, DenialException {
        if(merchantUser.getRole().equals(Role.CUSTOMER)){
            throw  new DenialException("Merchant Register not allowed as customer : " + merchantUser.getId()) ;
        }
          var result = userService.registerMerchant(merchantUser, register) ;
        return ResponseEntity.ok(result);
    }

    @PostMapping("/merchant/bill/{customer_id}")
    public ResponseEntity<ResponseBillGenerated> generateBill( @AuthenticationPrincipal User merchantUser , @PathVariable("customer_id") Long customerId ,
                                                              @Validated @RequestBody RequestBillCreation bill) throws DBException, DenialException {
          if(merchantUser.getRole().equals(Role.CUSTOMER)){
              throw  new DenialException("Bill generation not allowed as customer : " + customerId) ;
          }

          ResponseBillGenerated result = userService.generateBill(merchantUser , customerId , bill) ;
          return ResponseEntity.ok(result) ;
    }

    @GetMapping("/user/getdetails/{username}")
    public ResponseEntity<User> getUserDetails(@PathVariable("username") String username) throws DBException {
          return
                  ResponseEntity.ok(userService.getUserDetails(username));
    }
    @GetMapping("/user/me")
    public User me(@AuthenticationPrincipal User user) {
        user.setPassword(null);
        return user;
    }

    @GetMapping("/merchant/getDetails")
    public ResponseEntity<ResponseGetMerchantDetails> getMerchantDetails(@AuthenticationPrincipal User merchantUser) throws DBException {
          return ResponseEntity.ok(userService.getMerchantDetails(merchantUser)) ;
    }





}
