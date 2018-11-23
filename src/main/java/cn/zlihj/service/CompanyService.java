package cn.zlihj.service;

import cn.zlihj.dao.CompanyDao;
import cn.zlihj.domain.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {
    @Autowired
    private CompanyDao companyDao;

    public Company findById(Integer id) {
        return companyDao.findById(id);
    }

    public Company findByName(String name) {
        return companyDao.findByName(name);
    }

    public void insert(Company company) {
        companyDao.insert(company);
    }

    public List<Company> list(Integer pid) {
        return companyDao.list(pid);
    }
}
