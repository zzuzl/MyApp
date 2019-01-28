package cn.zlihj.service;

import cn.zlihj.dao.StorageDao;
import cn.zlihj.domain.Storage;
import cn.zlihj.enums.StorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class StorageService {

    @Autowired
    private StorageDao storageDao;

    private void add(Storage storage) {
        storageDao.insert(storage);
    }

    private void update(Storage storage) {
        Assert.isTrue(storageDao.update(storage) == 1, "找不到符合条件的条目，更新失败");
    }

    public List<Storage> listByType(StorageType storageType) {
        return storageDao.listByType(storageType);
    }

    public void del(Integer id) {
        Assert.isTrue(storageDao.del(id) == 1, "删除失败");
    }

    public void save(Storage storage) {
        if (storage.getId() == null) {
            add(storage);
        } else {
            update(storage);
        }
    }
}
