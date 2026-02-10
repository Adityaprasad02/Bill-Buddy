package com.company.Bill_Bridge.controller;


import com.company.Bill_Bridge.model.dtos.RequestDtos.SendBillNotification;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class SocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate ;

    @MessageMapping("/bill.send")
    public void handleSendMessage(@Payload SendBillNotification notification){
        String customer = notification.customerName() ;
        log.info("message {} -> {}" , notification , customer  ) ;
        messagingTemplate.convertAndSendToUser(customer , "/queue/notify" , notification);
    }
}
