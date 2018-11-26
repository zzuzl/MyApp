package cn.zlihj.service;

import cn.zlihj.dao.CompanyDao;
import cn.zlihj.domain.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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

    public void save(Company company) {
        Assert.notNull(company, "company 不能为空");

        if(company.getId() == null) {
            insert(company);
        } else {
            int update = companyDao.update(company);
            Assert.isTrue(update == 1, "修改失败");
        }
    }
}
