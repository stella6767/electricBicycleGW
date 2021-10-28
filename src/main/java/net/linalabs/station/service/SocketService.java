//package net.linalabs.station.service;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.linalabs.station.config.GlobalVar;
//import net.linalabs.station.dto.Opcode;
//import net.linalabs.station.dto.req.CMReqDto;
//import net.linalabs.station.dto.resp.CMRespDto;
//import net.linalabs.station.dto.resp.RespData;
//import net.linalabs.station.utills.Common;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//import org.springframework.http.*;
//import org.springframework.http.converter.StringHttpMessageConverter;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriUtils;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.nio.ByteBuffer;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//import static net.linalabs.station.dto.Opcode.RENTAL;
//
//@Slf4j
//@RequiredArgsConstructor
//@Service
//public class SocketService {
//
//    private final GlobalVar globalVar;
//    //private final SocketReadService socketReadService;
//
//
//
//    @Async
//    public void serverSocketStart() {
//
//        try {
//
//            ServerSocketChannel serverSocketChannel = null; // ServerSocketChannel은 하나
//
//            serverSocketChannel = ServerSocketChannel.open();
//            serverSocketChannel.bind(new InetSocketAddress(globalVar.socketPort)); // socket().
//
//            boolean bLoop = true;
//
//            log.info("socketStart");
//
//            while (bLoop) {
//
//                try {
//                    log.info("socket이 연결이 될 때까지 블록킹");
//                    SocketChannel schn = null;
//
//                    schn = serverSocketChannel.accept(); // 이 부분에서 연결이 될때까지 블로킹
//                    schn.configureBlocking(true); // 블록킹 방식
//
//                    log.info("socket connected 5051 port");
//                    socketReadService.readSocketData(schn);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//
//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//
//    }
//
//
//
//
//}
//
//
