package net.linalabs.station.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.dto.req.CMReqDto;
import net.linalabs.station.dto.resp.CMRespDto;
import net.linalabs.station.dto.resp.RespData;
import net.linalabs.station.utills.Common;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChargerSocketService {


    private final GlobalVar globalVar;
    private final ChargerReadService chargerReadService;
    ObjectMapper objectMapper = new ObjectMapper();


    @Async
    public void clientSocketStart(Integer socketSelectIp) throws IOException{
        // HL7 Test Panel에 보낼 프로토콜

        boolean bLoop = true;
        log.info("charger로 보내는 socket channel: " + Common.clientSocketIp +  String.valueOf(socketSelectIp + 20)  +" , " + Common.clientSocketPort);
        SocketChannel socketChannel = null; //socketchannel을 안에 선언!

        while (bLoop) {

            socketChannel = SocketChannel.open();


            try {
                socketChannel.connect(new InetSocketAddress(Common.clientSocketIp +  String.valueOf(socketSelectIp + 20) , Common.clientSocketPort));
                socketChannel.configureBlocking(false);// Non-Blocking I/O

                log.info("socketChannel connected to port " + Common.clientSocketPort +   " And " + Common.clientSocketIp+String.valueOf(socketSelectIp + 20));
                //clientReadService.readSocketData(socketChannel);
                readSocketData(socketChannel, socketSelectIp);

            } catch (Exception e2) {
                log.debug("clientSocket connected refused!!!");
                //e2.printStackTrace();
            }


        }
    }


    public void readSocketData(SocketChannel schn, Integer socketSelectIp) throws IOException {

        //concurrentConfig.globalSocketMap.put("cs", schn);
        globalVar.globalSocket.put(socketSelectIp.toString() , schn);
        log.info("Client-socket 담김: "+ socketSelectIp.toString() + " " + schn);

        boolean isRunning = true; // 일단 추가, socketWork 중지할지 안 중지할지

        while (isRunning && schn.isConnected()) {

            ByteBuffer readBuf = ByteBuffer.allocate(300); // 버퍼 메모리 공간확보
            int bytesRead = schn.read(readBuf);

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

                if(bytesRead == 0 && result.length() > 0 ) {

                    log.info("totalResult: " + result);
                    clasfy(result, schn);

                    result="";
                    break;

                }else if(bytesRead == 0){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }

            }



        }// 연결돼있다면 무한루프


        log.info("소켓 닫기");
        schn.close(); // 소켓 닫기
    }


    public void clasfy(String result, SocketChannel schn) throws IOException {
        //여기서 일괄적으로 RestTemplate으로 응답하는게 낫겠다.
        log.info("clasfy 분류: " + result);

        //byte[] chargerResponse = result.getBytes();	//문자열을 바이트 배열고 변경

        String chargerResponse = Common.byteArrayToHexaString(result.getBytes());
        System.out.println(" byte array를 16진수 문자열로 변환 : "+ chargerResponse);

        String[] splitArray = chargerResponse.split("\\s");

        for (String resp: splitArray) {
            System.out.println("resp: " + resp);

        }


    }


    @Async
    public void sendToCharger(CMReqDto cmReqDto) throws IOException { //일단 읽는 거 신경안쓰고,

        log.info("App server에사 보내는 chargeId: " + cmReqDto.getData().getChargerid());
        //String jsonData = objectMapper.writeValueAsString(cmReqDto);
        log.info("파싱된 요청 데이터: " + cmReqDto);

        String chargeId = String.valueOf(cmReqDto.getData().getChargerid());
        SocketChannel schn = globalVar.globalSocket.get(chargeId); //여기서 인자로 stationId를


        byte[] SendBuf = {0x1B,00, 0x10, 00, 00,00,00,00,00,0xD};
        SendBuf[Common.TCP_PACKET - 1] = 0xD;

        //10~19    0~8 + 20
        SendBuf[1] = (byte)(cmReqDto.getData().getChargerid()+20);

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

        }


        if (schn.isConnected()) {
            log.info("Socket channel이 정상적으로 연결되었고 버퍼를 씁니다.");
            schn.write(ByteBuffer.wrap(SendBuf));

        } else if (!schn.isConnected()) {
            log.info("Socket channel이 연결이 끊어졌습니다.");
        }

    }

}
