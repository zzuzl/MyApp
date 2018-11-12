package cn.zlihj.dao;

import cn.zlihj.domain.WorkType;

import java.util.List;

public interface WorkTypeDao {
    void insert(WorkType workType);

    List<WorkType> list();
}
