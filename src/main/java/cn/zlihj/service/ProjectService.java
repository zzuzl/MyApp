package cn.zlihj.service;

import cn.zlihj.dao.ProjectDao;
import cn.zlihj.domain.Company;
import cn.zlihj.domain.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectDao projectDao;

    public void insert(Project project) {
        projectDao.insert(project);
    }

    public void save(Project project) {
        Assert.notNull(project, "project 不能为空");

        if(project.getId() == null) {
            insert(project);
        } else {
            int update = projectDao.update(project);
            Assert.isTrue(update == 1, "修改失败");
        }
    }

    public Project findById(Integer id) {
        return projectDao.findById(id);
    }

    public Project findByName(String name) {
        return projectDao.findByName(name);
    }

    public List<Project> list(Integer pid) {
        return projectDao.list(pid);
    }
}
