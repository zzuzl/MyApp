package cn.zlihj.enums;

import cn.zlihj.mybatis.Convertable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Convertable
public enum StorageType {
    // 部门动态
    BM(1),
    // 制度文件
    ZD(2),
    // 制度-陈飞
    SYSTEM(3),
    // 资料-陈飞
    DATA(4)
    ;

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
