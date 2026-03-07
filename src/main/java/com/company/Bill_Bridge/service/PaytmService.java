package com.company.Bill_Bridge.service;

import com.company.Bill_Bridge.exceptions.DBException;
import com.company.Bill_Bridge.exceptions.DenialException;
import com.company.Bill_Bridge.model.Bill;
import com.company.Bill_Bridge.model.PaymentDetails;
import com.company.Bill_Bridge.model.RefundDetails;
import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RefundAPI;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.RefundResponse;
import com.company.Bill_Bridge.model.enums.Role;
import com.company.Bill_Bridge.repository.BillRepository;
import com.company.Bill_Bridge.repository.PaymentDetailsRepository;
import com.company.Bill_Bridge.repository.RefundDetailsRepository;
import com.paytm.pg.merchant.PaytmChecksum;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
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

    private final PaymentDetailsRepository paymentDetailsRepository ;

    private final RefundDetailsRepository refundDetailsRepository ;

    public PaytmService(PaymentDetailsRepository paymentDetailsRepository, RefundDetailsRepository refundDetailsRepository) {
        this.paymentDetailsRepository = paymentDetailsRepository;
        this.refundDetailsRepository = refundDetailsRepository;
    }


    public JsonNode initiate(Long billId, User customerUser) throws Exception {
        
        Bill bill = billRepository.findById(billId).orElseThrow( () -> new DBException("Bill with id : " + billId  + " not found")) ;

        if(bill.getBillLocation()==null){
            throw new DenialException("Cant Initiate Payment , The bill hasnt been uploaded " +
                    "by merchant yet ") ;
        }

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
        body.put("callbackUrl", "https://localhost:8000/payment-success-ho-chuki");

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

    public Map<?,?> makeRefund(RefundAPI refundAPI, User user) throws Exception {
        if(user.getRole().equals(Role.CUSTOMER)){
            throw new DenialException("Customer cant make refund") ;
        }

        Long paymentId = refundAPI.id() ;
        BigDecimal refundAmount  = refundAPI.refundAmount() ;

        Optional<RefundDetails> refundDetails = refundDetailsRepository.findByPaymentDetails_id(paymentId);

        if(refundDetails.isPresent()){
            return Map.of("message" , "refund Already initiated" ,
                          "refundDetails" , getRefundResponse(refundDetails.get())) ;
        }


        PaymentDetails paymentDetails = paymentDetailsRepository.findById(paymentId).orElseThrow(()->
                          new DBException("cant find payment with id : " + paymentId)) ;

        JSONObject paytmParams = new JSONObject();

        String refundId = UUID.randomUUID().toString().substring(0,10) ;

        JSONObject body = new JSONObject();
        body.put("mid", merchantId);
        body.put("txnType", "REFUND");
        body.put("orderId", paymentDetails.getOrderId());
        body.put("txnId", paymentDetails.getTxnId());
        body.put("refId", refundId);
        body.put("refundAmount", refundAmount);

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
                        .path("/refund/apply")
                        .build()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(post_data)
                .retrieve()
                .body(JsonNode.class);

           RefundResponse details = buildRefundDeatilsandSave(body1,paymentDetails) ;

        return Map.of("message" , "refund initiated" ,
                     "refundDetails" , details) ;

    }

    private RefundResponse buildRefundDeatilsandSave(JsonNode response, PaymentDetails paymentDetails) {
        log.info("{}" , response);
           ObjectNode bodyNode = (ObjectNode) response.get("body");
        String orderId = bodyNode.get("orderId") == null ? null : bodyNode.get("orderId").asString();
        String refId = bodyNode.get("refId") == null ? null : bodyNode.get("refId").asString();

        ObjectNode result = (ObjectNode)bodyNode.get("resultInfo") ;
        String resultStatus = result.get("resultStatus") == null ? null : result.get("resultStatus").asString();
        String resultCode = result.get("resultCode") == null ? null : result.get("resultCode").asString();
        String resultMsg = result.get("resultMsg") == null ? null : result.get("resultMsg").asString();

        String refundId = bodyNode.get("refundId") == null ? null : bodyNode.get("refundId").asString();
        BigDecimal refundAmount = bodyNode.get("refundAmount") == null ? null : BigDecimal.valueOf(Double.parseDouble(bodyNode.get("refundAmount").asString()));

        RefundDetails refundDetails = RefundDetails.builder()
                .refId(refId)
                .orderId(orderId)
                .paymentDetails(paymentDetails)
                .resultStatus(resultStatus)
                .resultCode(resultCode)
                .resultMsg(resultMsg)
                .refundId(refundId)
                .refundAmount(refundAmount)
                .build() ;

        var saved = refundDetailsRepository.save(refundDetails) ;


        return getRefundResponse(saved);
    }

    private static RefundResponse getRefundResponse(RefundDetails saved) {
        return new RefundResponse(saved.getOrderId(), saved.getRefId(), saved.getResultStatus(),
                saved.getResultCode(),
                saved.getResultMsg(), saved.getRefundId(), saved.getRefundAmount());
    }


    public JsonNode checkRefundStatus(String orderId, String refId) throws Exception {
        JSONObject paytmParams = new JSONObject();

        JSONObject body = new JSONObject();
        body.put("mid", merchantId);
        body.put("orderId", orderId);
        body.put("refId", refId);

        String checksum = PaytmChecksum.generateSignature(body.toString(), merchantKey);

        JSONObject head = new JSONObject();
        head.put("signature", checksum);

        paytmParams.put("body", body);
        paytmParams.put("head", head);

        String post_data = paytmParams.toString();

        JsonNode body1 = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/refund/status")
                        .build()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(post_data)
                .retrieve()
                .body(JsonNode.class);

        return body1 ;

    }
}