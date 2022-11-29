package com.dsec.backend.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Repo {
    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fullName;

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
}
