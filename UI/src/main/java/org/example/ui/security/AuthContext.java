package org.example.ui.security;

import com.vaadin.flow.server.VaadinSession;

import java.io.Serializable;
import java.util.UUID;

public final class AuthContext implements Serializable {

    private String token;
    private UUID userId;
    private String username;
    private String role;

    public static AuthContext get() {
        VaadinSession session = VaadinSession.getCurrent();
        AuthContext context = session.getAttribute(AuthContext.class);
        if (context == null) {
            context = new AuthContext();
            session.setAttribute(AuthContext.class, context);
        }
        return context;
    }

    public boolean isLoggedIn() {
        return token != null;
    }

    public String bearerToken() {
        return "Bearer " + token;
    }

    public void clear() {
        token = null;
        userId = null;
        username = null;
        role = null;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
