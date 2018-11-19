package test;

import cn.zlihj.domain.Company;
import cn.zlihj.domain.Staff;
import cn.zlihj.dto.ListResult;
import cn.zlihj.enums.Source;
import cn.zlihj.service.CompanyService;
import cn.zlihj.service.StaffService;
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
    private StaffService staffService;

    @Test
    public void testInsert() {
        Company company = new Company();
        company.setName("test1");
        companyService.insert(company);
    }

    @Test
    public void testFind() {
        /*Staff staff = staffService.findByEmail("672399171@qq.com");
        System.out.println(staff);

        ListResult<Staff> listResult = staffService.pageList(1, 20, Source.PROJECT, 1);
        System.out.println(listResult);*/

        System.out.println(staffService.findById(1L));
        System.out.println(companyService.findById(1));
    }
}
