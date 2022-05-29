package org.littil.api.school.api;

import lombok.Value;

@Value
public class SchoolUpsertResource {
    String name;
    String address;
    String postalCode;
    String contactPersonName;
    String contactPersonEmail;
}
