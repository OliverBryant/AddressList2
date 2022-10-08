package com.example.addresslist.entity;

public interface SimplyContector {
    String getName();
    String getNum();
    default String getString(){
        return "name"+getName()+";ImageId"+getNum();
    }
}
