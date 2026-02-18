package com.company.Bill_Bridge.service;


import com.company.Bill_Bridge.exceptions.DBException;
import com.company.Bill_Bridge.exceptions.DenialException;
import com.company.Bill_Bridge.model.Bill;
import com.company.Bill_Bridge.model.Merchant;
import com.company.Bill_Bridge.model.PaymentDetails;
import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestBillCreation;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestMerchantRegister;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestUserRegister;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.*;
import com.company.Bill_Bridge.model.enums.*;

import com.company.Bill_Bridge.repository.BillRepository;
import com.company.Bill_Bridge.repository.MerchantRepository;
import com.company.Bill_Bridge.repository.PaymentDetailsRepository;
import com.company.Bill_Bridge.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository ;

    @Autowired
    private MerchantRepository merchantRepository ;

    @Autowired
    private BillRepository billRepository ;

    @Autowired
     private PaymentDetailsRepository paymentDetailsRepository ;


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
                .authProvider(authProvider)
                .build();

        User save = userRepository.save(newUser);

        return new ResponseUserRegistration(save.getId(),
                save.getEmail(),
                save.getUsername() ,
                save.getRole() ,
                save.getAuthProvider()) ;

    }

    @Transactional
    public ResponseMerchantRegister registerMerchant(User merchantUser , RequestMerchantRegister register) throws DBException, DenialException {



        String businessName = register.getBusinessName();
        MerchantType type = register.getType() ;
        String gst = register.getGstNumber().toUpperCase();
        String address = register.getAddress();

        Merchant merchant = Merchant.builder()
                .user(merchantUser)
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
    public ResponseBillGenerated generateBill(User merchantUser, Long customerId, RequestBillCreation bill) throws DBException, DenialException {

        User customer = userRepository.findById(customerId)
                .orElseThrow( () -> new DBException("User with this id " + customerId + " doesn't exist") );


        Long merchantUserid = merchantUser.getId();

        if(merchantUserid.equals(customerId)){
            throw  new DenialException("Cant generate bill for self as customer") ;
        }

        Merchant merchant = merchantRepository.findByUserId(merchantUserid)
                .orElseThrow( () -> new DBException("MerchantUser with userid : " + merchantUserid + "doesn't exist")) ;



        var billGenerated = Bill.builder()
                .user(customer)
                .merchant(merchant)
                .title(bill.title())
                .amount(bill.amount())
                .billLocation(bill.billLocation())
                .status(bill.status())
                .mode(bill.mode())
                .build() ;

        Bill savedBill = billRepository.save(billGenerated) ;

        return new ResponseBillGenerated(
                 new BillMerchantDetails(savedBill.getMerchant().getMerchantId() , savedBill.getMerchant().getBusinessName(),
                                                   savedBill.getMerchant().getUser().getUsername()),
                 new BillCustomerDetails(savedBill.getUser().getId() , savedBill.getUser().getUsername()),
                 savedBill.getBillId(),
                 savedBill.getTitle(),
                 savedBill.getAmount(),
                 savedBill.getBillLocation(),
                 savedBill.getStatus(),
                 savedBill.getMode()
        );

    }



    public PaymentDetails savePaymentDetails(Map<String, Object> paymentData, Long billId) {

        PaymentDetails paymentDetails = new PaymentDetails() ;
        Bill bill = null ;
        try {
            bill =  billRepository.findByBillId(billId) ;
        } catch (Exception e) {
            throw new DBException("bill not found with id : " + billId);
        }
        var data =  paymentData.get("body") ; // get the Object
        Map<String,Object> dataMap = (Map<String, Object>) data ; // the body field is also a json


        paymentDetails.setTxnId(dataMap.get("txnId").toString());
        paymentDetails.setBill(bill);
        paymentDetails.setBankTxnId(dataMap.get("bankTxnId").toString());
        paymentDetails.setOrderId(dataMap.get("orderId").toString());
        paymentDetails.setTxnAmount( new BigDecimal(dataMap.get("txnAmount").toString()));
        paymentDetails.setTxnType(dataMap.get("txnType").toString());
        paymentDetails.setGatewayName(dataMap.get("gatewayName").toString());
        paymentDetails.setBankName(dataMap.get("bankName").toString());
        var refundAmt = dataMap.get("refundAmt");
        paymentDetails.setRefundAmt(refundAmt==null ? null  : new BigDecimal(refundAmt.toString()));

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

        paymentDetails.setTxnDate( LocalDateTime.parse(dataMap.get("txnDate").toString(),formatter) );


        var save = paymentDetailsRepository.save(paymentDetails);

        log.info("Payment details : {} " , save) ;

        return save;
    }

    public User getUserDetails(String username) throws DBException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new DBException("user with username : " + username +
                "doesn't exist")) ;
        user.setPassword(null);

        return user ;
    }

    public ResponseGetMerchantDetails getMerchantDetails(User merchantUser) throws DBException {
        Merchant merchant =  merchantRepository.findByUserId(merchantUser.getId()).orElseThrow(() -> new DBException("user with merchant with user id : " + merchantUser.getId() +
                "doesn't exist"));
        return new ResponseGetMerchantDetails(merchant.getMerchantId(),
                merchant.getBusinessName(),
                merchant.getType(),
                merchant.getAddress(),
                merchant.getGstNumber()
        );
    }

    public Bill updateBillStatus(Long billId, String status) {
        Bill bill = null ;
        try {
           bill =  billRepository.findByBillId(billId) ;
        } catch (Exception e) {
            throw new DBException("bill not found with id : " + billId);
        }
        bill.setStatus(PaymentStatus.fromGateway(status));
        return billRepository.save(bill) ;

    }
}
