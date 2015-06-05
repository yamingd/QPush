package com.argo.qpush.core;

/**
 * Created by yamingd on 6/5/15.
 */
public enum DeviceType {

    UNKNOWN(0, "UNKNOWN"),
    iPhone(1, "iPhone"),
    iPad(2, "iPad"),
    Android(3, "Android"),
    WinPhone(4, "WinPhone");

    int id;
    String name;

    DeviceType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "DeviceType{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
    }
}
