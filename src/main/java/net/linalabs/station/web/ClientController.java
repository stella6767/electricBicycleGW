package net.linalabs.station.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.linalabs.station.config.GlobalVar;
import net.linalabs.station.service.ClientSocketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ClientController {

    private final GlobalVar globalVar;
    private final ClientSocketService clientSocketService;


    @GetMapping("/clientSend")
    public void clientSend() throws JsonProcessingException {

        clientSocketService.writeSocket("");
    }


}
