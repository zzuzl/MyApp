package cn.zlihj.domain;

import cn.zlihj.enums.Gender;
import cn.zlihj.enums.Source;
import cn.zlihj.enums.WorkType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.validator.constraints.Length;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Staff implements Checkable {
    private Long id;
    @NotNull
    @Length(max = 10)
    private String name;
    @NotNull
    private Integer pid;
    private String pname;
    @NotNull
    private WorkType type;
    @NotNull
    private Source source;
    @NotNull
    private Gender gender;
    @JsonFormat(pattern = "yyyy-MM-dd", locale = "zh", timezone = "GMT+8")
    private Date birthday;
    private String school;
    private String major;
    @NotNull
    private String phone;
    private String qq;
    private String gxtAccount;
    @NotNull
    private String email;
    private String wx;
    private String workAddress;
    private String avatar;
    private String password;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public WorkType getType() {
        return type;
    }

    public void setType(WorkType type) {
        this.type = type;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getGxtAccount() {
        return gxtAccount;
    }

    public void setGxtAccount(String gxtAccount) {
        this.gxtAccount = gxtAccount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWx() {
        return wx;
    }

    public void setWx(String wx) {
        this.wx = wx;
    }

    public String getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(String workAddress) {
        this.workAddress = workAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public void checkAttribute() {
        if (StringUtils.hasText(name)) {
            Assert.isTrue(!StringUtils.containsWhitespace(name), "姓名包含空白字符");
        }

        if (StringUtils.hasText(phone)) {
            Assert.isTrue(NumberUtils.isDigits(phone), "手机号必须为数字");
            Assert.isTrue(phone.length() == 11, "手机号必须为11位，固定电话请加上区号");
        }

        if (StringUtils.hasText(qq)) {
            Assert.isTrue(NumberUtils.isDigits(qq), "QQ必须为数字");
        }

        if (StringUtils.hasText(wx)) {
            Assert.isTrue(!StringUtils.containsWhitespace(wx), "微信号包含空白字符");
        }

        if (StringUtils.hasText(gxtAccount)) {
            Assert.isTrue(!StringUtils.containsWhitespace(gxtAccount), "广讯通包含空白字符");
        }

        if (StringUtils.hasText(email)) {
            Assert.isTrue(!StringUtils.containsWhitespace(email), "邮箱包含空白字符");
        }
    }
}
