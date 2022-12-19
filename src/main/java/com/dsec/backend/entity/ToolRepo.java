package com.dsec.backend.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToolRepo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tool_id", nullable = false)
    private ToolEntity selectedTool;

    @ManyToOne(optional = false)
    @JoinColumn(name = "repo_id", nullable = false)
    private Repo repo;

}
