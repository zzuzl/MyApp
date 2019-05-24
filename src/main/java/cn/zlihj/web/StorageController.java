package cn.zlihj.web;

import cn.zlihj.domain.Exam;
import cn.zlihj.domain.Storage;
import cn.zlihj.dto.ListResult;
import cn.zlihj.dto.Result;
import cn.zlihj.enums.StorageType;
import cn.zlihj.service.StorageService;
import cn.zlihj.service.SubjectService;
import cn.zlihj.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/storage")
@Controller
public class StorageController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StorageService storageService;
    @Autowired
    private SubjectService subjectService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ListResult<Storage> list(@RequestParam("type") Integer type) {
        ListResult<Storage> result = null;
        try {
            StorageType storageType = StorageType.of(type);
            Assert.notNull(storageType, "参数错误");
            List<Storage> list = storageService.listByType(storageType);
            result = ListResult.successList(list);
        } catch (Exception e) {
            logger.error("查询存储条目失败：{}", e.getMessage(), e);
            result = ListResult.errorList(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/getExam", method = RequestMethod.GET)
    @ResponseBody
    public Result getExam() {
        Result result = null;
        try {
            Exam exam = subjectService.getExamById(1);
            result = Result.successResult();
            result.setData(exam);
        } catch (Exception e) {
            logger.error("获取题目配置失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    @Authorization(key = Authorization.STORAGE_OPERATE)
    public Result save(@RequestBody Storage storage) {
        Result result = null;
        try {
            ParamUtil.checkBean(storage);
            storageService.save(storage);
            result = Result.successResult();
        } catch (Exception e) {
            logger.error("保存存储条目失败：{}", e.getMessage(), e);
            result = Result.errorResult(e.getMessage());
        }
        return result;
    }
}
