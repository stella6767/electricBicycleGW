package net.linalabs.station.config;

import lombok.RequiredArgsConstructor;
import net.linalabs.station.service.ClientSocketService;
import net.linalabs.station.service.SocketService;
import net.linalabs.station.utills.Common;
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
    private final ClientSocketService clientSocketService;

    @PostConstruct
    public void socketStart() throws IOException, ExecutionException, InterruptedException {

            socketService.serverSocketStart();

        for (Integer item :Common.soketSelectlist) {
            clientSocketService.clientSocketStart(item);
        }

        //clientSocketService.clientSocketStart(1);


    }

}
