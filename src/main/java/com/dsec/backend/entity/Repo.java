package com.dsec.backend.entity;

import com.dsec.backend.exception.EntityMissingException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.lang.Nullable;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "repos")
public class Repo extends RepresentationModel<Repo> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long githubId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private RepoType type;

    @Column(nullable = false)
    private RepoDomain domain;

    @Column(nullable = false)
    private Boolean userData;

    @Column(nullable = false)
    private Integer security;

    @Column(nullable = false)
    private Integer availability;

    // Full name of the GitHub repository, in the form username/repository_name
    @Column(nullable = false, unique = true)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String repoName;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String htmlUrl;

    @Column(nullable = false)
    private String hooksUrl;

    @Column(nullable = false)
    private String hookUrl;

    @Column(nullable = false)
    private String branchesUrl;

    @Column(nullable = false)
    private String cloneUrl;

    @Column(nullable = false)
    private String defaultBranch;

    @Builder.Default
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "repo", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @ToString.Exclude
    private Set<UserRepo> userRepos = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "repo", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @ToString.Exclude
    private Set<Job> jobs = new LinkedHashSet<>();

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        Repo repo = (Repo) o;
        return id != null && Objects.equals(id, repo.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @JsonIgnore
    public UserEntity getOwner() {
        return this.getUserRepos().stream().filter(UserRepo::getIsOwner)
                .findAny().orElseThrow(() -> new EntityMissingException(UserRepo.class, "Owner userRepo"))
                .getUser();
    }
}
