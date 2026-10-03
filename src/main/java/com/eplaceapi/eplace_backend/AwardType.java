package com.eplaceapi.eplace_backend;

public enum AwardType {
    FIRST_ONLY("1st only"),
    FIRST_AND_SECOND("1st and 2nd"),
    FIRST_SECOND_AND_THIRD("1st, 2nd and 3rd");

    private final String label;

    AwardType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }


    public static AwardType fromLabel(String label) {
        for (AwardType type : values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown award distribution option: " + label);
    }
}