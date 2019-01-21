package cn.zlihj.dao;

import cn.zlihj.domain.Patent;
import cn.zlihj.domain.StaffPatentVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PatentDao {
    void insert(Patent patent);

    int update(Patent patent);

    int del(Integer id);

    List<Patent> pageList(@Param("from") int from,
                          @Param("size") int size);

    void delMapById(Long id);

    int count();

    void delMapByPatentId(Integer id);

    void insertMap(StaffPatentVo vo);

    List<StaffPatentVo> listMap(Integer id);

    Patent findById(Integer id);
}
