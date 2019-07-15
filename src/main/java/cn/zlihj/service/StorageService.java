package cn.zlihj.service;

import cn.zlihj.dao.StorageDao;
import cn.zlihj.domain.Storage;
import cn.zlihj.dto.StorageIndex;
import cn.zlihj.enums.StorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

@Service
public class StorageService {
    private final List<Integer> TYPES = Arrays.asList(StorageType.SYSTEM.value(), StorageType.DATA.value());

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
            if (storage.getStorageType() == StorageType.SYSTEM || storage.getStorageType() == StorageType.DATA) {
                int maxOrder = storageDao.getMaxOrder(TYPES);
                storage.setItemOrder(maxOrder + 1);
            }
            add(storage);
        } else {
            update(storage);
        }
    }

    public void saveIndex(StorageIndex storageIndex) {
        int row = storageDao.saveIndex(storageIndex);
        if (row != 1) {
            throw new RuntimeException("顺序更新失败:" + storageIndex.getId());
        }
    }
}
