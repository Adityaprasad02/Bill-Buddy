package com.company.Bill_Bridge.controller;


import com.company.Bill_Bridge.model.dtos.RequestDtos.SendBillNotification;
import com.company.Bill_Bridge.model.dtos.RequestDtos.SendPaymentInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class SocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping("/bill.send")
    public void handleSendBillNotificationMessage(@Payload SendBillNotification notification){
        String receiver = notification.customerName() ;
        try {
            messagingTemplate.convertAndSendToUser(receiver , "/queue/notify" , notification);
            log.info("Bill notification sent successfully to {}", receiver);
        } catch (Exception e) {
            log.error("Error sending bill notification to {}: {}", receiver, e.getMessage(), e);
        }
    }

    @MessageMapping("/bill.response")
    public void handleCustomerBillResponse(@Payload SendPaymentInfo paymentInfo , Message<?> message) {

        log.info("paymentData : {} " , paymentInfo.paymentData() ) ;
        log.info("paymentResponse : {} " , paymentInfo.paymentResponse() ) ;

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message) ;
        String merchantUserName = accessor.getFirstNativeHeader("merchantUserName") ;

        try {
            messagingTemplate.convertAndSendToUser(
                    merchantUserName ,
                    "/queue/notify",
                    paymentInfo
            );
        } catch (MessagingException e) {
            log.info("error :> {} " , e.getMessage());
        }


    }
}
