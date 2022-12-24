package com.dsec.backend.model.tools;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class RepoToolUpdateDTO {

    @Nullable
    private List<Integer> tools;
}
