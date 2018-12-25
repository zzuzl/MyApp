package cn.zlihj.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PermissionDao {

    List<String> listPermissions(@Param("email") String email);

}
