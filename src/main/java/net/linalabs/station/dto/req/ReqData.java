package net.linalabs.station.dto.req;

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
public class ReqData {

    private Integer docked;
    private Integer stationid;
    private Integer mobilityid;
    private Integer chargerid;

}
