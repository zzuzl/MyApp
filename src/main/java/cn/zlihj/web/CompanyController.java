package cn.zlihj.web;

import cn.zlihj.domain.Company;
import cn.zlihj.dto.ListResult;
import cn.zlihj.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/company")
@Controller
public class CompanyController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CompanyService companyService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ListResult<Company> list(Integer pid) {
        ListResult<Company> result = null;

        try {
            pid = pid == null ? 0 : pid;
            List<Company> list = companyService.list(pid);
            result = ListResult.successList(list);
        } catch (Exception e) {
            logger.error("查询失败：{}", e.getMessage(), e);
            result = ListResult.errorList(e.getMessage());
        }
        return result;
    }
}
