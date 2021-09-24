package net.linalabs.station.config;

import lombok.RequiredArgsConstructor;
import net.linalabs.station.service.SocketService;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Configuration
public class Emulator {

    private final SocketService socketService;

    @PostConstruct
    public void socketStart() throws IOException, ExecutionException, InterruptedException {

        CompletableFuture<SocketChannel> completableFuture = socketService.createServerSocket();
        SocketChannel schn = completableFuture.get(); //block
        //먼저 Staion Server를 킨 다음, charge를 켜서 연결한 다음 작동하도록
        System.out.println("channel: " + schn);
        socketService.readSocketData(schn);



    }

}
