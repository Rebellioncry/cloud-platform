package org.lyz.common.mybatis.enums;

public enum DataScopeType {
    ALL(1),
    SELF(5);

    private final int value;

    DataScopeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DataScopeType fromValue(int value) {
        for (DataScopeType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return ALL;
    }
}
