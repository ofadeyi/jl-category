package com.example.jlcategory.product;

import java.util.EnumSet;
import java.util.Optional;

public enum LabelType {
    WasNow("ShowWasNow"),
    WasThenNow("ShowWasThenNow"),
    PercentageDiscount("ShowPercDscount");

    private String labelType;

    LabelType(String label) {
        this.labelType = label;
    }

    public String label() {
        return labelType;
    }

    public static LabelType fromLabel(String labelType) {
        return Optional.ofNullable(labelType)
                .map(value -> EnumSet.allOf(LabelType.class)
                        .stream()
                        .filter(page -> page.labelType.equals(value))
                        .findFirst()
                        .orElse(WasNow))
                .orElse(WasNow);
    }
}
