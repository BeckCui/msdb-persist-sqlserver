package com.dhl.fin.api.common.enums;

import lombok.Getter;

@Getter
public enum TimeUnitEnum {

    DAY("DAY"),
    HOUR("HOUR"),
    MINUTE("MINUTE"),
    SECOND("SECOND");

    private String timeUnit;

    TimeUnitEnum(String timeUnit) {
        this.timeUnit = timeUnit;
    }


}
