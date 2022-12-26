package com.dsec.backend.model.tools;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitleaksDTO {

    private List<Result> results;

    @ToString
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonAlias("Commit")
        private String commit;
        @JsonAlias("Author")
        private String author;
        @JsonAlias("Date")
        private String date;
        @JsonAlias("Description")
        private String description;
        @JsonAlias("Email")
        private String email;
        @JsonAlias("EndColumn")
        private Integer endColumn;
        @JsonAlias("EndLine")
        private Integer endLine;
        @JsonAlias("Entropy")
        private Double entropy;
        @JsonAlias("File")
        private String file;
        @JsonAlias("Fingerprint")
        private String fingerprint;
        @JsonAlias("Match")
        private String match;
        @JsonAlias("Message")
        private String message;
        @JsonAlias("RuleID")
        private String ruleID;
        @JsonAlias("Secret")
        private String secret;
        @JsonAlias("StartColumn")
        private Integer startColumn;
        @JsonAlias("StartLine")
        private String startLine;
        @JsonAlias("SymlinkFile")
        private String symlinkFile;
    }
}
