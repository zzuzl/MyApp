package cn.zlihj.web;

import cn.zlihj.domain.Resume;
import cn.zlihj.dto.ListResult;
import cn.zlihj.dto.Result;
import cn.zlihj.service.ResumeService;
import cn.zlihj.service.StaffService;
import cn.zlihj.util.LoginContext;
import cn.zlihj.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/resume")
@Controller
public class ResumeController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ResumeService resumeService;
    @Autowired
    private StaffService staffService;

    @RequestMapping(value = "/findByStaff", method = RequestMethod.GET)
    @ResponseBody
    public ListResult<Resume> findByStaff(Long staffId) {
        ListResult<Resume> result = null;
        try {
            Assert.notNull(staffId, "参数错误");
            if (!staffId.equals(LoginContext.currentUser().getId())
                    && !staffService.hasPermission(Authorization.STAFF_LIST, LoginContext.currentUser().getEmail())) {
                throw new RuntimeException("权限不足");
            }

            List<Resume> resumeList = resumeService.listByStaff(staffId);
            result = ListResult.successList(resumeList);
        } catch (Exception e) {
            logger.error("查询简历失败：{}", e.getMessage(), e);
            result = ListResult.errorList(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public Result save(@RequestBody Resume resume) {
        Result result = null;
        try {
            ParamUtil.checkBean(resume);
            resumeService.save(resume);
            result = Result.successResult();
        } catch (Exception e) {
            logger.error("保存简历失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/del", method = RequestMethod.POST)
    @ResponseBody
    @Authorization(key = Authorization.RESUME_DEL)
    public Result del(@RequestParam Long id) {
        Result result = null;
        try {
            Assert.notNull(id, "参数错误");
            resumeService.del(id);
            result = Result.successResult();
        } catch (Exception e) {
            logger.error("删除失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }

}
