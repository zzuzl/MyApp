package cn.zlihj.enums;

import cn.zlihj.mybatis.Convertable;

@Convertable
public enum Gender {
    MEN(1, "男"),
    WOMEN(2, "女");

    private Integer value;
    private String text;

    private Gender(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public Integer value() {
        return value;
    }

    public static Gender of(Integer value) {
        for (Gender gender : values()) {
            if (gender.value.equals(value)) {
                return gender;
            }
        }
        return null;
    }
}
