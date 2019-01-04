package cn.zlihj.web;

import cn.zlihj.domain.Company;
import cn.zlihj.domain.Project;
import cn.zlihj.dto.ListResult;
import cn.zlihj.dto.Result;
import cn.zlihj.service.ProjectService;
import cn.zlihj.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/project")
@Controller
public class ProjectController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProjectService projectService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ListResult<Project> list(Integer pid) {
        ListResult<Project> result = null;

        try {
            pid = pid == null ? 0 : pid;
            List<Project> list = projectService.list(pid);
            result = ListResult.successList(list);
        } catch (Exception e) {
            logger.error("查询失败：{}", e.getMessage(), e);
            result = ListResult.errorList(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/findById", method = RequestMethod.GET)
    @ResponseBody
    public Result findById(@RequestParam("id") Integer id) {
        Result result = null;
        try {
            Project project = projectService.findById(id);
            result = Result.successResult();
            result.setData(project);
        } catch (Exception e) {
            logger.error("查询失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    @Authorization(key = Authorization.PROJECT_SAVE)
    public Result save(@RequestBody Project project) {
        Result result = null;
        try {
            ParamUtil.checkBean(project);
            projectService.save(project);
            result = Result.successResult();
        } catch (Exception e) {
            logger.error("保存失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/del", method = RequestMethod.POST)
    @ResponseBody
    @Authorization(key = Authorization.PROJECT_SAVE)
    public Result del(@RequestParam Integer id) {
        Result result = null;
        try {
            Project project = projectService.findById(id);
            Assert.notNull(project, "该项目不存在");

            int staffCount = projectService.staffCount(id);
            Assert.isTrue(staffCount <= 0, "该项目下还有" + staffCount + "个人，请移动人员后再删除！");


            projectService.del(id);
            result = Result.successResult();
        } catch (Exception e) {
            logger.error("删除失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }
}
