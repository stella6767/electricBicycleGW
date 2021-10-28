package net.linalabs.station.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.utills.Common;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClientSocketService {



    private final GlobalVar globalVar;
    private final ClientReadService clientReadService;


    @Async
    public void clientSocketStart(Integer socketSelectIp) throws IOException {
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

    //비동기 처리 안 하면 하나밖에 못 읽음 무한루프 하나에서 막히니까
    public void readSocketData(SocketChannel schn, Integer socketSelectIp) throws IOException {

        //concurrentConfig.globalSocketMap.put("cs", schn);
        globalVar.globalSocket.put("client " + socketSelectIp.toString() , schn);
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
                    //System.out.print((char) readBuf.get()); // read 1 byte at a time
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
                    //MSHClsfy(result);

                    result="";
                    break;
                }

            }

        }// 연결돼있다면 무한루프


        log.info("소켓 닫기");
        schn.close(); // 소켓 닫기
    }



    public void writeSocket(String data) throws JsonProcessingException {

        log.info("???");
        //log.info("CS에게 HL7 protocol 전송: " + Hl7parsingData);
        ByteBuffer writBuf = ByteBuffer.allocate(10);

        SocketChannel schn = globalVar.globalSocket.get("client 1");

        //byte[] SendBuf = new byte[Common.TCP_PACKET];

        byte[] SendBuf = {0x1B,(byte)(1+20), 0x10, 00, 00,00,00,00,00,0xD};

//        SendBuf[0] = 0x1B;
//        SendBuf[1] = (byte)(1+20);  //10~19
//        SendBuf[2] = 0x10;
//        SendBuf[3] = 00;
//        SendBuf[4] = 00;
//        SendBuf[5] = 00;
//        SendBuf[6] = 00;
//        SendBuf[7] = 00;
//        SendBuf[8] = 00;


        SendBuf[Common.TCP_PACKET - 1] = 0xD;

        log.info("1");
 //       ByteBuffer writBuf2 = ByteBuffer.wrap(new byte[] {SendBuf[0], SendBuf[1], SendBuf[2], SendBuf[Common.TCP_PACKET-1]});

        writBuf.put(SendBuf[0]);
        writBuf.put(SendBuf[1]);
        writBuf.put(SendBuf[2]);
        writBuf.put(SendBuf[3]);
        writBuf.put(SendBuf[4]);
        writBuf.put(SendBuf[5]);
        writBuf.put(SendBuf[6]);
        writBuf.put(SendBuf[7]);
        writBuf.put(SendBuf[8]);
        writBuf.put(SendBuf[Common.TCP_PACKET - 1]);

//        writBuf.put((byte)0x1B)
//                .put((byte)11)
//                .put((byte)10)
//                .rewind();
        log.info("2");
        System.out.println(Arrays.toString(writBuf.array()));
//        writBuf = Common.str_to_bb(Hl7parsingData);


               writBuf.flip();

        if (schn.isConnected()) {
            log.info("cssocket channel이 정상적으로 연결되었습니다.");
            while (writBuf.hasRemaining()) {
                log.info("SocketChannel open-3");
                try {
                    schn.write(writBuf);
                } catch (IOException e) {
                    log.info("close exception?  " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } else if (!schn.isConnected()) {
            log.info("cssocket channel이 연결이 끊어졌습니다.");
        }

        log.info("writeBuffer 초기화");


        writBuf.clear();


    }



}
