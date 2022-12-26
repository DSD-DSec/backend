package com.dsec.backend.model.job;

import java.time.LocalDateTime;

import com.dsec.backend.entity.Repo;
import com.dsec.backend.entity.ToolEntity;
import com.dsec.backend.model.tools.BanditDTO;
import com.dsec.backend.model.tools.GitleaksDTO;
import com.dsec.backend.model.tools.GoKartDTO;
import com.dsec.backend.model.tools.ProgPilotDTO;
import com.dsec.backend.model.tools.flawfinder.Sarif210Rtm5;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class JobDTO<T> {
    private Long id;

    @Schema(oneOf = { Sarif210Rtm5.class, GitleaksDTO.class, BanditDTO.class, GoKartDTO.class, ProgPilotDTO.class })
    private T log;
    private Repo repo;
    private ToolEntity tool;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss:SSS")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss:SSS")
    private LocalDateTime endTime;

    private Boolean compliant;
}
