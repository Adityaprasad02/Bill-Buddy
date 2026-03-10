package com.company.Bill_Bridge.service;

import com.company.Bill_Bridge.exceptions.DBException;
import com.company.Bill_Bridge.exceptions.DenialException;
import com.company.Bill_Bridge.model.Bill;
import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.model.enums.PaymentMode;
import com.company.Bill_Bridge.model.enums.PaymentStatus;
import com.company.Bill_Bridge.repository.BillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;


@Service
@Slf4j
public class FileService {

    @Autowired
    private BillRepository billRepository ;

    @Value("${supabase.url}")
    private String supabaseProjectURL ;

    @Value("${supabase.service-role-key}") // use it very wisely
    private String serviceKey ;

    @Value("${supabase.bucket}")
    private String supabaseBucket ;

    @Autowired
    private RestClient restClient ;


    public Boolean doUpload(MultipartFile file, Long billId, User user) throws IOException {

        Bill bill =  billRepository.findByBillId(billId)
                .orElseThrow(() -> new DBException("Bill not found with id : " + billId));


        if((bill.getMode()== PaymentMode.ONLINE && bill.getStatus()==PaymentStatus.PAID)
           || (bill.getMode()==PaymentMode.CASH && bill.getBillLocation()!=null)){
            throw new DenialException("Payment already completed by Customer , cant Upload AnyMore") ;
        }


        String path =   billId  + ".pdf" ;

        String resultFileUpload = processUpload(billId , path ,file) ;
        //log.info("result of file upload : {} " , resultFileUpload);

        bill.setBillLocation(path);
        billRepository.save(bill) ;

        return true ;
    }

    private String processUpload(Long billId, String path , MultipartFile file) throws IOException {

        // configure url for storage in bucket
        String url = supabaseProjectURL + "/storage/v1/object/" + supabaseBucket + "/" + path ;

        if(file.getContentType() == null){
            throw new RuntimeException("File's Content type is not as required")  ;
        }


        // create and send a post request
        return restClient.post()
                .uri(url)
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header("Authorization" , "Bearer " + serviceKey)
                .header("apikey" , serviceKey)
                .header("x-upsert" , "true")
                .body(file.getBytes())
                .retrieve()
                .body(String.class) ;

    }

    public String viewFileByCustomer(Long billId, User user) {

        Bill bill =  billRepository.findByBillId(billId)
                .orElseThrow(() -> new DBException("Bill not found with id : " + billId));

        var currentUserId  = user.getId() ;
        String billLocation = bill.getBillLocation();

        if(!bill.getUser().getId().equals(currentUserId)){
            throw new DenialException("Not authorized to view/download file")  ;
        }

        if(bill.getStatus()!=PaymentStatus.PAID){
            throw new DenialException("Payment not completed by Customer") ;
        }

        
        if(billLocation==null){
            throw new DenialException("Merchant have not uploaded bill yet") ;
        }

        return returnSignedUrl(billLocation);

    }

    private String returnSignedUrl(String billLocation) {
        String url = supabaseProjectURL + "/storage/v1/object/sign/" + supabaseBucket + "/" + billLocation ;

        JsonNode body = null ;
        try {
            body = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("apikey", serviceKey)
                    .body(Map.of("expiresIn", 120))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        var signedUrl = body.get("signedURL").toString();

        signedUrl = signedUrl.substring(1,signedUrl.length()-1) ;
        // log.info("SignedUrl : {} " , signedUrl );

        return supabaseProjectURL + "/storage/v1" + signedUrl ;

    }

    public String viewFileByMerchant(Long billId, User merchantUser) {

        Bill bill =  billRepository.findByBillId(billId)
                .orElseThrow(() -> new DBException("Bill not found with id : " + billId));

        var currentMerchantUserId  = merchantUser.getId() ;
        String billLocation = bill.getBillLocation();

        if(bill.getMerchant().getUser().getId()!=currentMerchantUserId){
            throw new DenialException("Not authorized to view/download file")  ;
        }

        if(billLocation==null){
            throw new DenialException("Merchant have not uploaded bill yet") ;
        }

        return returnSignedUrl(billLocation);
    }
}
