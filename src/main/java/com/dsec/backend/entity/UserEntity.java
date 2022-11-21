package com.dsec.backend.entity;

import com.dsec.backend.model.user.UserRegisterDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.Objects;

@Entity
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonRootName(value = "user")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "users")
public class UserEntity extends RepresentationModel<UserEntity> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, length = 30)
    private String firstName;

    @Column(nullable = false, length = 30)
    private String lastName;

    @Column(nullable = false, length = 320, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @ManyToOne(optional = false)
    private UserRole userRole;

    public UserEntity(UserRegisterDTO userRegisterDTO, UserRole userRole,
            PasswordEncoder passwordEncoder) {
        firstName = userRegisterDTO.getFirstName();
        lastName = userRegisterDTO.getLastName();
        email = userRegisterDTO.getEmail();
        password = passwordEncoder.encode(userRegisterDTO.getPassword());
        this.userRole = userRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserEntity that = (UserEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
