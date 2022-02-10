package org.littil.api.teacher;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Teacher {

    private @Id @GeneratedValue Long id;
    private String firstName;
    private String surName;
    private String email;
    private String postalCode;
    private String country;
    private String preferences;
    private String availability;

    public Teacher () {}

    public Teacher(String firstName, String surName,
                   String email, String postalCode) {
        this.firstName = firstName;
        this.surName = surName;
        this.email = email;
        this.postalCode = postalCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    @Override
    public String toString() {
        return "Teacher{" + "id=" + this.id + ", firstName='" + this.firstName + '\'' + ", surName='" +
                this.surName + '\'' + ", email='" + this.email + '\'' + ", postalCode='" + this.postalCode +
                ", country='" + this.country + '\'' + ", preferences='" + this.preferences + '\'' +
                ", availability='" + this.availability + '\'' + '}';
    }
}
