package cn.zlihj.service;

import cn.zlihj.dao.PatentDao;
import cn.zlihj.domain.Patent;
import cn.zlihj.domain.StaffPatentVo;
import cn.zlihj.dto.ListResult;
import com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;

@Service
public class PatentService {
    @Autowired
    private PatentDao patentDao;

    @Transactional(rollbackFor = Exception.class)
    public void save(Patent patent, List<StaffPatentVo> vos) {
        Iterable<String> authors = Splitter.on(',')
                .trimResults()
                .omitEmptyStrings()
                .split(patent.getAuthors());

        if (patent.getId() == null) {
            patentDao.insert(patent);
            for (String staffId : authors) {
                StaffPatentVo vo = new StaffPatentVo();
                vo.setPatentId(patent.getId());
                vo.setStaffId(Long.parseLong(staffId));
                patentDao.insertMap(vo);
            }
        } else {
            Assert.isTrue(patentDao.update(patent) == 1, "专利不存在：" + patent.getId());
            List<String> list = new ArrayList<>();
            for (String author : authors) {
                list.add(author);
            }
            Iterator<String> iterator = list.iterator();

            List<Long> ids = new ArrayList<>();
            Map<Long, StaffPatentVo> tmpMap = new HashMap<>();
            for (StaffPatentVo vo : vos) {
                ids.add(vo.getStaffId());
                tmpMap.put(vo.getStaffId(), vo);
            }

            while (iterator.hasNext()) {
                Long id = Long.parseLong(iterator.next());
                if (ids.contains(id)) {
                    iterator.remove();
                    ids.remove(id);
                    tmpMap.remove(id);
                }
            }
            for (StaffPatentVo patentVo : tmpMap.values()) {
                patentDao.delMapById(patentVo.getId());
            }
            for (String staffId : list) {
                StaffPatentVo vo = new StaffPatentVo();
                vo.setPatentId(patent.getId());
                vo.setStaffId(Long.parseLong(staffId));
                patentDao.insertMap(vo);
            }
        }
    }

    public void delPatent(Integer id) {
        patentDao.delMapByPatentId(id);
        patentDao.del(id);
    }

    public ListResult<Patent> listPage(int page, int size) {
        ListResult<Patent> result = null;
        result = ListResult.successList(patentDao.pageList((page - 1) * size, size));
        result.setTotal(patentDao.count());

        return result;
    }

    public List<StaffPatentVo> listMap(Integer patentId) {
        return patentDao.listMap(patentId);
    }

}
