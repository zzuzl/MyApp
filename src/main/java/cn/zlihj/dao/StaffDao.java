package cn.zlihj.dao;

import cn.zlihj.domain.SearchVo;
import cn.zlihj.domain.Staff;
import cn.zlihj.domain.VersionInfo;
import cn.zlihj.enums.WorkType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StaffDao {
    void insert(Staff staff);

    Staff findByEmail(@Param("email") String email);

    Staff findById(@Param("id") Long id);

    List<Staff> pageList(@Param("from") int from,
                         @Param("size") int size,
                         @Param("source") Integer source,
                         @Param("type") WorkType type,
                         @Param("pid") Integer pid,
                         @Param("key") String key);

    int count(@Param("source") Integer source,
              @Param("pid") Integer pid,
              @Param("key") String key);

    int changePassword(@Param("email") String email,
                       @Param("oldPassword") String oldPassword,
                       @Param("newPassword") String newPassword);

    int updateInfo(Staff staff);

    int modifyInfo(Staff staff);

    int moveStaff(@Param("source") int source,
                  @Param("pid") Integer pid,
                  @Param("oldSource") int oldSource,
                  @Param("oldPid") Integer oldPid,
                  @Param("id") Long id);

    List<SearchVo> searchAll(@Param("key") String key);

    VersionInfo findMaxVersion();

    String findUuid(@Param("uuid") String uuid);

    void insertIosUuid(@Param("uuid") String uuid);

    int resetPwd(@Param("email") String email,
                 @Param("pwd") String pwd);

    int updateAvatar(@Param("id") long id,
                     @Param("avatar") String avatar);
}
