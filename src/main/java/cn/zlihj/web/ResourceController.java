package cn.zlihj.web;

import cn.zlihj.domain.Company;
import cn.zlihj.domain.Project;
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
import cn.zlihj.util.ParamUtil;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/resource")
public class ResourceController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private CompanyService companyService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private Producer captchaProducer;
    @Autowired
    private JavaMailSenderImpl javaMailSender;
    @Autowired
    private VelocityEngine velocityEngine;

    private Cache<String, String> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @RequestMapping(value = "/spe", method = RequestMethod.POST)
    @ResponseBody
    public Result sendPasswordEmail(String email, String code, HttpServletRequest request) {
        Result result = Result.successResult();
        try {
            Assert.hasText(email, "email为空");
            Assert.hasText(code, "验证码为空");

            boolean success = false;
            String header = request.getHeader(Constants.KAPTCHA_SESSION_KEY);

            if (header != null) {
                String value = cache.getIfPresent(header);
                if (code.equalsIgnoreCase(value)) {
                    Assert.notNull(staffService.findByEmail(email), "该邮箱对应的用户不存在");
                    success = true;
                    // send
                    String token = ParamUtil.createToken(email, 20);
                    MimeMessagePreparator preparator = new MimeMessagePreparator() {
                        public void prepare(MimeMessage mimeMessage) throws Exception {
                            MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                            message.setTo(email);
                            message.setFrom("zlihj_no_reply@qq.com");
                            Map<String, Object> model = new HashMap<>();
                            model.put("url", "http://www.zlihj.cn/rest/vt?t=" + token + "&e=" + email);
                            String text = VelocityEngineUtils.mergeTemplateIntoString(
                                    velocityEngine, "reset_password.vm", model);
                            message.setText(text, true);
                        }
                    };
                    javaMailSender.send(preparator);
                }
            }

            Assert.isTrue(success, "验证码错误");
        } catch (Exception e) {
            logger.error("sendPasswordEmail error：", e);
            result = Result.errorResult(e.getMessage());
        }

        return result;
    }

    @RequestMapping("/vt")
    public String vt(String t, String e, String pwd, Model model) {
        model.addAttribute("t", t);
        model.addAttribute("e", e);
        model.addAttribute("success", false);
        model.addAttribute("msg", "发生错误，请重新操作");
        try {
            if (t != null && e != null) {
                String s = ParamUtil.parseToken(t);
                if (e.equals(s)) {
                    Assert.notNull(staffService.findByEmail(e), "用户不存在");
                    model.addAttribute("success", true);
                    model.addAttribute("msg", "");

                    if (pwd != null) {
                        Assert.isTrue(pwd.trim().length() >= 6 && pwd.length() <= 30, "密码长度[6-30]");
                        staffService.resetPwd(e, pwd.trim());
                    }
                }
            }
        } catch (Exception ex) {
            model.addAttribute("success", false);
            model.addAttribute("msg", ex.getMessage());
        }

        return "vt";
    }

    @RequestMapping("/captcha-image")
    public ModelAndView captchaImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");

        response.setContentType("image/jpeg");
        String capText = captchaProducer.createText();

        String randomUuid = ParamUtil.randomUuid();
        Cookie cookie = new Cookie(Constants.KAPTCHA_SESSION_KEY, randomUuid);
        cookie.setDomain(request.getServerName());
        cookie.setPath("/");
        response.addCookie(cookie);
        cache.put(randomUuid, capText);

        BufferedImage bi = captchaProducer.createImage(capText);
        ServletOutputStream out = response.getOutputStream();

        ImageIO.write(bi, "jpg", out);
        try {
            out.flush();
        } finally {
            out.close();
        }
        return null;
    }

    @RequestMapping("/mobileConfig")
    public ModelAndView mobileConfig(HttpServletResponse response) {
        response.setContentType("application/x-apple-aspen-config");

        try {
            File file = ResourceUtils.getFile("classpath:mobileconfig.xml");
            String xml = FileUtils.readFileToString(file, "utf-8");

            response.setCharacterEncoding("utf-8");
            PrintWriter out = response.getWriter();
            out.println(xml);
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.error("mobileConfig", e);
        }

        return null;
    }

    @RequestMapping("/receiveIosData")
    public void receiveIosData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            InputStream is = request.getInputStream();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(is);
            NodeList dict = document.getElementsByTagName("dict");
            Node item = dict.item(0);

            NodeList childNodes = item.getChildNodes();

            for (int i=0;i<childNodes.getLength();i++) {
                Node node = childNodes.item(i);
                logger.info(node.getNodeName() + " : " + node.getNodeValue());
                if (node.getNodeValue().equalsIgnoreCase("UDID")) {
                    String udid = node.getNextSibling().getNodeValue();
                    logger.info("UDID:" + udid);

                    // 301之后iOS设备会自动打开safari浏览器
                    response.setStatus(301);
                    response.setHeader("Location", "https://www.zlihj.cn/rest/resource/reportUuid?uuid=" + udid);

                    break;
                }
            }
        } catch (Exception e) {
            logger.error("receiveIosData", e);
        }
    }

    @RequestMapping("/reportUuid")
    public void reportUuid(@RequestParam String uuid) {
        try {
            staffService.insertIosUuid(uuid);
        } catch (Exception e) {
            logger.error("reportUuid：", e);
        }
    }

    @RequestMapping(value = "/downloadTpl", method = RequestMethod.GET)
    public void downloadTpl(HttpServletResponse response) {
        List<Company> companies = companyService.list(0);
        List<Project> projects = projectService.list(0);

        String[] arr = new String[companies.size() + projects.size()];
        int i=0;
        for (Company company : companies) {
            arr[i] = company.getName();
            i ++;
        }

        for (Project project : projects) {
            arr[i] = project.getName();
            i ++;
        }

        ExcelUtil.downloadTemplate(response, "导入模板.xlsx", arr);
    }

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        Result result = null;
        try {
            if (!file.isEmpty()) {
                if (!FilenameUtils.isExtension(file.getOriginalFilename(), "xlsx")
                        || !FilenameUtils.isExtension(file.getOriginalFilename(), "xls")) {
                    throw new RuntimeException("仅支持.xlsx和.xls文件");
                }

                String rootPath = "/tmp";
                File dir = new File(rootPath + File.separator + "tmpFiles");
                if (!dir.exists()) {
                    Assert.isTrue(dir.mkdirs(), "文件夹创建失败");
                }
                File serverFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
                file.transferTo(serverFile);
                logger.info("You successfully uploaded file=" +  file.getOriginalFilename());
                List<String[]> strings = ExcelUtil.readFromFile(serverFile, 14, 1000);
                for (int i=1;i<strings.size();i++) {
                    String[] string = strings.get(i);
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
                    staff.setPassword(ParamUtil.md5(staff.getEmail()));
                    staff.setSource(Source.ofText(string[11]));

                    if (staff.getSource() == null) {
                        throw new RuntimeException("来源错误，行号:" + i);
                    }
                    Assert.hasText(staff.getEmail(), "email不能为空,行号:" + i);
                    Assert.hasText(staff.getName(), "姓名不能为空,行号:" + i);
                    Assert.notNull(staff.getGender(), "性别不能为空,行号:" + i);
                    Assert.notNull(staff.getType(), "类型不能为空,行号:" + i);

                    String pname = string[12];
                    if (staff.getSource() == Source.COMPANY) {
                        Company company = companyService.findByName(pname);
                        Assert.notNull(company, "对应部门不存在,行号：" + i);
                        staff.setPid(company.getId());
                    } else if (staff.getSource() == Source.PROJECT) {
                        Project project = projectService.findByName(pname);
                        Assert.notNull(project, "对应项目不存在,行号：" + i);
                        staff.setPid(project.getId());
                    }

                    try {
                        staffService.addStaff(staff);
                    } catch (DataIntegrityViolationException e) {
                        logger.error("已存在：" + Arrays.toString(string));
                    }
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

    @RequestMapping("/index")
    public String index(Model model) {
        return "index";
    }

    @RequestMapping(value = "/workTypes", method = RequestMethod.GET)
    @ResponseBody
    public ListResult workTypes() {
        ListResult result = null;
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            for (WorkType workType : WorkType.values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", workType.getText());
                map.put("type", workType.value());
                list.add(map);
            }
            result = ListResult.successList(list);
        } catch (Exception e) {
            logger.error("workTypes error：", e);
            result = ListResult.errorList(e.getMessage());
        }

        return result;
    }
}
