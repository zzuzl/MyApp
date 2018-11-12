package cn.zlihj.web;

import cn.zlihj.domain.Project;
import cn.zlihj.dto.ListResult;
import cn.zlihj.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
