package com.company.Bill_Bridge.controller;


import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.service.UploadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
public class UploadFileController {

    @Autowired
    private UploadFileService uploadFileService ;

    @PostMapping("/merchant/upload/bill/{bill_id}")
    public Boolean uploadPdf(@RequestParam("file")MultipartFile file ,
                          @PathVariable("bill_id") Long billId , @AuthenticationPrincipal User user) throws IOException {

        return uploadFileService.doUpload(file,billId,user) ;
    }
}
