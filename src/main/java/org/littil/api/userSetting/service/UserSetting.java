package org.littil.api.userSetting.service;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UserSetting {
    @NotEmpty(message = "{UserSetting.key.required}")
    private String key;
    @NotEmpty(message = "{UserSetting.value.required}")
    private String value;
}