package cn.zlihj.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StorageIndex {
    @NotNull
    private Integer id;
    @NotNull
    @Min(0)
    private Integer index;
    @NotNull
    private Integer oldIndex;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getOldIndex() {
        return oldIndex;
    }

    public void setOldIndex(Integer oldIndex) {
        this.oldIndex = oldIndex;
    }
}
