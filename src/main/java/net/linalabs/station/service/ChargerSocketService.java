package net.linalabs.station.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.dto.Opcode;
import net.linalabs.station.dto.req.CMReqDto;
import net.linalabs.station.dto.resp.CMRespDto;
import net.linalabs.station.dto.resp.RespData;
import net.linalabs.station.utills.Common;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChargerSocketService {


    private final GlobalVar globalVar;




    @Async
    public void clientSocketStart(Integer socketSelectIp) throws IOException {
        // HL7 Test Panel에 보낼 프로토콜

        boolean bLoop = true;
        log.info("charger로 보내는 socket channel: " + Common.clientSocketIp + String.valueOf(socketSelectIp + 20) + " , " + Common.clientSocketPort);
        SocketChannel socketChannel = null; //socketchannel을 안에 선언!

        while (bLoop) {

            socketChannel = SocketChannel.open();


            try {
                socketChannel.connect(new InetSocketAddress(Common.clientSocketIp + String.valueOf(socketSelectIp + 20), Common.clientSocketPort));
                socketChannel.configureBlocking(false);// Non-Blocking I/O

                log.info("socketChannel connected to port " + Common.clientSocketPort + " And " + Common.clientSocketIp + String.valueOf(socketSelectIp + 20));
                //clientReadService.readSocketData(socketChannel);
                readSocketData(socketChannel, socketSelectIp);

            } catch (Exception e2) {
                log.info("clientSocket connected refused!!!");

                log.info("Exception" + Common.getPrintStackTrace(e2));

                //e2.printStackTrace();
            }


        }
    }


    public void readSocketData(SocketChannel schn, Integer socketSelectIp) {

        //concurrentConfig.globalSocketMap.put("cs", schn);
        globalVar.globalSocket.put(socketSelectIp.toString(), schn);
        log.info("Client-socket 담김: " + socketSelectIp.toString() + " " + schn);

        boolean isRunning = true; // 일단 추가, socketWork 중지할지 안 중지할지

        while (isRunning && schn.isConnected()) {

            ByteBuffer readBuf = ByteBuffer.allocate(300); // 버퍼 메모리 공간확보
            int bytesRead = 0;
            try {
                bytesRead = schn.read(readBuf);

                String result = "";

                while (bytesRead != -1) {// 만약 소켓채널을 통해 buffer에 데이터를 받아왔으면

                    readBuf.flip(); // make buffer ready for read
                    // 10240로 정의한 buffer의 크기를 실제 데이터의 크기로 flip() 함

                    while (readBuf.hasRemaining()) {
                        //log.info("!!!");
                        // read 1 byte at a time
                        //log.info("readBuf.hasRemaining1():  " + readBuf.hasRemaining() );
                        result = result + String.valueOf(((char) readBuf.get()));
                        //log.info("result1: " + result);
                    }
                    //log.info("가"+result);
                    readBuf.clear(); //make buffer ready for writing
                    //readBuf.compact();
                    bytesRead = schn.read(readBuf);
                    //log.info("byteRead size: " + bytesRead);
                    //log.info("resultLength: " + result.length());

                    if (bytesRead == 0 && result.length() > 0) {

                        log.info("totalResult: " + result);
                        clasfy(result, socketSelectIp);

                        result = "";
                        break;

                    } else if (bytesRead == 0) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            log.info("Exception" + Common.getPrintStackTrace(e));


                            return;
                        }
                    }

                }

            } catch (Exception e) {
                log.info("Exception" + Common.getPrintStackTrace(e));
                log.info("소켓 닫기");
                try {
                    schn.close(); // 소켓 닫기
                } catch (Exception ex) {
                    log.info("Exception" + Common.getPrintStackTrace(ex));
                }
            }

        }// 연결돼있다면 무한루프

    }


    public void clasfy(String result, Integer chargerId) throws IOException {
        //여기서 일괄적으로 RestTemplate으로 응답하는게 낫겠다.
        log.info("clasfy 분류: " + result);

        //byte[] chargerResponse = result.getBytes();	//문자열을 바이트 배열고 변경

        String chargerResponse = Common.byteArrayToHexaString(result.getBytes());
        log.info(" byte array를 16진수 문자열로 변환 : " + chargerResponse);

        String[] splitArray = chargerResponse.split("\\s");

        for (String resp : splitArray) {
            log.info("resp: " + resp);
        }


        updateRespProceed(chargerId, splitArray);

        if (splitArray[Common.TCP_PACKET - 3].equals("00")) {
            log.info("Lock " + splitArray[Common.TCP_PACKET - 3]);
            dockingRespProceed(chargerId, 2);

        } else {
            log.info("unLock " + splitArray[Common.TCP_PACKET - 3]);
            dockingRespProceed(chargerId, 1);
        }

    }

    public void updateRespProceed(Integer chargerId, String[] resultArray) throws IOException { //요거는 소켓에서 정보를 다 보내줘여..
        ObjectMapper objectMapper = new ObjectMapper();
        RestTemplate rt = new RestTemplate();

        rt.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));

        String battery = resultArray[3];
        String mobilityId = resultArray[6];


        log.info("지속적 전송을 위한 사전준비: " + battery + "  " + mobilityId);

        RespData respData = RespData.builder()
                .stationid(Common.stationId)
                .chargerid(chargerId)
                .slotno(1)
                .mobilityid(Integer.valueOf(mobilityId, 16))
                .battery(Integer.valueOf(battery,16))
                .build();

        log.info("1분마다 받은 데이터를 등록: " + respData);

        //중복제거를 위해
        globalVar.updateData.put(chargerId, respData);
        //RespData data = updateData.get(cmRespDto.getData().getChargerId());
        List list = new ArrayList(globalVar.updateData.values());

        log.info("중복제거된 list...: " + list);

        globalVar.globalUpdateList = list;

        //globalVar.globalUpdateList.add(updateData); //중복제거는 chargeId 기준으로 해야 되는데..
        //globalVar.globalUpdateList.stream().map(d-> d.getChargerId() == data.getChargerId()).collect(Collectors.toList());

    }


    //도킹 또는 도킹해제 시
    public void dockingRespProceed(Integer chargerId, Integer docked) throws IOException { //요거는 소켓에서 정보를 다 보내줘여..
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        rt.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));


        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setAcceptCharset(Arrays.asList(Charset.forName("UTF-8")));


        MultiValueMap<String, Integer> map = new LinkedMultiValueMap<String, Integer>();

        map.add("stationid", Common.stationId);
        map.add("chargerid", chargerId);
        map.add("slotno", 1);
        map.add("docked", docked);
        map.add("mobilityid", 1);

        log.info("dockingRequest map: " + map.toString());

        HttpEntity<MultiValueMap<String, Integer>> request = new HttpEntity<MultiValueMap<String, Integer>>(map, headers);
        ResponseEntity<RespData> response = rt.postForEntity(globalVar.dockingUrl, request, RespData.class);

        //String decodedResult = UriUtils.decode(response.getBody(),"UTF-8");
        log.info("docking response: " + response.getBody());

    }


    //@Scheduled(initialDelay = 1000*60, fixedDelay = 1000 * 60)
    public void scheuledUpdate() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        RestTemplate rt = new RestTemplate();
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


    @Async
    public void sendToCharger(CMReqDto cmReqDto) throws IOException { //일단 읽는 거 신경안쓰고,

        log.info("App server에사 보내는 chargeId: " + cmReqDto.getData().getChargerid());
        //String jsonData = objectMapper.writeValueAsString(cmReqDto);
        log.info("파싱된 요청 데이터: " + cmReqDto);

        String chargeId = String.valueOf(cmReqDto.getData().getChargerid());
        SocketChannel schn = globalVar.globalSocket.get(chargeId); //여기서 인자로 stationId를


        byte[] SendBuf = {0x1B, 00, 0x10, 00, 00, 00, 00, 00, 00, 0xD};
        SendBuf[Common.TCP_PACKET - 1] = 0xD;

        //10~19    0~8 + 20
        SendBuf[1] = (byte) (cmReqDto.getData().getChargerid() + 20);

        switch (cmReqDto.getOpcode()) {
            case RENTAL:
                //SendBuf[2] = 0x12; //언락이 의미가없음. 기기에서 눌러야 됨

                RespData rentalData = RespData.builder()
                        .result_code(0)
                        .result_message("대여 요청되었습니다")
                        .stationid(Common.stationId)
                        .chargerid(Integer.valueOf(chargeId))
                        .build();
                //연동되는 기기에서는 stationID, chartgerId, mobilityId가 랜덤하게 생성돼서, 사실상 의미가 없다...
                globalVar.globalDispatchData.put(Integer.valueOf(chargeId), rentalData);
                break;

            case RETURN:
                //SendBuf[2] = 0x11; //락이 의미가없음. 기기에서 눌러야 됨
                RespData returnData = RespData.builder()
                        .result_code(0)
                        .result_message("반납 요청되었습니다.")
                        .chargerid(Integer.valueOf(chargeId))
                        .build();
                //연동되는 기기에서는 stationID, chartgerId, mobilityId가 랜덤하게 생성돼서, 사실상 의미가 없다...
                globalVar.globalDispatchData.put(Integer.valueOf(chargeId), returnData);

                break;


            case UPDATE:
                log.info("update.. " + chargeId);

                break;

            default:
                break;
        }


        if (schn.isConnected()) {
            log.info("Socket channel이 정상적으로 연결되었고 버퍼를 씁니다.");
            schn.write(ByteBuffer.wrap(SendBuf));

        } else if (!schn.isConnected()) {
            log.info("Socket channel이 연결이 끊어졌습니다.");
        }

    }

}
