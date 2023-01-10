package com.dsec.backend.model.tools;

import java.util.List;
import java.util.Map;

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
public class BanditDTO {
    private List<Error> errors;
    private Map<String, Metric> metrics;
    private List<Result> results;

    @ToString
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Error {
        private String filename;
        private String reason;
    }

    @ToString
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Metric {
        @JsonAlias("CONFIDENCE.HIGH")
        private Long confidenceHigh;
        @JsonAlias("CONFIDENCE.LOW")
        private Long confidenceLow;
        @JsonAlias("CONFIDENCE.MEDIUM")
        private Long confidenceMedium;
        @JsonAlias("CONFIDENCE.UNDEFINED")
        private Long confidenceUndefined;

        @JsonAlias("SEVERITY.HIGH")
        private Long severityHigh;
        @JsonAlias("SEVERITY.LOW")
        private Long severityLow;
        @JsonAlias("SEVERITY.MEDIUM")
        private Long severityMedium;
        @JsonAlias("SEVERITY.UNDEFINED")
        private Long severityUndefined;

        private Long loc;
        private Long nosec;
        @JsonAlias("skipped_tests")
        private Long skippedTests;
    }

    @ToString
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Result {
        private String code;
        @JsonAlias("col_offset")
        private Long colOffset;
        private String filename;
        @JsonAlias("issue_confidence")
        private String issueConfidence;
        @JsonAlias("issue_cwe")
        private IssueCwe issueCwe;
        @JsonAlias("issue_severity")
        private String issueSeverity;
        @JsonAlias("issue_text")
        private String issueText;
        @JsonAlias("line_number")
        private Long lineNumber;
        @JsonAlias("line_range")
        private List<Long> lineRange;
        @JsonAlias("more_info")
        private String moreInfo;
        @JsonAlias("test_id")
        private String testId;
        @JsonAlias("test_name")
        private String testName;

        @ToString
        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class IssueCwe {
            private Long id;
            private String link;
        }
    }
}
