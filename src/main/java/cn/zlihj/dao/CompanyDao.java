package cn.zlihj.dao;

import cn.zlihj.domain.Company;

import java.util.List;

public interface CompanyDao {
    void insert(Company company);

    int update(Company company);

    Company findById(Integer id);

    Company findByName(String name);

    List<Company> list(Integer pid);
}
