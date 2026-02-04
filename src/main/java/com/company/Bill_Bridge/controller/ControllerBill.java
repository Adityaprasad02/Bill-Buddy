package com.company.Bill_Bridge.controller;


import com.company.Bill_Bridge.exceptions.DBException;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestMerchantRegister;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestUserRegister;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.ResponseMerchantRegister;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.ResponseUserRegistration;
import com.company.Bill_Bridge.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerBill {

    @Autowired
    private  UserService userService ;

    @PostMapping("/create")
    public ResponseEntity<ResponseUserRegistration> createUser(@Valid @RequestBody RequestUserRegister register){
        ResponseUserRegistration responseDetails = null ;
        try {
             responseDetails = userService.createUser(register);
        } catch (DBException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(responseDetails);
    }

    @PostMapping("/merchant/register/{user_id}")
    public ResponseEntity<ResponseMerchantRegister> registerMerchant(@PathVariable("user_id") Long userId , @Validated @RequestBody RequestMerchantRegister register) throws DBException {
          var result = userService.registerMerchant(userId , register) ;
        return ResponseEntity.ok(result);
    }
}
