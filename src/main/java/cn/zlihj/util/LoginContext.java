package cn.zlihj.util;

import cn.zlihj.domain.Staff;

public class LoginContext {
    private static ThreadLocal<Staff> USER = new ThreadLocal<>();

    public static Staff currentUser() {
        return USER.get();
    }

    public static void setUser(Staff user) {
        USER.set(user);
    }
}
