package net.linalabs.station.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController //데이터를 리턴하기 위해서
@ControllerAdvice //모든 익셉션을 낚아챔.
public class GlobalExceptionHandler {


    @ExceptionHandler(value= Exception.class)
    public ResponseEntity<?> noLoginException(Exception e) {

        log.info("Exception 터짐: " + e.getMessage());

        return new ResponseEntity<>(e.getMessage(), HttpStatus.LOCKED); //에러를 보낸다.
    }


//    @ExceptionHandler(value = DuplicateException.class)
//    public CMRespDto<?> illegalArgumentException(DuplicateException e){
//        return new CMRespDto<>(-1,"같은 유저네임이 있습니다.", null);
//    }


}
