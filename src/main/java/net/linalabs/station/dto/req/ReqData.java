package net.linalabs.station.dto.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReqData {

    private Integer docked;

    @NotNull(message = "stationid를 제대로 입력해주세요.")
    private Integer stationid;

    @NotNull(message = "mobilityid를 제대로 입력해주세요.")
    private Integer mobilityid;

    @NotNull(message = "chargerid를 제대로 입력해주세요.")
    private Integer chargerid;

}
