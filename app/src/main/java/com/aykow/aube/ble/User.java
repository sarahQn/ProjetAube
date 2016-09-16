package com.aykow.aube.ble;

/**
 * Created by sarah on 09/03/2016.
 */
public class User {
    String email, password, firstName,idUser, lastName, address, birthDate;

    public User(String email){
        this.email=email;
        this.firstName="";
        this.lastName="";
        this.idUser="";
        this.address="";
    }

    public User(String email, String password){
        this.email=email;
        this.password = password;
        this.firstName="";
        this.lastName="";
        this.address="";
    }
    public User(String firstName, String lastName, String email, String password, String birthDate, String address) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.password = password;
        this.birthDate = birthDate;
    }
    public User(String firstName, String lastName, String email, String idUser, String birthDate, String address, boolean val) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.idUser = idUser;
        this.address = address;
        this.birthDate = birthDate;
    }

}
