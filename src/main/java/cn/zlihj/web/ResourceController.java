package cn.zlihj.web;

import cn.zlihj.domain.*;
import cn.zlihj.dto.ListResult;
import cn.zlihj.dto.Result;
import cn.zlihj.enums.Gender;
import cn.zlihj.enums.Source;
import cn.zlihj.enums.SubjectType;
import cn.zlihj.enums.WorkType;
import cn.zlihj.service.CompanyService;
import cn.zlihj.service.ProjectService;
import cn.zlihj.service.StaffService;
import cn.zlihj.service.SubjectService;
import cn.zlihj.util.ExcelUtil;
import cn.zlihj.util.ParamUtil;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.velocity.app.VelocityEngine;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import javax.imageio.ImageIO;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    @Autowired
    private SubjectService subjectService;

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
            File file = ResourceUtils.getFile("classpath:udid.mobileconfig");
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
        String udid = "";
        try {
            InputStream is = request.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            StringBuilder sb = new StringBuilder();

            String buffer = null;
            while ((buffer = br.readLine()) != null) {
                sb.append(buffer);
            }
            String content = sb.toString().substring(sb.toString().indexOf("<?xml"), sb.toString().indexOf("</plist>")+8);
            logger.info("receiveIosData:" + content);

            Document document = DocumentHelper.parseText(content);

            //获取根节点对象
            Element rootElement = document.getRootElement();
            Iterator iterator = rootElement.element("dict").elementIterator();

            while (iterator.hasNext()) {
                Element next = (Element) iterator.next();
                if (next.getStringValue().equalsIgnoreCase("UDID")) {
                    udid = ((Element) iterator.next()).getStringValue();
                    logger.info("udid:" + udid);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("receiveIosData", e);
        }
        // 301之后iOS设备会自动打开safari浏览器
        response.setStatus(301);
        response.setHeader("Location", "https://www.zlihj.cn/rest/resource/reportUuid?uuid=" + udid);
    }

    @RequestMapping("/reportUuid")
    public void reportUuid(@RequestParam String uuid) {
        try {
            if (StringUtils.hasText(uuid)) {
                staffService.insertIosUuid(uuid);
            }
        } catch (Exception e) {
            logger.error("reportUuid：", e);
        }
    }

    @RequestMapping(value = "/uploadPatent", method = RequestMethod.POST)
    @ResponseBody
    public Result uploadPatent(@RequestParam("file") MultipartFile file) {
        Result result = null;
        try {
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            Assert.isTrue(isPdf(extension), "文件格式错误");

            logger.info(file.getOriginalFilename() + ",size:" + file.getSize());

            Assert.isTrue(file.getSize() < 5120000, "文件超过5M");

            String rootPath = "/tmp";
            File dir = new File(rootPath + File.separator + "tmpPatent");
            if (!dir.exists()) {
                Assert.isTrue(dir.mkdirs(), "文件夹创建失败");
            }
            File serverFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
            file.transferTo(serverFile);

            // 上传到COS
            COSCredentials cred = new BasicCOSCredentials("AKIDIQNCnEdaYTGD7OSUIVXO6e4JQNchpMzs", "uhKwMbheR3BtZ4NZiieS7jAPVWM1qbDg");
            ClientConfig clientConfig = new ClientConfig(new Region("ap-beijing"));
            COSClient cosclient = new COSClient(cred, clientConfig);

            String key = "patent/" + (System.currentTimeMillis() / 1000) + "-" + file.getOriginalFilename();
            PutObjectRequest putObjectRequest = new PutObjectRequest("zlihj-zpk-1251746773", key, serverFile);
            cosclient.putObject(putObjectRequest);
            cosclient.shutdown();

            serverFile.deleteOnExit();
            String url = "https://zlihj-zpk-1251746773.cos.ap-beijing.myqcloud.com/" + key;
            result = Result.successResult();
            result.setMsg(url);
        } catch (Exception e) {
            logger.error("uploadFile error:", e);
            result = Result.errorResult(e.getMessage());
        }

        return result;
    }

    @RequestMapping(value = "/uploadStorage", method = RequestMethod.POST)
    @ResponseBody
    public Result uploadStorage(@RequestParam("file") MultipartFile file) {
        Result result = null;
        try {
            logger.info(file.getOriginalFilename() + ",size:" + file.getSize());

            Assert.isTrue(file.getSize() < 5120000, "文件超过5M");

            String rootPath = "/tmp";
            File dir = new File(rootPath + File.separator + "tmpStorage");
            if (!dir.exists()) {
                Assert.isTrue(dir.mkdirs(), "文件夹创建失败");
            }
            File serverFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
            file.transferTo(serverFile);

            // 上传到COS
            COSCredentials cred = new BasicCOSCredentials("AKIDIQNCnEdaYTGD7OSUIVXO6e4JQNchpMzs", "uhKwMbheR3BtZ4NZiieS7jAPVWM1qbDg");
            ClientConfig clientConfig = new ClientConfig(new Region("ap-beijing"));
            COSClient cosclient = new COSClient(cred, clientConfig);

            String key = "storage/" + (System.currentTimeMillis() / 1000) + "-" + file.getOriginalFilename();
            PutObjectRequest putObjectRequest = new PutObjectRequest("zlihj-zpk-1251746773", key, serverFile);
            cosclient.putObject(putObjectRequest);
            cosclient.shutdown();

            serverFile.deleteOnExit();
            String url = "https://zlihj-zpk-1251746773.cos.ap-beijing.myqcloud.com/" + key;
            result = Result.successResult();
            result.setMsg(url);
        } catch (Exception e) {
            logger.error("uploadFile error:", e);
            result = Result.errorResult(e.getMessage());
        }

        return result;
    }

    private boolean isPdf(String extension) {
        return "pdf".equalsIgnoreCase(extension.toLowerCase());
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
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        Map<String, Object> map = new HashMap<>();
        Result result = null;
        int count = 0;
        int i=1;
        try {
            if (!file.isEmpty()) {
                if (!FilenameUtils.isExtension(file.getOriginalFilename(), "xlsx")
                        && !FilenameUtils.isExtension(file.getOriginalFilename(), "xls")) {
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

                List<Staff> staffList = Lists.newArrayList();

                for (i=1;i<strings.size();i++) {
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
                    ParamUtil.checkBean(staff);

                    staffList.add(staff);
                }

                for (Staff staff : staffList) {
                    try {
                        staffService.addStaff(staff);
                    } catch (DataIntegrityViolationException e) {
                        logger.error("已存在：" + staff.getEmail());
                        continue;
                    }
                    count ++;
                }

                serverFile.deleteOnExit();
            } else {
                logger.error("You failed to upload " +  file.getOriginalFilename() + " because the file was empty.");
            }
            result = Result.successResult();
            result.setMsg("上传成功：" + count + "条");
        } catch (Exception e) {
            logger.error("行号：" + i + "上传失败：{}", e.getMessage(), e);
            result = Result.errorResult("行号：" + i + ",错误：" + e.getMessage());
        }

        convertToMap(result, map);
        return map;
    }

    @RequestMapping(value = "/uploadSubject", method = RequestMethod.POST)
    @ResponseBody
    public Result uploadSubject(@RequestParam("file") MultipartFile file, Model model) {
        Result result = null;
        try {
            if (!file.isEmpty()) {
                if (!FilenameUtils.isExtension(file.getOriginalFilename(), "xlsx")
                        && !FilenameUtils.isExtension(file.getOriginalFilename(), "xls")) {
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
                List<Subject> subjects = parseExcel(serverFile);

                serverFile.deleteOnExit();
                if (subjects.size() > 0) {
                    // 上传到COS
                    COSCredentials cred = new BasicCOSCredentials("AKIDIQNCnEdaYTGD7OSUIVXO6e4JQNchpMzs", "uhKwMbheR3BtZ4NZiieS7jAPVWM1qbDg");
                    ClientConfig clientConfig = new ClientConfig(new Region("ap-beijing"));
                    COSClient cosclient = new COSClient(cred, clientConfig);

                    String key = "subject/" + (System.currentTimeMillis() / 1000) + "-" + file.getOriginalFilename();
                    PutObjectRequest putObjectRequest = new PutObjectRequest("zlihj-zpk-1251746773", key, serverFile);
                    cosclient.putObject(putObjectRequest);
                    cosclient.shutdown();

                    serverFile.deleteOnExit();
                    // 更新URL
                    String url = "https://zlihj-zpk-1251746773.cos.ap-beijing.myqcloud.com/" + key;
                    Exam exam = subjects.get(0).getExam();
                    exam.setUrl(url);
                    subjectService.saveExam(exam, subjects);
                }
            } else {
                logger.error("You failed to upload " +  file.getOriginalFilename() + " because the file was empty.");
            }
            result = Result.successResult();
            result.setMsg("");
        } catch (Exception e) {
            logger.error("上传失败：{}", e.getMessage(), e);
            result = Result.errorResult("错误：" + e.getMessage());
        }

        return result;
    }

    private List<Subject> parseExcel(File file) throws IOException {
        List<String[]> list = ExcelUtil.readFromFile(file, 20, 1000);

        List<Subject> subjects = new ArrayList<>(list.size() - 1);
        for (int j=0;j<list.size() - 1;j++) {
            subjects.add(new Subject());
        }

        String[] header = list.get(0);
        for (int i=0;i<header.length;i++) {
            String h = header[i];
            if (h == null) {
                continue;
            }
            switch (h) {
                case "题号":
                    for (int j=0;j<subjects.size();j++) {
                        try {
                            subjects.get(j).setOrder(Integer.parseInt(list.get(j+1)[i]));
                        } catch (Exception e) {
                            throw new RuntimeException("题号错误,行号：" + (j+2));
                        }
                    }
                    break;
                case "题型":
                    for (int j=0;j<subjects.size();j++) {
                        try {
                            subjects.get(j).setType(SubjectType.ofText(list.get(j+1)[i]));
                        } catch (Exception e) {
                            throw new RuntimeException("题型错误,行号：" + (j+2));
                        }
                    }
                    break;
                case "题目":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setTitle(list.get(j+1)[i]);
                    }
                    break;
                case "选项1":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setOption1(list.get(j+1)[i]);
                    }
                    break;
                case "选项2":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setOption2(list.get(j+1)[i]);
                    }
                    break;
                case "选项3":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setOption3(list.get(j+1)[i]);
                    }
                    break;
                case "选项4":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setOption4(list.get(j+1)[i]);
                    }
                    break;
                case "选项5":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setOption5(list.get(j+1)[i]);
                    }
                    break;
                case "选项6":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setOption6(list.get(j+1)[i]);
                    }
                    break;
                case "选项7":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setOption7(list.get(j+1)[i]);
                    }
                    break;
                case "选项8":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setOption8(list.get(j+1)[i]);
                    }
                    break;
                case "选项9":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setOption9(list.get(j+1)[i]);
                    }
                    break;
                case "选项10":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setOption10(list.get(j+1)[i]);
                    }
                    break;
                case "答案":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setAnswer(list.get(j+1)[i]);
                    }
                    break;
                case "答案解析":
                    for (int j=0;j<subjects.size();j++) {
                        subjects.get(j).setAnalysis(list.get(j+1)[i]);
                    }
                    break;
                case "分值":
                    for (int j=0;j<subjects.size();j++) {
                        try {
                            subjects.get(j).setScore(parseOrDefault(list.get(j+1)[i], 10));
                        } catch (Exception e) {
                            throw new RuntimeException("题型错误,行号：" + (j+2));
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("未知列名");
            }
        }

        Exam exam = new Exam();
        exam.setTitle("test");
        exam.setId(1);

        Set<Integer> set = new HashSet<>();
        for (int j=0;j<subjects.size();j++) {
            subjects.get(j).setExam(exam);

            try {
                subjects.get(j).check();
            } catch (Exception e) {
                throw new IllegalArgumentException("行号：" + (j+2) + ",出现错误:" + e.getMessage());
            }

            if (set.contains(subjects.get(j).getOrder())) {
                throw new IllegalArgumentException("题号重复,行号：" + (j+2));
            } else {
                set.add(subjects.get(j).getOrder());
            }
            if (subjects.get(j).getOrder() > subjects.size() || subjects.get(j).getOrder() < 1) {
                throw new IllegalArgumentException("题号范围错误");
            }
        }

        return subjects;
    }

    private Integer parseOrDefault(String text, int defaultVal) {
        Integer result = null;
        try {
            result = Integer.parseInt(text);
        } catch (Exception e) {

        }
        if (result == null) {
            result = defaultVal;
        }
        return result;
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

    private void convertToMap(Result result, Map<String, Object> map) {
        if (result == null || map == null) {
            return;
        }
        if (result.isSuccess()) {
            map.put("response", result.getMsg());
        } else {
            map.put("error", result.getMsg());
        }
    }
}
