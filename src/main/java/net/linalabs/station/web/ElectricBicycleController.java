package net.linalabs.station.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.dto.Opcode;
import net.linalabs.station.dto.req.CMReqDto;
import net.linalabs.station.dto.req.ReqData;
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

    private ObjectMapper objectMapper = new ObjectMapper();




    @PostMapping("/rental")
    public String rentalRequest(@RequestBody ReqData rentalReq, HttpServletResponse response) throws IOException, ExecutionException, InterruptedException {

        log.info("전기자전거 대여요청 옴: " + rentalReq);
        //opCode는 나중에 enum으로 분류

        socketService.chargerid = rentalReq.getChargerid();

        CMReqDto cmReqDto = new CMReqDto(Opcode.RENTAL, rentalReq);//Opcode.RENTAL.getCode()

        String jsonRespData = objectMapper.writeValueAsString(cmReqDto);
        log.info("파싱된 대여요청 데이터: " + jsonRespData);
        socketService.sendToCharger(jsonRespData);




        //globalVar.globalResponse.put("resp", response);

        //String aa = socketService.readSocketData();
        //throw new MRentalException("aaaaa");
        return null;
        //return aa;

    }



    @PostMapping("/return")
    public void returnRequest(@RequestBody ReqData returnReq) throws IOException {

        log.info("전기자전거 반납요청 옴: " + returnReq);
//        rentalReq.setOpcode("return");
//        String jsonRespData = objectMapper.writeValueAsString(rentalReq);
//        log.info("파싱된 대여요청 데이터: " + jsonRespData);
        //String response = socketService.sendToChargerAndRespRead(jsonRespData);


    }


}
