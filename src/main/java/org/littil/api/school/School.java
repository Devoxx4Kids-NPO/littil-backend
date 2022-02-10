package org.littil.api.school;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class School {

    private @Id @GeneratedValue Long id;
    private String name;
    private String address;
    private String postalCode;
    private String contactPersonName;
    private String contactPersonEmail;

    public School () {}

    public School(String name, String postalCode,
                  String contactPersonName, String contactPersonEmail) {
        this.name = name;
        this.postalCode = postalCode;
        this.contactPersonName = contactPersonName;
        this.contactPersonEmail = contactPersonEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public String getContactPersonEmail() {
        return contactPersonEmail;
    }

    public void setContactPersonEmail(String contactPersonEmail) {
        this.contactPersonEmail = contactPersonEmail;
    }

    @Override
    public String toString() {
        return "School{" + "id=" + this.id + ", name='" + this.name + '\'' + ", address='" + this.address + '\'' +
        ", postalCode='" + this.postalCode + '\'' + ", contactPersonName='" + this.contactPersonName + '\'' +
                ", contactPersonEmail='" + this.contactPersonEmail + '\'' + '}';
    }
}
