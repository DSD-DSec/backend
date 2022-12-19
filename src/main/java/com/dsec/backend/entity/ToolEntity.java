package com.dsec.backend.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;

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
    @Column(nullable = false)
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


    public ToolEntity(Tool toolName, Integer userData, Integer security, Integer privacy, Language language) {
        this.toolName = toolName;
        this.userData = userData;
        this.security = security;
        this.privacy = privacy;
        this.language = language;
    }
}
