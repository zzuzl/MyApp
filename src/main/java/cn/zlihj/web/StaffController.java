package cn.zlihj.web;

import cn.zlihj.domain.Staff;
import cn.zlihj.dto.ListResult;
import cn.zlihj.dto.Result;
import cn.zlihj.enums.Source;
import cn.zlihj.enums.WorkType;
import cn.zlihj.service.CompanyService;
import cn.zlihj.service.ProjectService;
import cn.zlihj.service.StaffService;
import cn.zlihj.util.LoginContext;
import cn.zlihj.util.ParamUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

@RequestMapping("/staff")
@Controller
public class StaffController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static final String[] EXTENSIONS = new String[]{
            "jpg", "jpeg", "png"
    };

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
                                       Integer type,
                                       @RequestParam("page") Integer page) {
        ListResult<Staff> result = null;
        try {
            WorkType workType = WorkType.of(type);
            result = staffService.pageList(page, 20, Source.of(source), workType, pid, null);
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
            result = staffService.pageList(page, 50, null, null, null, key);
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

    @RequestMapping(value = "/uploadAvatar", method = RequestMethod.POST)
    @ResponseBody
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        Result result = null;
        try {
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            Assert.isTrue(isPhoto(extension), "文件格式错误");

            logger.info(file.getOriginalFilename() + ",size:" + file.getSize());

            Assert.isTrue(file.getSize() < 10240000, "图片超过10M");

            String rootPath = "/tmp";
            File dir = new File(rootPath + File.separator + "tmpAvatars");
            if (!dir.exists()) {
                Assert.isTrue(dir.mkdirs(), "文件夹创建失败");
            }
            File serverFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
            file.transferTo(serverFile);

            BufferedImage bufImage = ImageIO.read(serverFile);
            int width = bufImage.getWidth();
            int height = bufImage.getHeight();
            int w = width;
            int x=0, y=0;
            if (width > height) {
                x += (width - height) / 2;
                w = height;
            } else if (width < height) {
                y += (height - width) / 2;
                w = width;
            }
            BufferedImage subimage = bufImage.getSubimage(x, y, w, w);
            ImageIO.write(subimage, "jpg", serverFile);

            // 上传到COS
            COSCredentials cred = new BasicCOSCredentials("AKIDIQNCnEdaYTGD7OSUIVXO6e4JQNchpMzs", "uhKwMbheR3BtZ4NZiieS7jAPVWM1qbDg");
            ClientConfig clientConfig = new ClientConfig(new Region("ap-beijing"));
            COSClient cosclient = new COSClient(cred, clientConfig);

            String key = "avatars/" + ParamUtil.md5(file.getOriginalFilename()) + ".jpeg";
            PutObjectRequest putObjectRequest = new PutObjectRequest("zlihj-zpk-1251746773", key, serverFile);
            cosclient.putObject(putObjectRequest);
            cosclient.shutdown();

            serverFile.deleteOnExit();

            // 更新字段
            String avatar = "https://zlihj-zpk-1251746773.cos.ap-beijing.myqcloud.com/" + key;
            staffService.updateAvatar(LoginContext.currentUser().getId(), avatar);

            result = Result.successResult();
            result.setMsg(avatar);
        } catch (Exception e) {
            logger.error("uploadFile error:", e);
            result = Result.errorResult(e.getMessage());
        }

        return result;
    }

    private boolean isPhoto(String extension) {
        for (String s : EXTENSIONS) {
            if (s.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }
}
