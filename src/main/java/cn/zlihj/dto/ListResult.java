package cn.zlihj.dto;

import java.util.ArrayList;
import java.util.List;

public class ListResult<T> extends Result {
    private int page;
    private long total;
    private int size;
    private List<T> data = new ArrayList<>();

    public ListResult(boolean success, List<T> data) {
        super(success);
        setData(data);
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        if (data == null) {
            data = new ArrayList<>();
        }
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static <T> ListResult<T> successList(List<T> collection) {
        return new ListResult<>(true, collection);
    }

    public static <T> ListResult<T> errorList(String msg) {
        ListResult<T> result = new ListResult<T>(false, null);
        result.setMsg(msg);
        return result;
    }

    @Override
    public String toString() {
        return "ListResult{" +
                "data=" + data +
                '}';
    }
}
