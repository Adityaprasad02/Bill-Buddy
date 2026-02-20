package com.company.Bill_Bridge.service;

import com.company.Bill_Bridge.exceptions.DenialException;
import com.company.Bill_Bridge.model.Bill;
import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.repository.BillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
@Slf4j
public class UploadFileService {

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

        Bill bill = billRepository.findByBillId(billId) ;

        if( !(bill.getMerchant().
                getUser()
                .getId().equals(user.getId())) ){
            throw  new DenialException(" Not authorized to upload Bill") ;
        }

        String path =   billId + "-" + file.getOriginalFilename() + ".pdf" ;

        String resultFileUpload = processUpload(billId , path ,file) ;
        log.info("result of file upload : {} " , resultFileUpload);

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
}
