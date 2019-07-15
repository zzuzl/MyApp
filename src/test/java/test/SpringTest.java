package test;

import cn.zlihj.dao.PermissionDao;
import cn.zlihj.dao.StorageDao;
import cn.zlihj.domain.*;
import cn.zlihj.dto.ListResult;
import cn.zlihj.enums.StorageType;
import cn.zlihj.enums.WorkType;
import cn.zlihj.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring-config.xml")
public class SpringTest {
    @Autowired
    private CompanyService companyService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private JavaMailSenderImpl javaMailSender;
    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private ResumeService resumeService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private StorageDao storageDao;

    @Test
    public void testSubject() {
        List<Subject> subjects = subjectService.selectSubjects(1, 10);
        System.out.println(subjects);
    }

    @Test
    public void testInsert() {
        Company company = new Company();
        company.setName("test1");
        companyService.insert(company);
    }

    @Test
    public void testMaxOrder() {
        Integer max = storageDao.getMaxOrder(Arrays.asList(StorageType.SYSTEM.value(), StorageType.DATA.value()));
        System.out.println(max);
    }

    @Test
    public void testFind() {
        /*Staff staff = staffService.findByEmail("672399171@qq.com");
        System.out.println(staff);

        ListResult<Staff> listResult = staffService.pageList(1, 20, Source.PROJECT, 1);
        System.out.println(listResult);*/

        /*Staff staff = staffService.findById(1L);
        System.out.println(staff);

        VersionInfo maxVersion = staffService.findMaxVersion();
        System.out.println(maxVersion.getUrl());*/
        // System.out.println(companyService.findById(1));

        /*System.out.println(staffService.searchAll("zl"));
        System.out.println(staffService.searchAll("锦艺"));*/
        // System.out.println(staffService.pageList(1, 20, null, null).getTotal());

        System.out.println(staffService.searchAll("张"));
        System.out.println(staffService.searchAll("传"));
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
        ListResult<Staff> list = staffService.pageList(1, 20, null, WorkType.ZG, null, null);
        System.out.println(list.getSize());
    }

    @Test
    public void testUuid() {
        staffService.insertIosUuid("123456");
        System.out.println(staffService.findUuid("123"));
        System.out.println(staffService.findUuid("123456"));
        staffService.insertIosUuid("123456");
    }

    @Test
    public void testMail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("zlihj_no_reply@qq.com");
        message.setTo("zl672399171@gmail.com");
        message.setSubject("重置密码");
        message.setText("重置密码");
        javaMailSender.send(message);
    }

    @Test
    public void testPermission() {
        List<String> list = permissionDao.listPermissions("672399171@qq.com");
        System.out.println(list);
    }

    @Test
    public void testResume() {
        Resume resume = new Resume();
        resume.setStaffId(1L);
        resume.setContent("dawdawda");
        // resumeService.save(resume);
        System.out.println(resumeService.listByStaff(1L));
        resumeService.del(1L);
    }
}
