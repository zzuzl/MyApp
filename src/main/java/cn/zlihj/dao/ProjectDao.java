package cn.zlihj.dao;

import cn.zlihj.domain.Project;

import java.util.List;

public interface ProjectDao {
    void insert(Project project);

    Project findByName(String name);

    List<Project> list(Integer pid);
}
