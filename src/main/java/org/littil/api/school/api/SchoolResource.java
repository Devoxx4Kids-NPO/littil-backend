package org.littil.api.school.api;

import lombok.Value;

import java.util.UUID;

@Value
public class SchoolResource {
    UUID id;
    String name;
    String address;
    String postalCode;
    String contactPersonName;
    String contactPersonEmail;
}
