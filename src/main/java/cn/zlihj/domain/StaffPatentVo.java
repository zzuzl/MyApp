package cn.zlihj.domain;

public class StaffPatentVo {
    private Long id;
    private Long staffId;
    private Integer patentId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Integer getPatentId() {
        return patentId;
    }

    public void setPatentId(Integer patentId) {
        this.patentId = patentId;
    }

    @Override
    public String toString() {
        return "StaffPatentVo{" +
                "id=" + id +
                ", staffId=" + staffId +
                ", patentId=" + patentId +
                '}';
    }
}
