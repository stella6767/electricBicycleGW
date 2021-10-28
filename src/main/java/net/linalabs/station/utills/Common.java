package net.linalabs.station.utills;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

@Slf4j
public class Common {

    public static final Integer[] soketSelectlist = {0,1,2,3,4,5,6,7,8};

    public static final String clientSocketIp =  "192.168.250.";

    public static final Integer clientSocketPort = 5000;

    public static Integer updateMin = 1000 * 60;


    public static final byte TCP_PACKET = 10;



//    public const byte TCP_CMD_RD_STATUS = 0x10;
//    public const byte TCP_CMD_LOCK_BIKE = 0x11;
//    public const byte TCP_CMD_UNLOCK_BIKE = 0x12;
//    SendBuf[0] = 0x1B;
//    SendBuf[1] = (byte)(slave_sel+20);  //10~19
//    SendBuf[2] = TCP_CMD_RD_STATUS;
//    SendBuf[TCP_PACKET - 1] = 0xD;      //Get_Check_sum_data(SendBuf);



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

}
