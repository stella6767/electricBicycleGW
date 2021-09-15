package net.linalabs.station.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.Emulator;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.utills.Common;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

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

@Slf4j
@EnableAsync
@RequiredArgsConstructor
@Service
public class SocketService {

    private final GlobalVar globalVar;

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
    public String sendToChargerAndRespRead(String jsonData) throws IOException { //일단 읽는 거 신경안쓰고,

        SocketChannel schn = globalVar.globalSocket.get("schn");
        ByteBuffer writeBuf = ByteBuffer.allocate(10240);
        ByteBuffer readBuf = ByteBuffer.allocate(10240);

        Charset charset = Charset.forName("UTF-8");

        if(schn.isConnected()) {
            log.info("Socket channel이 정상적으로 연결되었고 버퍼를 씁니다.");
            writeBuf = Common.str_to_bb(jsonData);
            schn.write(writeBuf);

            schn.read(readBuf); // 클라이언트로부터 데이터 읽기
            readBuf.flip();
            //log.info("rental Received Data : " + charset.decode(readBuf).toString());


        }else if(!schn.isConnected()) {
            log.info("Socket channel이 연결이 끊어졌습니다.");
        }
        //schn.close();

        return charset.decode(readBuf).toString();
    }


}


