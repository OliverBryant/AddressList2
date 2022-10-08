package com.example.addresslist.entity;

public class SimContactor {
    private int id;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String name;
    private String num;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    private String mobilePhone;

    public String getMobilePhone() {
        return mobilePhone;
    }
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }
    @Override
    public String toString() {
        return "SimContactor{" +
                "name='" + name + '\'' +
                ", num='" + num + '\'' +
                '}';
    }
}
