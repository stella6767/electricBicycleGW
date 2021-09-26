package net.linalabs.station.dto.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RespData {

    private Integer result_code; //0은 성공, 1은 실패
    private String result_message;
    private Integer stationid;
    private Integer chargerid;
    private Integer mobilityid;
    private Integer slotno;
    private Integer docked;
    private Integer battery;
}
