package com.dsec.backend.model.repo;

import javax.validation.constraints.NotEmpty;

import com.dsec.backend.entity.Language;
import com.dsec.backend.entity.RepoDomain;
import com.dsec.backend.entity.RepoType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRepoDTO {
    @NotEmpty(message = "Please enter a repository name")
    private String repoName;

    @NotEmpty(message = "Please enter a description")
    private String description;

    private RepoType type;

    private RepoDomain domain;

    private Boolean userData;

    private Integer security;

    private Integer privacy;

    private Language language;
}
