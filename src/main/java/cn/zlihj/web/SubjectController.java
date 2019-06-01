package cn.zlihj.web;

import cn.zlihj.domain.Subject;
import cn.zlihj.dto.ListResult;
import cn.zlihj.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/subject")
public class SubjectController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SubjectService subjectService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ListResult<Subject> listByExam(@RequestParam(required = true, value = "examId") Integer examId) {
        ListResult<Subject> result = null;

        try {
            List<Subject> subjects = subjectService.list(examId);
            result = ListResult.successList(subjects);
        } catch (Exception e) {
            logger.error("listByExam:{}", examId, e);
            result = ListResult.errorList(e.getMessage());
        }

        return result;
    }
}
