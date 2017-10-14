package com.example.giangnguyen.model;

/**
 * Created by Giang Nguyen on 12/10/2017.
 */

public class Contact {
    private String name;
    private String phone;
    private int sex;

    public Contact(String name, String phone, int sex) {
        this.name = name;
        this.phone = phone;
        this.sex = sex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }
}
