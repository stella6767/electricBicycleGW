package net.linalabs.station.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.handler.customexception.MValidException;
import net.linalabs.station.handler.customexception.TimeOutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackages = {"net.linalabs.station"})
public class GlobalExceptionHandler {


    @ExceptionHandler(value= TimeOutException.class)
    public String timeOutException(TimeOutException e) {

        log.info("timeOutError 뜸 " + e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(value= MValidException.class)
    public String mValidException(MValidException e) {
        log.info("timeOutError 뜸 " + e.getMessage());
        return e.getMessage();
    }


    @ExceptionHandler(value= Exception.class)
    public ResponseEntity<?> noLoginException(Exception e) {

        log.info("Exception 터짐: " + e.getMessage());

        return new ResponseEntity<>(e.getMessage(), HttpStatus.LOCKED); //에러를 보낸다.
    }



}
