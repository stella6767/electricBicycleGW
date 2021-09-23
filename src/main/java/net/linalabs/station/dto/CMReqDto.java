package net.linalabs.station.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CMReqDto {

    private String opcode; //대여인지, 반납인지 결정
    private Integer docked;
    private Integer stationid;
    private Integer mobilityid;
    private Integer chargerid;

}
