package cn.zlihj.dao;

import cn.zlihj.domain.Storage;
import cn.zlihj.dto.StorageIndex;
import cn.zlihj.enums.StorageType;

import java.util.List;

public interface StorageDao {

    void insert(Storage storage);

    int update(Storage storage);

    List<Storage> listByType(StorageType storageType);

    int del(Integer id);

    int saveIndex(StorageIndex storageIndex);
}
