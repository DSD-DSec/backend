package com.dsec.backend.model.github;

import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;

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
@JsonInclude(JsonInclude.Include.ALWAYS)
public class UserDTO {

    private String login;

    private Long id;

    @URL
    private String url;

    @JsonAlias("html_url")
    @URL
    private String htmlUrl;

    @JsonAlias("organizations_url")
    @URL
    private String organizationsUrl;

    @JsonAlias("repos_url")
    @URL
    private String reposUrl;

    private String name;

    private String email;

    private String location;
}
