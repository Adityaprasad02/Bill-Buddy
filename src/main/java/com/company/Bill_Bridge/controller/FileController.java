package com.company.Bill_Bridge.controller;


import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@RestController
public class FileController {

    @Autowired
    private FileService fileService ;

    @PostMapping("/merchant/upload/bill/{bill_id}")
    public Boolean uploadPdf(@RequestParam("file")MultipartFile file ,
                          @PathVariable("bill_id") Long billId , @AuthenticationPrincipal User user) throws IOException {

        return fileService.doUpload(file,billId,user) ;
    }

    @GetMapping("/merchant/view/bill/{bill_id}")
    public ResponseEntity<?> viewBillByMerchant(@PathVariable("bill_id") Long billId ,
                                          @AuthenticationPrincipal User merchantUser ){
        var res = fileService.viewFileByMerchant(billId, merchantUser);
        return ResponseEntity.ok(Map.of("signedUrl" , res)) ;
    }

    @GetMapping("customer/view/bill/{bill_id}")
    public ResponseEntity<?> viewBillByUser(@PathVariable("bill_id") Long billId
                                          , @AuthenticationPrincipal User user ){
        var res = fileService.viewFileByCustomer(billId, user);
        return ResponseEntity.ok(Map.of("signedUrl" , res)) ;
    }
}
