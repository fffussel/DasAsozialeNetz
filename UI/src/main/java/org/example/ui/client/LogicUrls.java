package org.example.ui.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LogicUrls {

    @Value("${logic.public.url}")
    private String publicUrl;

    public String media(UUID mediaId) {
        return mediaId == null ? null : publicUrl + "/api/media/" + mediaId;
    }

    public String media(String mediaId) {
        return mediaId == null || mediaId.isEmpty() ? null : publicUrl + "/api/media/" + mediaId;
    }
}
