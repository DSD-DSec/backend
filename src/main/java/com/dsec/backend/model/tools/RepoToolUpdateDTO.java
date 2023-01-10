package com.dsec.backend.model.tools;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode()
@NoArgsConstructor
public class RepoToolUpdateDTO {

    @NotNull
    private List<Integer> tools;
}
