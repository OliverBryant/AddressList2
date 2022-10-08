package com.example.addresslist.entity;

public class Contector {
    private Integer id;

    private String name;
    private String mobilePhone;
    private String officePhone;
    private String familyPhone;
    private String position;
    private String company;
    private String address;
    private String zipcode;
    private String email;
    private String remark;
    private String num;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public String getFamilyPhone() {
        return familyPhone;
    }

    public void setFamilyPhone(String familyPhone) {
        this.familyPhone = familyPhone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":\"" + id + '\"' +
                ",\"name\":\"" + name + '\"' +
                ",\"mobilePhone\":\"" + mobilePhone + '\"' +
                ",\"officePhone\":\"" + officePhone + '\"' +
                ",\"familyPhone\":\"" + familyPhone + '\"' +
                ",\"position\":\"" + position + '\"' +
                ",\"company\":\"" + company + '\"' +
                ",\"address\":\"" + address + '\"' +
                ",\"zipcode\":\"" + zipcode + '\"' +
                ",\"email\":\"" + email + '\"' +
                ",\"remark\":\"" + remark + '\"' +
                ",\"num\":\"" + num + '\"' +
                '}';
    }
}
