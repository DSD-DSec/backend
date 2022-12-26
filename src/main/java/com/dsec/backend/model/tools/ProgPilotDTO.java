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
public class ProgPilotDTO {

    private List<Result> results;

    @ToString
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Result {
        @JsonAlias("sink_column")
        private Long sinkColumn;
        @JsonAlias("sink_file")
        private String sinkFile;
        @JsonAlias("sink_line")
        private Long sinkLine;
        @JsonAlias("sink_name")
        private String sinkName;
        @JsonAlias("source_column")
        private List<Long> sourceColumn;
        @JsonAlias("source_file")
        private List<String> sourceFile;
        @JsonAlias("source_line")
        private List<Long> sourceLine;
        @JsonAlias("source_name")
        private List<String> sourceName;
        @JsonAlias("vuln_cwe")
        private String vulnCwe;
        @JsonAlias("vuln_id")
        private String vulnId;
        @JsonAlias("vuln_name")
        private String vulnName;
        @JsonAlias("vuln_type")
        private String vulnType;
    }

}
