package net.linalabs.station.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.dto.CMReqDto;
import net.linalabs.station.handler.MRentalException;
import net.linalabs.station.service.SocketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ElectricBicycleController {

    private final SocketService socketService;
    private final GlobalVar globalVar;

    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/rental2")
    public String aa(){
        log.info("dddddddddd");

        return "aaaaaaaasssss";
    }



    @PostMapping("/rental")
    public String rentalRequest(@RequestBody CMReqDto rentalReq, HttpServletResponse response) throws IOException, ExecutionException, InterruptedException {

        log.info("전기자전거 대여요청 옴: " + rentalReq);
        rentalReq.setOpcode("rental");
        String jsonRespData = objectMapper.writeValueAsString(rentalReq);
        log.info("파싱된 대여요청 데이터: " + jsonRespData);
        socketService.sendToCharger(jsonRespData);

        //String aa = socketService.readSocketData();
        //throw new MRentalException("aaaaa");
        return null;
        //return aa;

    }



    @PostMapping("/return")
    public void returnRequest(@RequestBody CMReqDto rentalReq) throws IOException {

        log.info("전기자전거 반납요청 옴: " + rentalReq);
        rentalReq.setOpcode("return");
        String jsonRespData = objectMapper.writeValueAsString(rentalReq);
        log.info("파싱된 대여요청 데이터: " + jsonRespData);
        //String response = socketService.sendToChargerAndRespRead(jsonRespData);


    }


}
