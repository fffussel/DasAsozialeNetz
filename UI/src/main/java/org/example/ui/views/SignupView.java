package org.example.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.example.logic.dto.RegisterRequest;
import org.example.ui.client.AuthClient;
import org.example.ui.client.UserClient;
import org.example.ui.security.AuthContext;
import org.example.ui.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;

@Route("signup")
@PageTitle("Sign up | WAVO")
public class SignupView extends Div {

    private final AuthClient authClient;
    private final UserClient userClient;

    private final TextField username = new TextField("Username");
    private final EmailField email = new EmailField("Email address");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm password");

    @Autowired
    public SignupView(AuthClient authClient, UserClient userClient) {
        this.authClient = authClient;
        this.userClient = userClient;

        addClassName("login-rich-content");

        username.setAllowedCharPattern("[a-zA-Z0-9_&]");

        Button register = new Button("Sign up", event -> register());
        register.setAutofocus(true);

        Button haveAccount = new Button("I already have an account",
                event -> getUI().ifPresent(ui -> ui.navigate("login")));

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.add(username, email, password, confirmPassword, register, haveAccount);

        Card card = new Card();
        card.setTitle("Sign up");
        card.setWidth("300px");
        card.add(formLayout);

        String theme = CookieUtil.read("theme");
        if (theme != null) {
            card.getElement().getThemeList().add(theme);
        }

        add(card);
    }

    private void register() {
        if (!password.getValue().equals(confirmPassword.getValue())) {
            Notification notification = Notification.show("Passwords do not match", 3000, Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            var response = authClient.register(new RegisterRequest(username.getValue(), email.getValue(), password.getValue()));
            String token = response.getValue();

            AuthContext context = AuthContext.get();
            context.setToken(token);

            var self = userClient.getSelf(context.bearerToken());
            context.setUserId(self.getId());
            context.setUsername(self.getUsername());
            context.setRole(self.getRole());

            getUI().ifPresent(ui -> ui.navigate(""));
        } catch (Exception e) {
            Notification notification = Notification.show("Registration failed: " + e.getMessage(), 4000, Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
