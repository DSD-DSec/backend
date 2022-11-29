package com.dsec.backend.service;

import com.dsec.backend.model.github.WebhookDTO;

public interface WebHookService {
    void webhook(WebhookDTO dto);
}
