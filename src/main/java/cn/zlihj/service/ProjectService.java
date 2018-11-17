package cn.zlihj.service;

import cn.zlihj.dao.ProjectDao;
import cn.zlihj.domain.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectDao projectDao;

    public void insert(Project project) {
        projectDao.insert(project);
    }

    public Project findById(Integer id) {
        return projectDao.findById(id);
    }

    public List<Project> list(Integer pid) {
        return projectDao.list(pid);
    }
}
