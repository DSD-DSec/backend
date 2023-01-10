package com.dsec.backend.model.github;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateWebhook {
    private String name;
    private boolean active;
    private List<String> events;
    private Map<String, String> config;
}
