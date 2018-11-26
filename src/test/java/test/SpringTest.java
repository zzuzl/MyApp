package test;

import cn.zlihj.domain.Company;
import cn.zlihj.domain.Project;
import cn.zlihj.domain.Staff;
import cn.zlihj.domain.VersionInfo;
import cn.zlihj.dto.ListResult;
import cn.zlihj.enums.Source;
import cn.zlihj.service.CompanyService;
import cn.zlihj.service.ProjectService;
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
    private ProjectService projectService;
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

        Staff staff = staffService.findById(1L);
        System.out.println(staff);

        VersionInfo maxVersion = staffService.findMaxVersion();
        System.out.println(maxVersion.getUrl());
        // System.out.println(companyService.findById(1));
    }

    @Test
    public void testSave() {
        /*Company company = companyService.findById(1);
        company.setName("技术质量部1");
        companyService.save(company);*/

        Project project = projectService.findById(1);
        project.setName("技术质量部1");
        projectService.save(project);
    }

    @Test
    public void testMove() {
        staffService.move(0, 1, 1, 1, 1L);
    }

    @Test
    public void testPage() {
        ListResult<Staff> list = staffService.pageList(1, 20, null, null);
        System.out.println(list.getSize());
    }
}
