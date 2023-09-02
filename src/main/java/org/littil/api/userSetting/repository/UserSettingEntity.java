package org.littil.api.userSetting.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(value = UserSettingEntity.UserSettingId.class)
@Entity(name = "UserSetting")
@Table(name = "user_setting")
public class UserSettingEntity implements Serializable {
    private static final long serialVersionUID = 42L;

    @Id
    @Column(name = "user_id", columnDefinition = " BINARY(16)")
    private UUID userId;

    @Id
    @Column(name = "setting_key")
    private String key;

    @Column(name = "setting_value")
    private String value;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSettingId implements Serializable {
        private static final long serialVersionUID = 42L;
        private UUID userId;
        private String key;
    }
}
