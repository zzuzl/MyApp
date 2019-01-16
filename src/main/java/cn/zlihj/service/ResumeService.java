package cn.zlihj.service;

import cn.zlihj.dao.ResumeDao;
import cn.zlihj.domain.Resume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class ResumeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ResumeDao resumeDao;

    public void save(Resume resume) {
        if (resume.getId() == null) {
            resumeDao.insert(resume);
        } else {
            Assert.isTrue(resumeDao.update(resume) == 1, "简历更新失败");
        }
    }

    public List<Resume> listByStaff(Long staffId) {
        return resumeDao.listByStaff(staffId);
    }

    public void del(Long id) {
        Assert.isTrue(resumeDao.del(id) == 1, "简历删除失败");
    }
}
