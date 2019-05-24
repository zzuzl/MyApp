package cn.zlihj.enums;

import cn.zlihj.mybatis.Convertable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Convertable
public enum SubjectType {

    RADIO(1, "单选"),
    MULTI(2, "多选");

    private Integer value;
    private String text;

    private SubjectType(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    @JsonValue
    public Integer value() {
        return value;
    }

    @JsonCreator
    public static SubjectType of(Integer value) {
        for (SubjectType gender : values()) {
            if (gender.value.equals(value)) {
                return gender;
            }
        }
        return null;
    }

    public static SubjectType ofText(String text) {
        for (SubjectType gender : values()) {
            if (gender.text.equals(text)) {
                return gender;
            }
        }
        return null;
    }
}
