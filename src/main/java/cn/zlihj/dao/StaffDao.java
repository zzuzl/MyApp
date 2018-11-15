package cn.zlihj.dao;

import cn.zlihj.domain.Staff;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StaffDao {
    void insert(Staff staff);

    Staff findByEmail(@Param("email") String email);

    List<Staff> pageList(@Param("from") int from,
                         @Param("size") int size,
                         @Param("source") int source,
                         @Param("pid") Integer pid);

    int changePassword(@Param("email") String email,
                       @Param("oldPassword") String oldPassword,
                       @Param("newPassword") String newPassword);

    int updateInfo(Staff staff);
}
