package net.linalabs.station.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.dto.Opcode;
import net.linalabs.station.dto.req.CMReqDto;
import net.linalabs.station.dto.req.ReqData;
import net.linalabs.station.dto.resp.RespData;
import net.linalabs.station.utills.Common;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScheuledService {

    private final GlobalVar globalVar;
    private final ChargerSocketService chargerSocketService;


    //@Scheduled(initialDelay = 10000, fixedDelay = 10000)
    public void chargerStatusPolling() throws IOException {  //지속적으로 상태값들을 charger들로부터 알아온다.

        for (Integer item : Common.soketSelectlist) {

            ReqData data = ReqData.builder()
                    .chargerid(item)
                    .build();
            CMReqDto cmReqDto = new CMReqDto(Opcode.UPDATE, data);

            chargerSocketService.sendToCharger(cmReqDto);

        }
    }


    //@Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 60)
    public void scheuledUpdate() throws JsonProcessingException {
        RestTemplate rt = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        log.info("1분마다 App 서버로 정보 전송 " + globalVar.globalUpdateList);

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setAcceptCharset(Arrays.asList(Charset.forName("UTF-8")));

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        String jsonStatueList = objectMapper.writeValueAsString(globalVar.globalUpdateList);
        log.info("jsonStatueList: " + jsonStatueList);


        map.add("statlist", jsonStatueList);
        log.info("statlist: " + map.toString());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        ResponseEntity<RespData> response = rt.postForEntity(globalVar.statusUpdateUrl, request, RespData.class);
        log.info("상시정보 업데이트 응답됨: " + response); //에러.........
    }


}
