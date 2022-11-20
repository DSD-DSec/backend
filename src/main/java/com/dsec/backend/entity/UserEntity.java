package com.dsec.backend.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.dsec.backend.model.user.UserRegisterDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
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
}
