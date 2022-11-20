package com.dsec.backend.model.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class LoginDTO {

    @NotEmpty(message = "Please enter an email")
    @Email(message = "Email is not valid")
    private @NonNull String email;

    @NotBlank(message = "Please enter in a password")
    private @NonNull String password;
}
