package com.company.Bill_Bridge.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(DBException.class)
    public ResponseEntity<?> handleDBExceptions(DBException e){
         return new ResponseEntity<>( Map.of( "error" , e.getMessage()) , HttpStatus.BAD_REQUEST) ;
    }
}
