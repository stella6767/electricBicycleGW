package net.linalabs.station.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.dto.Opcode;
import net.linalabs.station.dto.req.CMReqDto;
import net.linalabs.station.dto.resp.CMRespDto;
import net.linalabs.station.utills.Common;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static net.linalabs.station.dto.Opcode.RENTAL;

@Slf4j
@RequiredArgsConstructor
@Service
public class SocketService {

    private final GlobalVar globalVar;
    private RestTemplate rt = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    ObjectMapper objectMapper = new ObjectMapper();
    private StringBuffer sb = new StringBuffer();
    JSONParser parser = new JSONParser();
    JSONObject obj;



    @Async
    public void serverSocketStart() {

        try {

            ServerSocketChannel serverSocketChannel = null; // ServerSocketChannel은 하나

            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(globalVar.socketPort)); // socket().

            boolean bLoop = true;

            log.info("socketStart");

            while (bLoop) {

                try {
                    log.info("socket이 연결이 될 때까지 블록킹");
                    SocketChannel schn = null;

                    schn = serverSocketChannel.accept(); // 이 부분에서 연결이 될때까지 블로킹
                    schn.configureBlocking(true); // 블록킹 방식

                    log.info("socket connected 5051 port");
                    readSocketData(schn);

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }



    //@Async //해야될지도?
    public void readSocketData(SocketChannel schn) throws IOException {

        log.info("read Charger Socket Client ");

        boolean isRunning = true; // 일단 추가, socketWork 중지할지 안 중지할지

        while (isRunning && schn.isConnected()) {

            try {
                long lThId = Thread.currentThread().getId();
                int byteCount = 0;
                byte[] readByteArr;

                // ByteBuffer readBuf = ByteBuffer.allocate(10); //버퍼 메모리 공간확보
                ByteBuffer readBuf = ByteBuffer.allocate(10240);

                log.info("첫번째  while문");

                // 무한 루프
                String result = ""; // 요기서 초기화

                while (byteCount >= 0) {

                    try {

                        byteCount = schn.read(readBuf); // 소켓채널에서 한번에 초과되는 버퍼사이즈의 데이터가 들어오면..

                        log.info("[gwEmulThread #100] TID[" + "] byteCount :  " + byteCount);
                        // logger.debug("isRunning why: " + isRunning);
                    } catch (Exception e) {
                        // e.printStackTrace();
                        log.info("갑자기 클라이언트 소켓이 닫혔을 시");
                        schn.close();
                        isRunning = false;
                        break;
                    }

                    int i = 0;

                    // 버퍼에 값이 있다면 계속 버퍼에서 값을 읽어 result 를 완성한다.
                    while (byteCount > 0) {


                        readBuf.flip(); // 입력된 데이터를 읽기 위해 read-mode로 바꿈, positon이 데이터의 시작인 0으로 이동
                        readByteArr = new byte[readBuf.remaining()]; // 현재 위치에서 limit까지 읽어드릴 수 있는 데이터의 개수를 리턴
                        readBuf.get(readByteArr); // 데이터 읽기

                        result = result + new String(readByteArr, Charset.forName("UTF-8"));

                        try {
                            byteCount = schn.read(readBuf);
                            log.info("[gwEmulThread #210] TID[" + result + "] byteCount :  " + byteCount);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // break;
                        }

                        boolean bEtxEnd = true; // 아래 while문을 실행할지 안할지

                        while (!result.equals("") && bEtxEnd) {

                            clasfy(result, schn);

                            result = "";
                            bEtxEnd = false;
                            readBuf.clear();
                        }

                    } // #ETX# 단위로 루프
                } // byteCount > 0

                log.info("소켓 닫기");
                schn.close(); // 소켓 닫기

            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }


    public void clasfy(String result, SocketChannel schn) throws IOException {
        //여기서 일괄적으로 RestTemplate으로 응답하는게 낫겠다.
        log.info("clasfy 분류: " + result);

        try {
            obj = (JSONObject) parser.parse(result);
            String opcode = String.valueOf(obj.get("opcode"));

            Opcode opCode = Opcode.valueOf(opcode);

            log.info("Opcode: " + opCode);

            switch (opCode) {
                case INIT:
                    initSocketChargerId(result, schn);
                    break;

                case RENTAL:
                    rentalRespProceed(result);
                    break;

                case DOCKING:
                    dockingRespProceed(result);
                    break;

                case RETURN:

                    break;


            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Async
    public void sendToCharger(CMReqDto cmReqDto) throws IOException { //일단 읽는 거 신경안쓰고,

        log.info("App server에사 보내는 chargeId: " + cmReqDto.getData().getChargerid());

        String chargeId = String.valueOf(cmReqDto.getData().getChargerid());

        SocketChannel schn = globalVar.globalSocket.get(chargeId); //여기서 인자로 stationId를
        String jsonData = objectMapper.writeValueAsString(cmReqDto);
        log.info("파싱된 대여요청 데이터: " + jsonData);

        ByteBuffer writeBuf = ByteBuffer.allocate(10240);

        if (schn.isConnected()) {
            log.info("Socket channel이 정상적으로 연결되었고 버퍼를 씁니다.");
            writeBuf = Common.str_to_bb(jsonData);
            schn.write(writeBuf);


        } else if (!schn.isConnected()) {
            log.info("Socket channel이 연결이 끊어졌습니다.");
        }

    }


    public void initSocketChargerId(String result, SocketChannel schn){ //최초 chargeId global hashmap에 socketchannel 동기화
        System.out.println("init: " + result);
        String chargerId = String.valueOf(obj.get("data"));
        globalVar.globalSocket.put(chargerId, schn);
    }



    public void rentalRespProceed(String result) throws JsonProcessingException {


        log.info("rental Resp: " + result);
        CMRespDto cmRespDto = objectMapper.readValue(result, CMRespDto.class); //여기서는 어쩔 수 없이 null값 포함. 나중에 메시지컨버터로 리턴할떄 어차피 빠지니

        log.info("파싱된 대여응답 데이터: " + cmRespDto);

        globalVar.globalDispatchData.put(cmRespDto.getData().getChargerId(), cmRespDto.getData());
    }


    public void dockingRespProceed(String result) throws JsonProcessingException {

        rt.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));

        log.info("docking Resp: " + result);
        CMRespDto cmRespDto = objectMapper.readValue(result, CMRespDto.class);
        log.info("파싱된 대여응답 데이터: " + cmRespDto);

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setAcceptCharset(Arrays.asList(Charset.forName("UTF-8")));

        MultiValueMap<String, Integer> map= new LinkedMultiValueMap<String, Integer>();

        map.add("stationid", cmRespDto.getData().getStationId());
        map.add("chargerid", cmRespDto.getData().getChargerId());
        map.add("slotno", cmRespDto.getData().getSlotno());
        map.add("docked", cmRespDto.getData().getDocked());
        map.add("mobilityid", cmRespDto.getData().getMobilityId());


        HttpEntity<MultiValueMap<String, Integer>> request = new HttpEntity<MultiValueMap<String, Integer>>(map, headers);
        ResponseEntity<String> response = rt.postForEntity(globalVar.dockingUrl, request , String.class );

        //String decodedResult = UriUtils.decode(response.getBody(),"UTF-8");
        log.info("docking response: " + response.getBody());
        //log.info("docking response2: " + decodedResult);

    }



}


