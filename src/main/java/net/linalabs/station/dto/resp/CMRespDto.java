package net.linalabs.station.dto.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.linalabs.station.dto.Opcode;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class CMRespDto {

    private Opcode opcode; //추가
    private RespData data;
}

