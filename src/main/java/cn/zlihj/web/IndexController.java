package cn.zlihj.web;

import cn.zlihj.domain.*;
import cn.zlihj.dto.ListResult;
import cn.zlihj.dto.Result;
import cn.zlihj.enums.Gender;
import cn.zlihj.enums.Source;
import cn.zlihj.enums.WorkType;
import cn.zlihj.service.CompanyService;
import cn.zlihj.service.ProjectService;
import cn.zlihj.service.StaffService;
import cn.zlihj.util.ExcelUtil;
import cn.zlihj.util.LoginContext;
import cn.zlihj.util.ParamUtil;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
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
    private StaffService staffService;

    @RequestMapping("/checkLogin")
    @ResponseBody
    public Result checkLogin() {
        Result result = Result.successResult();
        Staff staff = LoginContext.currentUser();
        result.setData(staffService.fillStaffInfo(staff));
        return result;
    }

    @RequestMapping("/checkVersion")
    @ResponseBody
    public Result checkVersion(String uuid) {
        Result result = Result.successResult();
        VersionInfo maxVersion = staffService.findMaxVersion();
        result.setData(maxVersion);

        // logger.info("uuid:" + uuid);

        /*if (StringUtils.hasText(uuid) && staffService.findUuid(uuid) != null) {

        }*/
        return result;
    }

    @RequestMapping("/reportUuid")
    @ResponseBody
    public Result reportUuid(@RequestParam String uuid) {
        Result result = Result.successResult();
        try {
            staffService.insertIosUuid(uuid);
        } catch (DataIntegrityViolationException e) {
            logger.error("uuid已存在：" + uuid);
        }

        return result;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public ListResult<SearchVo> search(@RequestParam("key") String key) {
        ListResult<SearchVo> result = null;
        try {
            List<SearchVo> list = staffService.searchAll(key);
            result = ListResult.successList(list);
        } catch (Exception e) {
            logger.error("查询失败：{}", e.getMessage(), e);
            result = ListResult.errorList(e.getMessage());
        }
        return result;
    }
}
