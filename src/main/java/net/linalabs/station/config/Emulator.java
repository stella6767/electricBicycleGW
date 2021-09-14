package net.linalabs.station.config;

import lombok.RequiredArgsConstructor;
import net.linalabs.station.service.SocketService;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Configuration
public class Emulator {

    private final SocketService socketService;

    @PostConstruct
    public void socketStart() throws IOException {
        //socketService.socketClient();
        socketService.emulSocketStart();

    }

}
