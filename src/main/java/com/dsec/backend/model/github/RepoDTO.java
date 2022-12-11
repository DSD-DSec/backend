package com.dsec.backend.model.github;

import java.util.Objects;

import org.hibernate.validator.constraints.URL;
import org.springframework.lang.Nullable;

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
public class RepoDTO {
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

    @JsonAlias("branches_url")
    @URL
    private String branchesUrl;
    
    @JsonAlias("clone_url")
    @URL
    private String cloneUrl;

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        RepoDTO repoDTO = (RepoDTO) o;
        return id.equals(repoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
