package net.linalabs.station.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.utills.Common;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChargerReadService {

    private final GlobalVar globalVar;

    public void readSocketData2(SocketChannel schn) throws IOException {

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
                        // log.info("isRunning why: " + isRunning);
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

                            //clasfy(result, schn);
                            log.info("result 값.....: " + result);

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

    public void writeSocket(String data) throws IOException {

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

        schn.write(ByteBuffer.wrap(SendBuf));

//        if (schn.isConnected()) {
//            log.info("cssocket channel이 정상적으로 연결되었습니다.");
//            while (writBuf.hasRemaining()) {
//                log.info("SocketChannel open-3");
//                try {
//                    schn.write(writBuf);
//                } catch (IOException e) {
//                    log.info("close exception?  " + e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//
//        } else if (!schn.isConnected()) {
//            log.info("cssocket channel이 연결이 끊어졌습니다.");
//        }

        log.info("writeBuffer 초기화");


        writBuf.clear();


    }
}
