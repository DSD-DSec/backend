package com.dsec.backend.model.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import com.dsec.backend.validation.UniqueEmail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class UserUpdateDTO {

    @NotEmpty(message = "Please enter an email")
    @Email(message = "Email is not valid")
    @UniqueEmail
    private @NonNull String email;

    @NotBlank(message = "Please enter your first name")
    @Pattern(regexp = "^[\\p{L}]{1,32}$", message = "Please use only letters in your first name")
    private @NonNull String firstName;

    @NotBlank(message = "Please enter your lastname")
    @Pattern(regexp = "^[\\p{L}]{1,32}$", message = "Please use only letters in your last name")
    private @NonNull String lastName;

}
