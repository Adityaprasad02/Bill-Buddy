package com.company.Bill_Bridge.service;


import com.company.Bill_Bridge.exceptions.DBException;
import com.company.Bill_Bridge.model.Merchant;
import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestMerchantRegister;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestUserRegister;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.ResponseMerchantRegister;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.ResponseUserRegistration;
import com.company.Bill_Bridge.model.enums.LoginAuthProvider;
import com.company.Bill_Bridge.model.enums.MerchantType;
import com.company.Bill_Bridge.model.enums.Role;

import com.company.Bill_Bridge.repository.MerchantRepository;
import com.company.Bill_Bridge.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserService {
    @Autowired
    private UserRepository userRepository ;

    @Autowired
    private MerchantRepository merchantRepository ;

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
                .password(password)
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
    public ResponseMerchantRegister registerMerchant(Long userId , RequestMerchantRegister register) throws DBException {

        if(!userRepository.existsById(userId)){
            throw new DBException("User with given id " + userId + "doesn't exist") ;
        }
        Optional<User> user = userRepository.findById(userId);

        String businessName = register.getBusinessName();
        MerchantType type = register.getType() ;
        Long gst = register.getGstNumber();
        String address = register.getAddress();

        Merchant merchant = Merchant.builder()
                .user(user.get())
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
}
