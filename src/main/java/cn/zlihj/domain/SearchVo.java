package cn.zlihj.domain;

public class SearchVo {
    public static final int STAFF = 1;
    public static final int COMPANY = 2;
    public static final int PROJECT = 3;

    private Long id;
    private int type;
    private String title;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
