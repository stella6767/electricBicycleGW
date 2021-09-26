package net.linalabs.station.config;

import lombok.RequiredArgsConstructor;
import net.linalabs.station.handler.customexception.MValidException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Aspect
public class BindingAdvice {


    @Around("execution(* net.linalabs.station.web..*Controller.*(..))")
    public Object validCheck(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String type = proceedingJoinPoint.getSignature().getDeclaringTypeName();
        String method = proceedingJoinPoint.getSignature().getName();

        System.out.println("type : " + type);
        System.out.println("method : " + method);

        Object[] args = proceedingJoinPoint.getArgs();

        for (Object arg : args) {
            if (arg instanceof BindingResult) {

                BindingResult bindingResult = (BindingResult) arg;
                System.out.println(bindingResult);

                // 서비스 : 정상적인 화면 -> 사용자요청
                if (bindingResult.hasErrors()) {
                    Map<String, String> errorMap = new HashMap<>();

                    for (FieldError error : bindingResult.getFieldErrors()) {
                        errorMap.put(error.getField(), error.getDefaultMessage());
                    }
//                    return HttpStatus.BAD_REQUEST.value();
                    throw new MValidException("Valid 체크에 실패하였습니다. Request 인자를 정확히 입력해주십시오.");

                }
            }
        }
        return proceedingJoinPoint.proceed(); // 정상적이면 함수의 스택을 실행해라

    }


}
