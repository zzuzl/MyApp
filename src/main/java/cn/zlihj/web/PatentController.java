package cn.zlihj.web;

import cn.zlihj.domain.Patent;
import cn.zlihj.dto.ListResult;
import cn.zlihj.dto.Result;
import cn.zlihj.service.PatentService;
import cn.zlihj.service.StaffService;
import cn.zlihj.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ListResult<Patent> findByPid(@RequestParam("page") Integer page) {
        ListResult<Patent> result = null;
        try {
            result = patentService.listPage(page, 20);
        } catch (Exception e) {
            logger.error("查询专利失败：{}", e.getMessage(), e);
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

            for (Long staffId : patent.getAuthors()) {
                Assert.notNull(staffService.findById(staffId), "人员不存在：" + staffId);
            }

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
