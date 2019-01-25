package cn.zlihj.enums;

import cn.zlihj.mybatis.Convertable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Convertable
public enum StorageType {
    BM(1),
    ZD(2);

    private Integer value;

    private StorageType(Integer value) {
        this.value = value;
    }

    @JsonValue
    public Integer value() {
        return value;
    }

    @JsonCreator
    public static StorageType of(Integer value) {
        for (StorageType storageType : values()) {
            if (storageType.value.equals(value)) {
                return storageType;
            }
        }
        return null;
    }
}
