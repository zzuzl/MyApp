package cn.zlihj.service;

import cn.zlihj.dao.CompanyDao;
import cn.zlihj.dao.PermissionDao;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class StaffService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ConcurrentMap<Integer, Company> companyConcurrentMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Project> projectConcurrentMap = new ConcurrentHashMap<>();
    private final Comparator<VersionInfo> comparator = new Comparator<VersionInfo>() {
        @Override
        public int compare(VersionInfo o1, VersionInfo o2) {
            return o1.getVersion().compareTo(o2.getVersion());
        }
    };

    @Autowired
    private StaffDao staffDao;
    @Autowired
    private CompanyDao companyDao;
    @Autowired
    private ProjectDao projectDao;
    @Autowired
    private PermissionDao permissionDao;

    public Staff login(String email, String password) {
        Staff staff = staffDao.findByEmail(email);
        Assert.notNull(staff, "用户不存在:" + email);

        Assert.isTrue(ParamUtil.md5(password).equals(staff.getPassword()), "密码错误");

        return staff;
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        int count = staffDao.changePassword(email, ParamUtil.md5(oldPassword), ParamUtil.md5(newPassword));
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

    public ListResult<Staff> pageList(int page,
                                      int size,
                                      Source source,
                                      WorkType type,
                                      Integer pid,
                                      String key) {
        List<Staff> list = staffDao.pageList((page - 1) * size,
                size,
                source == null ? null : source.value(),
                type,
                pid,
                key);

        for (Staff staff : list) {
            fillStaffInfo(staff);
        }
        ListResult<Staff> listResult = ListResult.successList(list);
        listResult.setTotal(staffDao.count(source == null ? null : source.value(), pid, key));

        return listResult;
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

    public void resetPwd(String email, String pwd) {
        Assert.isTrue(staffDao.resetPwd(email, pwd) == 1, "重置密码失败");
    }

    public void save(Staff staff) {
        Assert.notNull(staff, "staff 不能为空");

        if(staff.getId() == null) {
            addStaff(staff);
        } else {
            staffDao.modifyInfo(staff);
        }
    }

    public void move(int source, Integer pid, int oldSource, Integer oldPid, Long id) {
        int i = staffDao.moveStaff(source, pid, oldSource, oldPid, id);
        Assert.isTrue(i == 1, "移动失败");
    }

    public List<SearchVo> searchAll(String key) {
        return staffDao.searchAll(key);
    }

    public VersionInfo findMaxVersion() {
        return staffDao.findMaxVersion();
    }

    public void insertIosUuid(String uuid) {
        staffDao.insertIosUuid(uuid);
    }

    public String findUuid(String uuid) {
        return staffDao.findUuid(uuid);
    }

    public boolean updateAvatar(long id, String avatar) {
        return staffDao.updateAvatar(id, avatar) == 1;
    }

    public List<String> permissions(String email) {
        return permissionDao.listPermissions(email);
    }

    public boolean hasPermission(String permission, String email) {
        List<String> list = permissions(email);
        if (list != null) {
            for (String key : list) {
                if (key.equals(permission)) {
                    return true;
                }
            }

        }
        return false;
    }
}
