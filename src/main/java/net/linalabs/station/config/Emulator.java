package net.linalabs.station.config;

import lombok.RequiredArgsConstructor;
import net.linalabs.station.service.ChargerSocketService;
import net.linalabs.station.utills.Common;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Configuration
public class Emulator {

    // private final SocketService socketService;
    private final ChargerSocketService chargerSocketService;

    @PostConstruct
    public void socketStart() throws IOException, ExecutionException, InterruptedException {

        //socketService.serverSocketStart();

        for (Integer item : Common.soketSelectlist) {
            chargerSocketService.clientSocketStart(item);
        }

        //       clientSocketService.clientSocketStart(1);


    }

}
