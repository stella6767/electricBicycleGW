package net.linalabs.station.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.dto.CMReqDto;
import net.linalabs.station.service.SocketService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ElectricBicycleController {

    private final SocketService socketService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/rental")
    public String rentalRequest(@RequestBody CMReqDto rentalReq) throws IOException, ExecutionException, InterruptedException {

        log.info("전기자전거 대여요청 옴: " + rentalReq);
        rentalReq.setOpcode("rental");
        String jsonRespData = objectMapper.writeValueAsString(rentalReq);
        log.info("파싱된 대여요청 데이터: " + jsonRespData);
        CompletableFuture<String> completableFuture = socketService.sendToChargerAndRespRead(jsonRespData);

        return completableFuture.get();

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
