package com.dsec.backend.model.repo;

import com.dsec.backend.entity.RepoDomain;
import com.dsec.backend.entity.RepoType;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class RepoUpdateDTO {

    @JsonAlias("full_name")
    @NotEmpty(message = "Please enter a repository name")
    private @NonNull String fullName;

    @JsonAlias("description")
    @NotBlank(message = "Please enter a description")
    private @NonNull String description;

    @JsonAlias("type")
    private RepoType type;

    @JsonAlias("domain")
    private RepoDomain domain;

    @JsonAlias("user_data")
    private Boolean userData;

    @JsonAlias("security")
    private Integer security;

    @JsonAlias("availability")
    private Integer availability;
}
