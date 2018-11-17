package cn.zlihj.web;

import cn.zlihj.dao.WorkTypeDao;
import cn.zlihj.domain.Company;
import cn.zlihj.domain.Project;
import cn.zlihj.domain.Staff;
import cn.zlihj.domain.WorkType;
import cn.zlihj.dto.Result;
import cn.zlihj.service.CompanyService;
import cn.zlihj.service.ProjectService;
import cn.zlihj.util.LoginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/")
public class IndexController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private CompanyService companyService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private WorkTypeDao workTypeDao;

    @RequestMapping("/index")
    public String index(Model model) {
        return "index";
    }

    @RequestMapping("/checkLogin")
    @ResponseBody
    public Result checkLogin() {
        Result result = Result.successResult();
        Staff staff = LoginContext.currentUser();
        List<WorkType> workTypes = workTypeDao.list();
        for (WorkType workType : workTypes) {
            if (workType.getId().equals(staff.getType().getId())) {
                staff.setType(workType);
                break;
            }
        }

        switch (staff.getSource()) {
            case COMPANY:
                Company company = companyService.findById(staff.getPid());
                if (company != null) {
                    staff.setPname(company.getName());
                }
                break;
            case PROJECT:
                Project project = projectService.findById(staff.getPid());
                if (project != null) {
                    staff.setPname(project.getName());
                }
                break;
            default:
                break;
        }

        result.setData(staff);
        return result;
    }
}
