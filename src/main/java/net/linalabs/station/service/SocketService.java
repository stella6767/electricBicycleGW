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
            serverSocketChannel.bind(new InetSocketAddress(5051)); // socket().

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




//    @Async
//    public void socketClient() throws IOException {
//
//        SocketChannel schn = null;
//
//        schn = SocketChannel.open();
//        try {
//            schn.connect(new InetSocketAddress(globalVar.ip, 5051));
//            schn.configureBlocking(true);// Non-Blocking I/O
//            log.info("socketChannel connected to port 5051");
//            globalVar.globalSocket.put("schnClient", schn);
//
//        } catch (Exception e2) {
//            log.debug("connected refused!!!");
//        }
//
//    }



    public void sendToCharger(String jsonData) throws IOException {

        SocketChannel schn = globalVar.globalSocket.get("schn");
        ByteBuffer writeBuf = ByteBuffer.allocate(10240);

        if(schn.isConnected()) {
            log.info("Socket channel이 정상적으로 연결되었고 버퍼를 씁니다.");
            writeBuf.flip();
            writeBuf = Common.str_to_bb(jsonData);

            schn.write(writeBuf);
            writeBuf.clear();

        }else if(!schn.isConnected()) {
            log.info("Socket channel이 연결이 끊어졌습니다.");
        }

    }


}


