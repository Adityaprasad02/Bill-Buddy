package com.company.Bill_Bridge.controller;


import com.company.Bill_Bridge.exceptions.DBException;
import com.company.Bill_Bridge.exceptions.DenialException;
import com.company.Bill_Bridge.model.Bill;
import com.company.Bill_Bridge.model.PaymentDetails;
import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestBillCreation;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestMerchantRegister;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestUserRegister;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.*;
import com.company.Bill_Bridge.model.enums.Role;
import com.company.Bill_Bridge.service.UserService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
public class ControllerBill {

    @Autowired
    private  UserService userService ;

    @GetMapping("/")
    public void getHealth(HttpServletRequest request , HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter() ;
        writer.println("Bill-Bridge working fine at port 8000");
    }

    @PostMapping("/user/create")
    public ResponseEntity<ResponseUserRegistration> createUser(@Valid @RequestBody RequestUserRegister register){ResponseUserRegistration responseDetails = null ;try {responseDetails = userService.createUser(register);} catch (DBException e) {throw new RuntimeException(e);
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


    @PostMapping("/merchant/save/payment/data/{billId}")
    public ResponseEntity<PaymentDetails> savePaymentDetails(@RequestBody Map<String,Object> paymentData , @PathVariable("billId") Long billId) {
        return new ResponseEntity<>(userService.savePaymentDetails(paymentData, billId), HttpStatus.OK) ;
    }

    @PutMapping("/merchant/update/bill/status/{billId}/{status}")
    public ResponseEntity<Bill> updateBill(@PathVariable("billId") Long billId ,
                                               @PathVariable("status") String status) {

        return new ResponseEntity<>(userService.updateBillStatus(billId , status) , HttpStatus.OK);
    }

    @GetMapping("/customer/fetch/bill/{customer_id}")
    public  ResponseEntity<List<FetchAllBills>> getCustomerListBills(@PathVariable Long customer_id){
        var allBills = userService.customerGetAllBills(customer_id);
        return new ResponseEntity<>(allBills , HttpStatus.OK);
    }

    @GetMapping("/merchant/fetch/bill/{merchant_id}")
    public ResponseEntity<List<FetchAllBills>> getMerchantListBills(@PathVariable UUID merchant_id){
        var allBills = userService.merchantGetAllBills(merchant_id) ;
        return new ResponseEntity<>(allBills , HttpStatus.OK);
    }

    @GetMapping("/merchant/fetch/bill/{merchant_id}/{page}/{pageSize}")
    public ResponseEntity<PaginatedBillResponse> getMerchantListBillsbyPagination
            (@PathVariable UUID merchant_id,@PathVariable int page , @PathVariable int pageSize){
        var allBills = userService.merchantGetAllBillsByPagination(merchant_id,page,pageSize) ;
        return new ResponseEntity<>(allBills , HttpStatus.OK);
    }

    @GetMapping("/user/fetch/paymentDetails/{billId}")
    public ResponseEntity<ResponsePaymentFetch> getPaymentDetails(@PathVariable Long billId){
        var paymentDetails = userService.getPaymentDetails(billId) ;
        return ResponseEntity.ok(paymentDetails);
    }

    @DeleteMapping("/merchant/delete/bill/{bill_Id}")
    public ResponseEntity<String> deletePendingBill(@PathVariable Long bill_Id , @AuthenticationPrincipal User user){
       var response =   userService.deletePendingBill(bill_Id,user) ;
       return ResponseEntity.ok(response) ;
    }




}
