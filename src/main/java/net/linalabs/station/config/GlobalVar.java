package net.linalabs.station.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class GlobalVar {

    @Value("${ip}")
    public String ip;

    @Value("${socketPort}")
    public Integer socketPort;

    public ConcurrentHashMap<String, SocketChannel> globalSocket = new ConcurrentHashMap<>();

}
