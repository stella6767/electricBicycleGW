package net.linalabs.station.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.utills.Common;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
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


    public Integer chargerid; //요렇게 하는 게 맞나싶다..


//    public void setChargerid(Integer chargerid){
//        this.chargerid = chargerid;
//    }



    @Async
    public CompletableFuture<SocketChannel> createServerSocket() {
        ServerSocketChannel serverSocketChannel = null; // ServerSocketChannel은 하나
        SocketChannel schn = null;


        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(globalVar.socketPort)); // socket().
            log.info("socketStart");

            try {
                log.info("socket이 연결이 될 때까지 블록킹");
                schn = serverSocketChannel.accept(); // 이 부분에서 연결이 될때까지 블로킹
                schn.configureBlocking(true); // 블록킹 방식
                log.info("socket connected 5051 port");

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return CompletableFuture.completedFuture(schn); // 다른 대안 탐색중..
    }


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
                    //globalVar.globalSocket.put("schn", schn);
                    readSocketData(schn);

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

        SocketChannel schn = globalVar.globalSocket.get("schn"); //여기서 인자로 stationId를



        ByteBuffer writeBuf = ByteBuffer.allocate(10240);

        if (schn.isConnected()) {
            log.info("Socket channel이 정상적으로 연결되었고 버퍼를 씁니다.");
            writeBuf = Common.str_to_bb(jsonData);
            schn.write(writeBuf);


        } else if (!schn.isConnected()) {
            log.info("Socket channel이 연결이 끊어졌습니다.");
        }

    }


//    public String readSocketData() throws IOException { //일단 읽는 거 신경안쓰고,
//
//        SocketChannel schn = globalVar.globalSocket.get("schn");
//
//        ByteBuffer readBuf = ByteBuffer.allocate(10240);
//
//        Charset charset = Charset.forName("UTF-8");
//
//        if(schn.isConnected()) {
//            log.info("Socket channel이 정상적으로 연결되었고 버퍼를 읽습니다.");
//
//            schn.read(readBuf); // 클라이언트로부터 데이터 읽기
//            readBuf.flip();
//            //log.info("rental Received Data : " + charset.decode(readBuf).toString());
//
//
//        }else if(!schn.isConnected()) {
//            log.info("Socket channel이 연결이 끊어졌습니다.");
//        }
//
//        return charset.decode(readBuf).toString();
//        //return  CompletableFuture.completedFuture(charset.decode(readBuf).toString());
//    }


    //@Async
    public void readSocketData(SocketChannel schn) throws IOException { //여기서 일괄적으로 RestTemplate으로 응답하는게 낫겠다.

        log.info("docking 여부 리스닝 ");
        //SocketChannel schn = globalVar.globalSocket.get("schn");

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

//                            globalVar.globalSocket.put("chargerid", );
                            //dockingResp();
                            clasfy(result);

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


    public void clasfy(String result) throws IOException {

        log.info("clsfy: " + result);

        JSONParser parser = new JSONParser();
        JSONObject obj;

        try {
            obj = (JSONObject) parser.parse(result);
            String opCode = String.valueOf(obj.get("opcode"));

            switch (opCode) {

                case "rental":
                    break;

                case "return":

                    break;

                case "docking":

                    break;

            }


        } catch (ParseException e) {
            e.printStackTrace();
        }

        //if(result.) 여기서 동기화를 해야겠구만..

        //globalVar.globalSocket.put("chargerid", );

        //throw new MRentalException(result); //다른 스레드에서는 안 먹히네..

//        HttpServletResponse response =  globalVar.globalResponse.get("resp");
//
//        PrintWriter out = response.getWriter();
//
//        out.println(result);
//        out.flush();

    }


    public void restAPi() {

        //channelId..
        //여기서 끄내고 바로지우고,

        globalVar.globalDispatchData.put("", "");


    }


//    public void dockingResp() {
//
//        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//
//
//        CMReqDto cmReqDto = CMReqDto.builder()
//                .stationid(3)
//                .chargerid(12)
//                .docked(1)
//                .mobilityid(15)
//                .build();
//
//        log.info("cmReqDto: " + cmReqDto);
//
//        String jsonData = null;
//
//        try {
//            jsonData = objectMapper.writeValueAsString(cmReqDto);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//
//        HttpEntity<String> request = new HttpEntity<>(jsonData, headers);
//
//        ResponseEntity response = rt.exchange(globalVar.dockingUrl, HttpMethod.POST, request, String.class);
//
//        log.info("docking response: " + response.getBody());
//
//
//    }


}


