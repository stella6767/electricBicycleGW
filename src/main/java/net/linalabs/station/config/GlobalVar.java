package net.linalabs.station.config;

import net.linalabs.station.dto.resp.RespData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletResponse;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class GlobalVar {

    @Value("${ip}")
    public String ip;

    @Value("${socketPort}")
    public Integer socketPort;

    @Value("${dockingUrl}")
    public String dockingUrl;

    public ConcurrentHashMap<String, SocketChannel> globalSocket = new ConcurrentHashMap<>();

    public ConcurrentHashMap<Integer, RespData> globalDispatchData = new ConcurrentHashMap<>();

    //public ConcurrentHashMap<String, HttpServletResponse> globalResponse = new ConcurrentHashMap<>();

}
