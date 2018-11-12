package test;

import cn.zlihj.dao.CompanyDao;
import cn.zlihj.domain.Company;
import cn.zlihj.service.CompanyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring-config.xml")
public class SpringTest {
    @Autowired
    private CompanyService companyService;
    @Autowired
    private CompanyDao companyDao;

    @Test
    public void testInsert() {
        Company company = new Company();
        company.setName("test1");
        companyService.insert(company);
    }

    @Test
    public void testFind() {
        // System.out.println(companyDao.findByName("test1").getCreateTime());
    }

}
