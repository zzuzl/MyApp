package cn.zlihj.service;

import cn.zlihj.dao.SubjectDao;
import cn.zlihj.domain.Exam;
import cn.zlihj.domain.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubjectService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SubjectDao subjectDao;

    @Transactional(rollbackFor = Exception.class)
    public void saveExam(Exam exam, List<Subject> subjects) {
        int updateExam = subjectDao.updateExam(exam);
        Assert.isTrue(updateExam == 1, "更新失败");

        List<Integer> orders = new ArrayList<>();
        for (Subject subject : subjects) {
            subjectDao.insertOrUpdate(subject);
            orders.add(subject.getOrder());
        }

        int others = subjectDao.deleteOthers(exam.getId(), orders);
        logger.info("删除其他题目：{}个", others);
    }

    public List<Subject> list(Integer examId) {
        return subjectDao.list(examId);
    }

    public Exam getExamById(Integer examId) {
        return subjectDao.getById(examId);
    }

}
