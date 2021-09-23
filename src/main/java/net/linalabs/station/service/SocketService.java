package net.linalabs.station.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.Emulator;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.dto.CMReqDto;
import net.linalabs.station.dto.CMRespDto;
import net.linalabs.station.utills.Common;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class SocketService {

    private final GlobalVar globalVar;
    private RestTemplate rt = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    ObjectMapper objectMapper = new ObjectMapper();
    private StringBuffer sb = new StringBuffer();



    @Async
    public void emulSocketStart() {

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
                    globalVar.globalSocket.put("schn", schn);
                    //readSocketData();

                } catch (Exception e) {
                    //logger.debug("AsynchronousCloseException 터짐");
                    //socketChannel.close();

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




    @Async
    public void sendToCharger(String jsonData) throws IOException { //일단 읽는 거 신경안쓰고,

        SocketChannel schn = globalVar.globalSocket.get("schn");
        ByteBuffer writeBuf = ByteBuffer.allocate(10240);

        if(schn.isConnected()) {
            log.info("Socket channel이 정상적으로 연결되었고 버퍼를 씁니다.");
            writeBuf = Common.str_to_bb(jsonData);
            schn.write(writeBuf);


        }else if(!schn.isConnected()) {
            log.info("Socket channel이 연결이 끊어졌습니다.");
        }

    }



    public String readSocketData() throws IOException { //일단 읽는 거 신경안쓰고,

        SocketChannel schn = globalVar.globalSocket.get("schn");

        ByteBuffer readBuf = ByteBuffer.allocate(10240);

        Charset charset = Charset.forName("UTF-8");

        if(schn.isConnected()) {
            log.info("Socket channel이 정상적으로 연결되었고 버퍼를 읽습니다.");

            schn.read(readBuf); // 클라이언트로부터 데이터 읽기
            readBuf.flip();
            //log.info("rental Received Data : " + charset.decode(readBuf).toString());


        }else if(!schn.isConnected()) {
            log.info("Socket channel이 연결이 끊어졌습니다.");
        }
        //schn.close();

        return charset.decode(readBuf).toString();
        //return  CompletableFuture.completedFuture(charset.decode(readBuf).toString());
    }





//    public String readSocketData() throws IOException { //여기서 일괄적으로 RestTemplate으로 응답하는게 낫겠다.
//
//        log.info("read socket Data");
//        SocketChannel schn = globalVar.globalSocket.get("schn");
//
//        log.info("read socket data");
//
//        String result = "";
//
//        byte[] readByteArr;
//
//        // Client로부터 글자 받기
//        ByteBuffer readBuf = ByteBuffer.allocate(10240);//read into buffer. 일단은 버퍼 초과 신경쓰지 않고
//        schn.read(readBuf); // 클라이언트로부터 데이터 읽기
//        readBuf.flip();
//
//        readByteArr = new byte[readBuf.remaining()];
//        readBuf.get(readByteArr); // 데이터 읽기
//        result = result + new String(readByteArr, Charset.forName("UTF-8")); // 어차피 여기서 계속 더하니까.
//
//        log.info("------------------------------처음 파싱되서 도착한 데이터---------------------------------------");
//        log.info(result);
//
//        //clasfy(result, schn);
//
//        log.info("Received Data : " + result);
//
//
//        schn.close(); //요기서 -1를 리턴해주는거만
//        return result;
//    }


    public String clasfy(String result) throws IOException {
        sb.delete(0, sb.length()); // 초기화

        log.info("clsfy: " + result);

        sb.append("ddddd" + result);

        return "okkkkk";
    }


    public String ddddd(){
        return sb.toString();
    }



    public void dockingResp(){

        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);


        CMReqDto cmReqDto = CMReqDto.builder()
                .stationid(3)
                .chargerid(12)
                .docked(1)
                .mobilityid(15)
                .build();

        log.info("cmReqDto: " + cmReqDto);

        String jsonData = null;

        try {
            jsonData = objectMapper.writeValueAsString(cmReqDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<>(jsonData, headers);

        ResponseEntity response = rt.exchange(globalVar.dockingUrl, HttpMethod.POST, request, String.class);

        log.info("docking response: " + response.getBody());


    }


}


