package cn.zlihj.enums;

import cn.zlihj.mybatis.Convertable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Date;

@Convertable
public enum WorkType {
    ZG(1, "总工"),
    JL(2, "技术质量部经理"),
    ZJ(3, "质量总监"),
    JS(4, "技术员"),
    CL(5, "测量员"),
    ZL(6, "资料员"),
    SY(7, "试验员"),
    AZ(8, "安装员");

    private Integer value;
    private String text;

    private WorkType(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public Integer value() {
        return value;
    }

    @JsonValue
    public String getText() {
        return text;
    }

    @JsonCreator
    public static WorkType of(Integer value) {
        for (WorkType workType : values()) {
            if (workType.value.equals(value)) {
                return workType;
            }
        }
        return null;
    }

    public static WorkType ofText(String text) {
        for (WorkType workType : values()) {
            if (workType.text.equals(text)) {
                return workType;
            }
        }
        return null;
    }
}
