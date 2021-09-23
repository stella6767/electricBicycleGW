package net.linalabs.station.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.dto.CMReqDto;
import net.linalabs.station.service.SocketService;
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

    @PostMapping("/rental")
    public void rentalRequest(@RequestBody CMReqDto rentalReq, HttpServletResponse response) throws IOException, ExecutionException, InterruptedException {

        log.info("전기자전거 대여요청 옴: " + rentalReq);
        rentalReq.setOpcode("rental");
        String jsonRespData = objectMapper.writeValueAsString(rentalReq);
        log.info("파싱된 대여요청 데이터: " + jsonRespData);
        socketService.sendToCharger(jsonRespData);
        globalVar.globalResponse.put("response", response); //진짜 마음에 안 드는데..

        //return completableFuture.get();

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
