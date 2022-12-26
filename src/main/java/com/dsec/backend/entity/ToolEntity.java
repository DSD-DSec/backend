package com.dsec.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
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
@JsonRootName(value = "tool")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "tools")
public class ToolEntity extends RepresentationModel<ToolEntity> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Tool toolName;

    @Column(nullable = false)
    private Integer userData;

    @Column(nullable = false)
    private Integer security;

    @Column(nullable = false)
    private Integer privacy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @OneToMany(mappedBy = "tool", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    private Set<ToolRepo> toolRepos = new LinkedHashSet<>();

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        ToolEntity that = (ToolEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public ToolEntity(Tool toolName, Integer userData, Integer security, Integer privacy, Language language) {
        this.toolName = toolName;
        this.userData = userData;
        this.security = security;
        this.privacy = privacy;
        this.language = language;
    }
}
