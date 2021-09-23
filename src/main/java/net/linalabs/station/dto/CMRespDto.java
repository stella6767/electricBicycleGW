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
public class CMRespDto {

    private Integer resultCode;
    private String resultMsg;
    private Integer stationId;
    private Integer chargerId;
    private Integer mobilityId;

}

