package com.dsec.backend.model.github;

import org.hibernate.validator.constraints.URL;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

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
@JsonRootName(value = "repo")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "repos")
public class RepoDTO extends RepresentationModel<RepoDTO> {
    private Long id;
    @JsonAlias("full_name")
    private String fullName;
    @URL
    private String url;
    @JsonAlias("html_url")
    @URL
    private String htmlUrl;
    @JsonAlias("hooks_url")
    @URL
    private String hooksUrl;

    private String hookUrl;

    @JsonAlias("branches_url")
    @URL
    private String branchesUrl;
    @JsonAlias("clone_url")
    @URL
    private String cloneUrl;
}
