package org.example.ui.client;

import org.example.logic.dto.LoginRequest;
import org.example.logic.dto.RegisterRequest;
import org.example.logic.dto.SingleStringDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "logic-auth", url = "${logic.service.url}")
public interface AuthClient {

    @PostMapping("/api/auth/register")
    SingleStringDTO register(@RequestBody RegisterRequest registerRequest);

    @PostMapping("/api/auth/login-json")
    SingleStringDTO login(@RequestBody LoginRequest loginRequest);
}
