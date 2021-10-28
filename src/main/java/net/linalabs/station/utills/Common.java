package net.linalabs.station.utills;

import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

@Slf4j
public class Common {

    public static final Integer stationId = 1;

    public static final Integer[] soketSelectlist = {0, 1, 2, 3, 4, 5, 6, 7, 8}; //chargerId

    public static final String clientSocketIp = "192.168.250.";

    public static final Integer clientSocketPort = 5000;

    public static Integer updateMin = 1000 * 60;


    public static final byte TCP_PACKET = 10;


    public static ByteBuffer str_to_bb(String msg) {
        Charset charset = Charset.forName("UTF-8");
        CharsetEncoder encoder = charset.newEncoder();
        CharsetDecoder decoder = charset.newDecoder();
        try {
            return encoder.encode(CharBuffer.wrap(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String byteArrayToHexaString(byte[] bytes) {
        StringBuffer builder = new StringBuffer();
        for (byte data : bytes) {
            builder.append(String.format("%02X ", data));
        }
        return builder.toString();
    }

    public static String getPrintStackTrace(Exception e) {

        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

        return errors.toString();

    }

}
