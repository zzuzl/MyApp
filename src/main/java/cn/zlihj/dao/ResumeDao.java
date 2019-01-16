package cn.zlihj.dao;

import cn.zlihj.domain.Resume;
import java.util.List;

public interface ResumeDao {

    void insert(Resume resume);

    int update(Resume resume);

    List<Resume> listByStaff(Long staffId);

    int del(Long id);
}
