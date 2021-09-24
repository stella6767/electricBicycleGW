package net.linalabs.station.dto;


import lombok.Getter;

@Getter
public enum Opcode {

    DOCKING("docking"), RENTAL("rental"), RETURN("return"), INIT("");

    String code;

    Opcode(String code) {
        this.code = code;
    }
}
