package cn.zlihj.web;

import cn.zlihj.domain.Company;
import cn.zlihj.domain.Project;
import cn.zlihj.domain.SearchVo;
import cn.zlihj.domain.Staff;
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
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

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

    @RequestMapping("/index")
    public String index(Model model) {
        return "index";
    }

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
    public Result checkVersion(@RequestParam("version") String version) {
        Result result = Result.successResult();
        staffService.listVersion();
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

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public String uploadFile( @RequestParam("file") MultipartFile file, Model model) {
        Result result = null;
        try {
            if (!file.isEmpty()) {
                if (!FilenameUtils.isExtension(file.getOriginalFilename(), "xls")) {
                    throw new RuntimeException("仅支持.xls文件");
                }

                String rootPath = "/tmp";
                File dir = new File(rootPath + File.separator + "tmpFiles");
                if (!dir.exists()) {
                    Assert.isTrue(dir.mkdirs(), "文件夹创建失败");
                }
                File serverFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
                file.transferTo(serverFile);
                logger.info("You successfully uploaded file=" +  file.getOriginalFilename());
                String[][] strings = ExcelUtil.readFromFile(serverFile, 1, 1000);
                for (int i=1;i<strings.length;i++) {
                    String[] string = strings[i];
                    Staff staff = new Staff();
                    staff.setName(string[0]);
                    staff.setType(WorkType.ofText(string[1]));
                    staff.setGender(Gender.ofText(string[2]));
                    staff.setSchool(string[3]);
                    staff.setMajor(string[4]);
                    staff.setPhone(string[5]);
                    staff.setQq(string[6]);
                    staff.setWx(string[7]);
                    staff.setGxtAccount(string[8]);
                    staff.setEmail(string[9]);
                    staff.setWorkAddress(string[10]);
                    staff.setPassword(ParamUtil.md5(string[11]));
                    staff.setSource(Source.ofText(string[12]));

                    if (staff.getSource() == null) {
                        throw new RuntimeException("来源错误，行号:" + i);
                    }
                    Assert.hasText(staff.getEmail(), "email不能为空,行号:" + i);
                    Assert.hasText(staff.getName(), "姓名不能为空,行号:" + i);
                    Assert.notNull(staff.getGender(), "性别不能为空,行号:" + i);

                    String pname = string[13];
                    if (staff.getSource() == Source.COMPANY) {
                        Company company = companyService.findByName(pname);
                        Assert.notNull(company, "对应部门不存在,行号：" + i);
                        staff.setPid(company.getId());
                    } else if (staff.getSource() == Source.PROJECT) {
                        Project project = projectService.findByName(pname);
                        Assert.notNull(project, "对应项目不存在,行号：" + i);
                        staff.setPid(project.getPid());
                    }

                    try {
                        staffService.addStaff(staff);
                    } catch (DataIntegrityViolationException e) {
                        logger.error("已存在：" + Arrays.toString(string));
                        continue;
                    }

                    logger.info(Arrays.toString(string));
                }

                serverFile.deleteOnExit();
            } else {
                logger.error("You failed to upload " +  file.getOriginalFilename() + " because the file was empty.");
            }
        } catch (Exception e) {
            logger.error("上传失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }

        return "index";
    }
}
