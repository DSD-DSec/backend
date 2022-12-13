package com.dsec.backend.model.github;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GetWebhookDTO {
    private String name;
    private boolean active;
    private List<String> events;
    private Map<String, String> config;
    private String url;
}
