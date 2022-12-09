package com.dsec.backend.entity;

import java.util.Set;

import javax.persistence.*;

import com.dsec.backend.model.github.RepoDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

@Entity
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonRootName(value = "repo")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Repo extends RepresentationModel<Repo> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long owner;

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

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "repo_users", joinColumns = @JoinColumn(name = "repo_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserEntity> users = new java.util.LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Repo repo = (Repo) o;
        return id != null && Objects.equals(id, repo.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
