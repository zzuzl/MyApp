package cn.zlihj.web;

import cn.zlihj.domain.Staff;
import cn.zlihj.dto.ListResult;
import cn.zlihj.dto.Result;
import cn.zlihj.enums.Source;
import cn.zlihj.service.CompanyService;
import cn.zlihj.service.ProjectService;
import cn.zlihj.service.StaffService;
import cn.zlihj.util.LoginContext;
import cn.zlihj.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("/staff")
@Controller
public class StaffController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StaffService staffService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private ProjectService projectService;

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

    @RequestMapping(value = "/move", method = RequestMethod.POST)
    @ResponseBody
    public Result move(@RequestParam int source, @RequestParam Integer pid,
                       @RequestParam int oldSource, @RequestParam Integer oldPid, @RequestParam Long id) {
        Result result = Result.successResult();
        try {
            Staff staff = staffService.findById(id);
            Assert.notNull(staff, id + ":员工不存在");
            Source of = Source.of(source);
            Assert.notNull(of, id + ":来源错误");
            if (of == Source.COMPANY) {
                Assert.notNull(companyService.findById(pid), "company不存在");
            } else {
                Assert.notNull(projectService.findById(pid), "project不存在");
            }
            staffService.move(source, pid, oldSource, oldPid, id);
        } catch (Exception e) {
            logger.error("移动失败：{}", e.getMessage(), e);
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
            result = staffService.pageList(page, 20, Source.of(source), pid, null);
        } catch (Exception e) {
            logger.error("查询失败：{}", e.getMessage(), e);
            result = ListResult.errorList(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ListResult<Staff> list(@RequestParam("page") Integer page, String key) {
        ListResult<Staff> result = null;
        try {
            result = staffService.pageList(page, 50, null, null, key);
        } catch (Exception e) {
            logger.error("查询失败：{}", e.getMessage(), e);
            result = ListResult.errorList(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/findById", method = RequestMethod.GET)
    @ResponseBody
    public Result findById(@RequestParam("id") Long id) {
        Result result = null;
        try {
            Staff staff = staffService.findById(id);
            staffService.fillStaffInfo(staff);
            result = Result.successResult();
            result.setData(staff);
        } catch (Exception e) {
            logger.error("查询失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public Result save(@RequestBody Staff staff) {
        Result result = null;
        try {
            ParamUtil.checkBean(staff);
            staff.setPassword(ParamUtil.md5(staff.getEmail()));
            staffService.save(staff);
            result = Result.successResult();
        } catch (Exception e) {
            logger.error("保存失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }
}
