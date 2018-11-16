package cn.zlihj.service;

import cn.zlihj.dao.StaffDao;
import cn.zlihj.dao.WorkTypeDao;
import cn.zlihj.domain.Staff;
import cn.zlihj.domain.WorkType;
import cn.zlihj.dto.ListResult;
import cn.zlihj.enums.Source;
import cn.zlihj.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StaffService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StaffDao staffDao;
    @Autowired
    private WorkTypeDao workTypeDao;

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

    public void updateInfo(Staff staff) {
        int count = staffDao.updateInfo(staff);
        Assert.isTrue(count == 1, "修改失败");
    }

    public Staff findByEmail(String email) {
        return staffDao.findByEmail(email);
    }

    public ListResult<Staff> pageList(int page, int size, Source source, Integer pid) {
        List<WorkType> workTypes = workTypeDao.list();
        Map<Integer, WorkType> map = toMap(workTypes);

        List<Staff> list = staffDao.pageList((page - 1) * size, size, source.value().intValue(), pid);

        for (Staff staff : list) {
            staff.setType(map.get(staff.getType().getId()));
        }

        return ListResult.successList(list);
    }

    private Map<Integer, WorkType> toMap(final List<WorkType> workTypes) {
        Map<Integer, WorkType> map = new HashMap<>();
        for (WorkType workType : workTypes) {
            map.put(workType.getId(), workType);
        }
        return map;
    }
}
