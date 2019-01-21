package cn.zlihj.web;

import cn.zlihj.domain.Patent;
import cn.zlihj.domain.Staff;
import cn.zlihj.domain.StaffPatentVo;
import cn.zlihj.dto.ListResult;
import cn.zlihj.dto.Result;
import cn.zlihj.service.PatentService;
import cn.zlihj.service.StaffService;
import cn.zlihj.util.LoginContext;
import cn.zlihj.util.ParamUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/patent")
@Controller
public class PatentController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PatentService patentService;
    @Autowired
    private StaffService staffService;

    @RequestMapping(value = "/pageList", method = RequestMethod.GET)
    @ResponseBody
    public ListResult<Patent> pageList(@RequestParam("page") Integer page) {
        ListResult<Patent> result = null;
        try {
            result = patentService.listPage(page, 20);
        } catch (Exception e) {
            logger.error("查询专利失败：{}", e.getMessage(), e);
            result = ListResult.errorList(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/authors", method = RequestMethod.GET)
    @ResponseBody
    public ListResult<Map> authors(@RequestParam("patentId") Integer patentId) {
        ListResult<Map> result = null;
        try {
            List<Map> maps = new ArrayList<>();
            List<StaffPatentVo> vos = patentService.listMap(patentId);
            for (StaffPatentVo vo : vos) {
                Map<String, Object> map = new HashMap<>();
                Staff staff = staffService.findById(vo.getStaffId());
                if (staff != null) {
                    map.put("id", staff.getId());
                    map.put("name", staff.getName());
                    maps.add(map);
                } else {
                    logger.error("对应的人员不存在，专利：" + patentId);
                }
            }

            result = ListResult.successList(maps);
        } catch (Exception e) {
            logger.error("获取作者失败：{}", e.getMessage(), e);
            result = ListResult.errorList(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public Result save(@RequestBody Patent patent) {
        Result result = null;
        try {
            Assert.notNull(patent, "参数为空");
            ParamUtil.checkBean(patent);

            Iterable<String> authors = Splitter.on(',')
                    .trimResults()
                    .omitEmptyStrings()
                    .split(patent.getAuthors());

            List<String> authorNames = new ArrayList<>();
            for (String author : authors) {
                Staff staff = staffService.findById(Long.parseLong(author));
                Assert.notNull(staff, "人员不存在：" + author);
                authorNames.add(staff.getName());
            }
            patent.setAuthorNames(Joiner.on(',').join(authorNames));

            if (patent.getId() == null) {
                patentService.save(patent, null);
            } else {
                patentService.save(patent, patentService.listMap(patent.getId()));
            }

            result = Result.successResult();
        } catch (Exception e) {
            logger.error("保存专利失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }

        return result;
    }

    @RequestMapping(value = "/del", method = RequestMethod.POST)
    @ResponseBody
    @Authorization(key = Authorization.PATENT_DEL)
    public Result del(@RequestParam Integer id) {
        Result result = null;
        try {
            Assert.notNull(id, "参数错误");
            patentService.delPatent(id);
            result = Result.successResult();
        } catch (Exception e) {
            logger.error("删除专利失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }

}
