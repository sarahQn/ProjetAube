package com.aykow.aube.ble;

/**
 * Created by sarah on 28/04/2016.
 */
public class UserDevicesDB {
    private long id;
    private String addr;
    private String name;
    private String classDev;
    private String subClass;
    private String idUser;


    public long getIdDev() {
        return id;
    }

    public void setIdDev(long id) {
        this.id = id;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getNameDev() {
        return name;
    }

    public void setNameDev(String name) {
        this.name = name;
    }

    public String getClassDev() {
        return classDev;
    }

    public void setClassDev(String classDev) {
        this.classDev = classDev;
    }

    public String getSubClassDev() {
        return subClass;
    }

    public void setSubClassDev(String subClass) {
        this.subClass = subClass;
    }

    public String getIdUserDev() {
        return idUser;
    }

    public void setIdUserDev(String idUser) {
        this.idUser = idUser;
    }
}



