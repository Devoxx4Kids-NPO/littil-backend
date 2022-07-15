package org.littil.api.auth.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.littil.api.auditing.repository.AbstractAuditableEntity;
import org.littil.api.auth.provider.Provider;
import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.littil.api.school.repository.SchoolEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "User")
@Table(name = "user")
public class UserEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    @Length(max = 50)
    private Provider provider = Provider.AUTH0;

    @Column(name = "provider_id")
    @NotNull
    private String providerId;

    @NotEmpty(message = "{User.emailAddress.required}")
    @Email
    @Column(name = "email_address", unique = true)
    private String emailAddress;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school", referencedColumnName = "school_id")
    private SchoolEntity school;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_teacher", referencedColumnName = "guest_teacher_id")
    private GuestTeacherEntity guestTeacher;
}
