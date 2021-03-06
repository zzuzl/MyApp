package cn.zlihj.web;

import java.lang.annotation.*;

@Documented
@Target({
        ElementType.METHOD, ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorization {
    String STAFF_MOVE = "STAFF_MOVE";
    String STAFF_DEL = "STAFF_DEL";
    String STAFF_LIST = "STAFF_LIST";
    String COMPANY_SAVE = "COMPANY_SAVE";
    String PROJECT_SAVE = "PROJECT_SAVE";
    String RESUME_DEL = "RESUME_DEL";
    String PATENT_DEL = "PATENT_DEL";
    String STORAGE_OPERATE = "STORAGE_OPERATE";
    String key();
}
