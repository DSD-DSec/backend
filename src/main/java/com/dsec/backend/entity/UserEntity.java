package com.dsec.backend.entity;

import com.dsec.backend.model.user.UserRegisterDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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

    @Column(nullable = false, length = 32)
    private String firstName;

    @Column(nullable = false, length = 32)
    private String lastName;

    @Column(nullable = false, length = 320, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = true)
    @JsonIgnore
    private String token;

    @ManyToOne(optional = false)
    private UserRole userRole;

    @Builder.Default
    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private Set<Repo> repos = new LinkedHashSet<>();

    public UserEntity(UserRegisterDTO userRegisterDTO, UserRole userRole,
            PasswordEncoder passwordEncoder) {
        firstName = userRegisterDTO.getFirstName();
        lastName = userRegisterDTO.getLastName();
        email = userRegisterDTO.getEmail();
        password = passwordEncoder.encode(userRegisterDTO.getPassword());
        this.userRole = userRole;
    }

    @Override
    public boolean equals(@Nullable Object o) {
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
