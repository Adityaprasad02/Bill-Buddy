package com.company.Bill_Bridge.service;


import com.company.Bill_Bridge.exceptions.DBException;
import com.company.Bill_Bridge.exceptions.DenialException;
import com.company.Bill_Bridge.model.Bill;
import com.company.Bill_Bridge.model.Merchant;
import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestBillCreation;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestMerchantRegister;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestUserRegister;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.*;
import com.company.Bill_Bridge.model.enums.*;

import com.company.Bill_Bridge.repository.BillRepository;
import com.company.Bill_Bridge.repository.MerchantRepository;
import com.company.Bill_Bridge.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class UserService {
    @Autowired
    private UserRepository userRepository ;

    @Autowired
    private MerchantRepository merchantRepository ;

    @Autowired
    private BillRepository billRepository ;


    @Autowired
    private BCryptPasswordEncoder encoder ;

    @Transactional
    public ResponseUserRegistration createUser(RequestUserRegister register) throws DBException {
        String username = register.getUsername();
        String email = register.getEmail();
        String password = register.getPassword();
        Role role = register.getRole();
        LoginAuthProvider authProvider = register.getAuthProvider();

        if(userRepository.existsByEmail(email)){
            throw new DBException("email already exists") ;
        }

        User newUser = User.builder()
                .username(username)
                .email(email)
                .password(encoder.encode(password))
                .role(role)
                .authProvider(authProvider).build();

        User save = userRepository.save(newUser);

        return new ResponseUserRegistration(save.getId(),
                save.getEmail(),
                save.getUsername() ,
                save.getRole() ,
                save.getAuthProvider()) ;

    }

    @Transactional
    public ResponseMerchantRegister registerMerchant(Long userId , RequestMerchantRegister register) throws DBException, DenialException {


        User user = userRepository.findById(userId)
                .orElseThrow( () -> new DBException("User with this id " + userId + "doesn't exist") );


        if(user.getRole().equals(Role.CUSTOMER)){
            throw new DenialException("Customer is not allowed to access this resource") ;
        }


        String businessName = register.getBusinessName();
        MerchantType type = register.getType() ;
        Long gst = register.getGstNumber();
        String address = register.getAddress();

        Merchant merchant = Merchant.builder()
                .user(user)
                .businessName(businessName)
                .type(type)
                .gstNumber(gst)
                .address(address)
                .build();

        var save = merchantRepository.save(merchant);

        return new ResponseMerchantRegister(
                    new ResponseUserRegistration(save.getUser().getId() ,
                            save.getUser().getEmail(),
                            save.getUser().getUsername(),
                            save.getUser().getRole(),
                            save.getUser().getAuthProvider()
                            ),
                    save.getMerchantId() ,
                    save.getBusinessName(),
                    save.getType() ,
                    save.getAddress(),
                    save.getGstNumber()
        ) ;
    }

    @Transactional
    public ResponseBillGenerated generateBill(UUID merchantId, Long userId, RequestBillCreation bill) throws DBException, DenialException {

        User customer = userRepository.findById(userId)
                .orElseThrow( () -> new DBException("User with this id " + userId + "doesn't exist") );

        if(customer.getRole().equals(Role.MERCHANT)){
            throw new DenialException(" User id : " + userId + " is Customer") ;
        }

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow( () -> new DBException("Merchant with id : " + merchantId + "doesn't exist")) ;

        if(merchant.getUser().getId().equals(userId)){
            throw  new DenialException("Cant generate bill for self as customer") ;
        }

        PaymentStatus paymentStatus = getPaymentStatus(bill);

        var billGenerated = Bill.builder()
                .user(customer)
                .merchant(merchant)
                .title(bill.title())
                .amount(bill.amount())
                .billLocation(bill.billLocation())
                .status(paymentStatus)
                .mode(bill.mode())
                .build() ;

        Bill savedBill = billRepository.save(billGenerated) ;

        return new ResponseBillGenerated(
                 new BillMerchantDetails(savedBill.getMerchant().getMerchantId() , savedBill.getMerchant().getBusinessName()),
                 new BillCustomerDetails(savedBill.getUser().getId() , savedBill.getUser().getUsername()),
                 savedBill.getBillId(),
                 savedBill.getTitle(),
                 savedBill.getAmount(),
                 savedBill.getBillLocation(),
                 savedBill.getStatus(),
                 savedBill.getMode()
        );

    }

    private PaymentStatus getPaymentStatus(RequestBillCreation bill) {
        if(bill.mode().equals(PaymentMode.CASH)){
            return PaymentStatus.PAID;
        }else{
            return PaymentStatus.PENDING;
        }
    }
}
