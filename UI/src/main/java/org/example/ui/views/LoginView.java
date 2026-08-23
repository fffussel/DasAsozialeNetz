package org.example.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.example.logic.dto.LoginRequest;
import org.example.ui.client.AuthClient;
import org.example.ui.client.UserClient;
import org.example.ui.security.AuthContext;
import org.example.ui.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;

@Route("login")
@PageTitle("Log in | WAVO")
public class LoginView extends Div {

    private final AuthClient authClient;
    private final UserClient userClient;

    @Autowired
    public LoginView(AuthClient authClient, UserClient userClient) {
        this.authClient = authClient;
        this.userClient = userClient;

        addClassName("login-rich-content");

        LoginForm loginForm = new LoginForm();
        loginForm.setForgotPasswordButtonVisible(false);
        String theme = CookieUtil.read("theme");
        if (theme != null) {
            loginForm.getElement().getThemeList().add(theme);
        }
        loginForm.addLoginListener(event -> login(loginForm, event.getUsername(), event.getPassword()));

        Button signUp = new Button("Don't have an account? Sign up",
                event -> getUI().ifPresent(ui -> ui.navigate("signup")));

        add(loginForm, signUp);
    }

    private void login(LoginForm loginForm, String username, String password) {
        try {
            var response = authClient.login(new LoginRequest(username, password));
            String token = response.getValue();

            AuthContext context = AuthContext.get();
            context.setToken(token);

            var self = userClient.getSelf(context.bearerToken());
            context.setUserId(self.getId());
            context.setUsername(self.getUsername());
            context.setRole(self.getRole());

            getUI().ifPresent(ui -> ui.navigate(""));
        } catch (Exception e) {
            loginForm.setError(true);
        }
    }

}
