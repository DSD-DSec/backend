package com.dsec.backend.DTO;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
//@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "users", itemRelation = "user")
public class UserHALDTO extends RepresentationModel<UserHALDTO> {

    @NotNull
    private Integer IdUser;

    @NotNull
    @Size(min = 1, message = "{Size.UserHALDTO.firstName}")
    private String firstName;

    @NotNull
    @Size(message = "{Size.UserHALDTO.lastName}", min = 1)
    private String lastName;

    @NotNull
    @Email(message = "{UserHALDTO.email}")
    private String email;

    @NotNull
    @Size(message = "{UserHALDTO.password}")
    private String password;
