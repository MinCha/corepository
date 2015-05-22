package com.github.corepo.client.measurement.support;

import com.github.corepo.client.BaseObject;

import java.io.Serializable;

@SuppressWarnings("serial")
public class NameAge extends BaseObject implements Serializable {
    private String name;
    private int age;

    public NameAge(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}