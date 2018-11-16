package cn.zlihj.web;

import cn.zlihj.domain.Staff;
import cn.zlihj.dto.Result;
import cn.zlihj.service.StaffService;
import cn.zlihj.util.LoginContext;
import cn.zlihj.util.ParamUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;

public class UserInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StaffService staffService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean success = true;
        String token = request.getHeader("token");
        if (token == null) {
            success = false;
        }

        String user = null;
        try {
            user = ParamUtil.parseToken(token);
        } catch (Exception e) {
            logger.error("token error:{}", token);
            success = false;
        }
        Staff staff = staffService.findByEmail(user);

        if (staff == null) {
            success = false;
        }

        if (success) {
            LoginContext.setUser(staff);
            response.addHeader("token", ParamUtil.createToken(staff.getEmail()));
        } else {
            // response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            // response.getWriter().write(new ObjectMapper().writeValueAsString(Result.errorResult("未登录或登录凭证错误，请重新登录")));
            // return false;
        }

        return true;
    }

}
