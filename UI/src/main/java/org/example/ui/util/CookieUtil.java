package org.example.ui.util;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import jakarta.servlet.http.Cookie;

public final class CookieUtil {

    private CookieUtil() {
    }

    public static String read(String name) {
        var request = VaadinRequest.getCurrent();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void write(String name, String value) {
        var response = VaadinResponse.getCurrent();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365);
        response.addCookie(cookie);
    }
}
