package net.linalabs.station.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DockingReqDto {

    private Integer stationid;
    private Integer chargerid;
    private Integer docked;
    private Integer mobilityid;

}
