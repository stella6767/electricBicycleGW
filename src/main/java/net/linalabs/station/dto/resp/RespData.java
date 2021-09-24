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

    private Integer resultCode;
    private String resultMsg;
    private Integer stationId;
    private Integer chargerId;
    private Integer mobilityId;
    private Integer slotno;
    private Integer docked;
}
