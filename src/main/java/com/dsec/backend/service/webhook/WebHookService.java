package com.dsec.backend.service.webhook;

import com.dsec.backend.model.github.WebhookDTO;

public interface WebHookService {
    void webhook(WebhookDTO dto);
}
