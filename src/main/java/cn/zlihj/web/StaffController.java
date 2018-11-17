package cn.zlihj.web;

import cn.zlihj.domain.Staff;
import cn.zlihj.dto.ListResult;
import cn.zlihj.dto.Result;
import cn.zlihj.enums.Source;
import cn.zlihj.service.StaffService;
import cn.zlihj.util.LoginContext;
import cn.zlihj.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("/staff")
@Controller
public class StaffController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StaffService staffService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Result login(@RequestParam(value = "user") String user,
                        @RequestParam(value = "password") String password,
                        HttpServletResponse response) {
        Result result = Result.successResult();
        try {
            Staff staff = staffService.login(user, password);
            LoginContext.setUser(staff);
            staff.setPassword(null);
            result.setData(staffService.fillStaffInfo(staff));
            response.addHeader("token", ParamUtil.createToken(user));
        } catch (Exception e) {
            logger.error("登录失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    @ResponseBody
    public Result changePassword(@RequestParam(value = "oldPassword") String oldPassword,
                        @RequestParam(value = "newPassword") String newPassword) {
        Result result = Result.successResult();
        try {
            staffService.changePassword(LoginContext.currentUser().getEmail(), oldPassword, newPassword);
        } catch (Exception e) {
            logger.error("修改失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/updateInfo", method = RequestMethod.POST)
    @ResponseBody
    public Result updateInfo(@RequestBody Staff staff) {
        Result result = Result.successResult();
        try {
            staffService.updateInfo(staff);
        } catch (Exception e) {
            logger.error("修改失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/findByPid", method = RequestMethod.GET)
    @ResponseBody
    public ListResult<Staff> findByPid(@RequestParam("source") Integer source,
                                       Integer pid,
                                       @RequestParam("page") Integer page) {
        ListResult<Staff> result = null;
        try {
            result = staffService.pageList(page, 20, Source.of(source), pid);
        } catch (Exception e) {
            logger.error("查询失败：{}", e.getMessage(), e);
            result = ListResult.errorList(e.getMessage());
        }
        return result;
    }
}
