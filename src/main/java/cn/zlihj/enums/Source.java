package cn.zlihj.enums;

import cn.zlihj.mybatis.Convertable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Convertable
public enum Source {
    COMPANY(0, "总部"),
    PROJECT(1, "项目簇");

    private Integer value;
    private String text;

    private Source(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    @JsonValue
    public Integer value() {
        return value;
    }

    @JsonCreator
    public static Source of(Integer value) {
        for (Source source : values()) {
            if (source.value.equals(value)) {
                return source;
            }
        }
        return null;
    }
}
