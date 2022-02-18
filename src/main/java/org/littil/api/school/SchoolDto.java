package org.littil.api.school;

public class SchoolDto {

    private Long id;
    private String name;
    private String address;
    private String postalCode;
    private String contactPersonName;
    private String contactPersonEmail;

    public SchoolDto(final Long id, final String name, final String address, final String postalCode, final String contactPersonName, final String contactPersonEmail) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.postalCode = postalCode;
        this.contactPersonName = contactPersonName;
        this.contactPersonEmail = contactPersonEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

}
