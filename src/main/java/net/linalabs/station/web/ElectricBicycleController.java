package net.linalabs.station.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.dto.Opcode;
import net.linalabs.station.dto.req.CMReqDto;
import net.linalabs.station.dto.req.ReqData;
import net.linalabs.station.dto.resp.RespData;
import net.linalabs.station.handler.TimeOutException;
import net.linalabs.station.service.SocketService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ElectricBicycleController {

    private final SocketService socketService;
    private final GlobalVar globalVar;

    private final int maxTimeout = 3000;
    private final int sleepTime = 10;

    @PostMapping("/rental")
    public RespData rentalRequest(ReqData rentalReq, HttpServletResponse response) throws IOException, ExecutionException, InterruptedException {

        log.info("전기자전거 대여요청 옴: " + rentalReq);

        CMReqDto cmReqDto = new CMReqDto(Opcode.RENTAL, rentalReq);//Opcode.RENTAL.getCode()
        socketService.sendToCharger(cmReqDto);

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
    public void returnRequest(ReqData returnReq) throws IOException {

        log.info("전기자전거 반납요청 옴: " + returnReq);

        CMReqDto cmReqDto = new CMReqDto(Opcode.RETURN, returnReq);//Opcode.RENTAL.getCode()
        socketService.sendToCharger(cmReqDto);

//        String jsonRespData = objectMapper.writeValueAsString(rentalReq);
//        log.info("파싱된 대여요청 데이터: " + jsonRespData);
        //String response = socketService.sendToChargerAndRespRead(jsonRespData);


    }


}
