package com.dsec.backend.model.tools;

import java.util.List;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoKartDTO {

    private List<Result> results;

    @ToString
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Result {
        @JsonAlias("Type")
        private String type;
        @JsonAlias("Untrusted_Source")
        private List<Descriptor> untrustedSource;
        @JsonAlias("Vulnerable_Function")
        private Descriptor vulnerableFunction;

        @ToString
        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Descriptor {
            @JsonAlias("ParentFunction")
            private String parentFunction;
            @JsonAlias("SourceCode")
            private String sourceCode;
            @JsonAlias("SourceFilename")
            private String sourceFilename;
            @JsonAlias("SourceLineNum")
            private Long sourceLineNum;
        }
    }

}
