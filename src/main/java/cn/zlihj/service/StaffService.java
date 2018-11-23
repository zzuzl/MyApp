package cn.zlihj.service;

import cn.zlihj.dao.CompanyDao;
import cn.zlihj.dao.ProjectDao;
import cn.zlihj.dao.StaffDao;
import cn.zlihj.domain.*;
import cn.zlihj.dto.ListResult;
import cn.zlihj.enums.Source;
import cn.zlihj.enums.WorkType;
import cn.zlihj.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class StaffService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ConcurrentMap<Integer, Company> companyConcurrentMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Project> projectConcurrentMap = new ConcurrentHashMap<>();

    @Autowired
    private StaffDao staffDao;
    @Autowired
    private CompanyDao companyDao;
    @Autowired
    private ProjectDao projectDao;

    public Staff login(String email, String password) {
        Staff staff = staffDao.findByEmail(email);
        Assert.notNull(staff, "用户不存在:" + email);

        Assert.isTrue(ParamUtil.md5(password).equals(staff.getPassword()), "密码错误");

        return staff;
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        int count = staffDao.changePassword(email, oldPassword, newPassword);
        Assert.isTrue(count == 1, "修改失败");
    }

    public Staff findById(Long id) {
        return staffDao.findById(id);
    }

    public void updateInfo(Staff staff) {
        int count = staffDao.updateInfo(staff);
        Assert.isTrue(count == 1, "修改失败");
    }

    public Staff findByEmail(String email) {
        return staffDao.findByEmail(email);
    }

    public ListResult<Staff> pageList(int page, int size, Source source, Integer pid) {
        List<Staff> list = staffDao.pageList((page - 1) * size, size, source.value().intValue(), pid);

        for (Staff staff : list) {
            fillStaffInfo(staff);
        }

        return ListResult.successList(list);
    }

    public void addStaff(Staff staff) {
        staffDao.insert(staff);
    }

    public Staff fillStaffInfo(Staff staff) {
        switch (staff.getSource()) {
            case COMPANY:
                Company company = companyConcurrentMap.get(staff.getPid());
                if (company == null) {
                    company = companyDao.findById(staff.getPid());
                    companyConcurrentMap.put(staff.getPid(), company);
                }
                if (company != null) {
                    staff.setPname(company.getName());
                }
                break;
            case PROJECT:
                Project project = projectConcurrentMap.get(staff.getPid());
                if (project == null) {
                    project = projectDao.findById(staff.getPid());
                    projectConcurrentMap.put(staff.getPid(), project);
                }
                if (project != null) {
                    staff.setPname(project.getName());
                }
                break;
            default:
                break;
        }

        return staff;
    }

    public List<SearchVo> searchAll(String key) {
        return staffDao.searchAll(key);
    }
}
