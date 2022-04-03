package org.littil.api.school;

import lombok.Value;

@Value
public class SchoolDto {

    private final Long id;
    private final String name;
    private final String address;
    private final String postalCode;
    private final String contactPersonName;
    private final String contactPersonEmail;
}
