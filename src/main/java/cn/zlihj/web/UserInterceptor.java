package cn.zlihj.web;

import cn.zlihj.domain.Staff;
import cn.zlihj.service.StaffService;
import cn.zlihj.util.LoginContext;
import cn.zlihj.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StaffService staffService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (token == null) {
            // todo
        }

        String user = null;
        try {
            user = ParamUtil.parseToken(token);
        } catch (Exception e) {
            logger.error("token error:{}", token);
            return false;
        }
        Staff staff = staffService.findByEmail(user);

        if (staff == null) {
            throw new RuntimeException("未登录");
        }
        LoginContext.setUser(staff);
        return true;
    }
}
