package com.company.Bill_Bridge.service;

import com.company.Bill_Bridge.exceptions.DBException;
import com.company.Bill_Bridge.model.Bill;
import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.repository.BillRepository;
import com.paytm.pg.merchant.PaytmChecksum;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.util.JSONPObject;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class PaytmService {

    @Autowired
    private BillRepository billRepository ;

    @Value("${paytm.merchant-id}")
    private String merchantId ;

    @Value("${paytm.merchant-key}")
    private String merchantKey ;

    @Autowired
    private RestClient restClient ;

    @Autowired
    private ObjectMapper objectMapper ;


    public JsonNode initiate(Long billId, User customerUser) throws Exception {
        
        Bill bill = billRepository.findById(billId).orElseThrow( () -> new DBException("Bill with id : " + billId  + " not found")) ; 

        JSONObject paytmParams = new JSONObject() ;

        String orderId = "ORDERD_ID_" + String.valueOf(UUID.randomUUID()).substring(0,5);

        JSONObject body = extracted(customerUser, orderId, bill);

        String checksum = PaytmChecksum.generateSignature(body.toString(), merchantKey);

        JSONObject head = new JSONObject();
        head.put("signature", checksum);

        paytmParams.put("body", body);
        paytmParams.put("head", head);

        String postData = paytmParams.toString();

        JsonNode body1 = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/theia/api/v1/initiateTransaction")
                        .queryParam("mid", merchantId)
                        .queryParam("orderId", orderId)
                        .build()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(postData)
                .retrieve()
                .body(JsonNode.class);

        ObjectNode node = (ObjectNode) body1;

        // added extra things in response
        node.put("amount" , bill.getAmount()) ;
        node.put("orderId" , orderId) ;

        return body1 ;

    }

    private JSONObject extracted(User customerUser,  String orderId, Bill bill) {
        JSONObject body = new JSONObject();
        body.put("requestType", "Payment");
        body.put("mid", merchantId);
        body.put("websiteName", "WEBSTAGING");
        body.put("orderId", orderId);
        body.put("callbackUrl", "https://localhost:8080/payment-success-ho-chuki");

        JSONObject txnAmount = new JSONObject();
        txnAmount.put("value", bill.getAmount() );
        txnAmount.put("currency", "INR");

        JSONObject userInfo = new JSONObject();
        userInfo.put("custId", customerUser.getId());

        body.put("txnAmount", txnAmount);
        body.put("userInfo", userInfo);
        return body ;
    }


    public JsonNode verifyPayment(String orderId) {

        JSONObject paytmParams = new JSONObject();

        JSONObject body = new JSONObject();

        body.put("mid", merchantId);
        body.put("orderId", orderId);

        String checksum = null;
        try {
            checksum = PaytmChecksum.generateSignature(body.toString(), merchantKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JSONObject head = new JSONObject();

        head.put("signature", checksum);

        paytmParams.put("body", body);
        paytmParams.put("head", head);

        String post_data = paytmParams.toString();

        JsonNode body1 = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v3/order/status")
                        .queryParam("mid", merchantId)
                        .queryParam("orderId", orderId)
                        .build()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(post_data)
                .retrieve()
                .body(JsonNode.class);

        return body1 ;
    }
}
