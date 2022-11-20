package com.dsec.backend.model.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.dsec.backend.entity.UserEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.hateoas.RepresentationModel;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class LoginDTO extends RepresentationModel<UserEntity> {

    @NotEmpty(message = "Please enter an email")
    @Email(message = "Email is not valid")
    private @NonNull String email;

    @NotBlank(message = "Please enter in a password")
    private @NonNull String password;
}
