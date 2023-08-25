package org.littil.api.userSetting.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
public class UserSetting {
    @NotEmpty(message = "{UserSetting.key.required}")
    private String key;
    @NotEmpty(message = "{UserSetting.value.required}")
    private String value;
}