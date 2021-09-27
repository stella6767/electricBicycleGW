package net.linalabs.station.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.dto.Opcode;
import net.linalabs.station.dto.req.CMReqDto;
import net.linalabs.station.dto.req.ReqData;
import net.linalabs.station.dto.resp.RespData;
import net.linalabs.station.handler.customexception.TimeOutException;
import net.linalabs.station.service.SocketReadService;
import net.linalabs.station.service.SocketService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ElectricBicycleController {

    //private final SocketService socketService;

    private final SocketReadService socketReadService;
    private final GlobalVar globalVar;

    private final int maxTimeout = 3000;
    private final int sleepTime = 10;

    @PostMapping("/rental")
    public RespData rentalRequest(@Valid ReqData rentalReq, BindingResult bindingResult
    ) throws IOException, ExecutionException, InterruptedException {

        log.info("전기자전거 대여요청 옴: " + rentalReq);

        CMReqDto cmReqDto = new CMReqDto(Opcode.RENTAL, rentalReq);//Opcode.RENTAL.getCode()
        socketReadService.sendToCharger(cmReqDto);

        RespData respData = null;

        int i = 1;

        //timeOut을 설정
        while (respData == null){
            respData = globalVar.globalDispatchData.get(rentalReq.getChargerid());


            Thread.sleep(sleepTime);
            i++;

            if(maxTimeout < i*sleepTime){
                throw new TimeOutException("timeout 에러");
            }
        }


        log.info("앱서버에 응답할 대여응답 데이터: " + respData);
        globalVar.globalDispatchData.clear();

        return respData;
    }



    @PostMapping("/return")
    public RespData returnRequest(@Valid ReqData returnReq) throws IOException, InterruptedException {

        log.info("전기자전거 반납요청 옴: " + returnReq);

        CMReqDto cmReqDto = new CMReqDto(Opcode.RETURN, returnReq);//Opcode.RENTAL.getCode()
        socketReadService.sendToCharger(cmReqDto);

        RespData respData = null;

        int i = 1;

        //timeOut을 설정
        while (respData == null){
            respData = globalVar.globalDispatchData.get(returnReq.getChargerid());
            Thread.sleep(sleepTime);
            i++;

            if(maxTimeout < i*sleepTime){
                throw new TimeOutException("timeout 에러");
            }
        }


        log.info("앱서버에 응답할 반납응답 데이터: " + respData);
        globalVar.globalDispatchData.clear();

        return respData;

    }


}
