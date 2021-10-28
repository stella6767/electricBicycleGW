package net.linalabs.station.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClientReadService {

    private final GlobalVar globalVar;

    @Async
    public void readSocketData(SocketChannel schn) throws IOException {

        //concurrentConfig.globalSocketMap.put("cs", schn);
        globalVar.globalSocket.put("client", schn);
        log.info("Client-socket 담김: " + schn);

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


}
