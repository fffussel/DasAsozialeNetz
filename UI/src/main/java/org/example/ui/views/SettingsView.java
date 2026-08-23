package org.example.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.example.ui.client.LogicUrls;
import org.example.ui.client.UserClient;
import org.example.ui.components.UserInfoSmall;
import org.example.ui.security.AuthContext;
import org.example.ui.util.ByteArrayMultipartFile;
import org.example.ui.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings | WAVO")
public class SettingsView extends VerticalLayout implements BeforeEnterObserver {

    private final UserClient userClient;

    @Autowired
    public SettingsView(UserClient userClient, LogicUrls logicUrls) {
        this.userClient = userClient;

        setWidthFull();
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("Settings");
        title.addClassName("page-title");
        add(title);

        RadioButtonGroup<String> theme = new RadioButtonGroup<>();
        theme.setLabel("Theme");
        theme.setItems("dark", "light");
        theme.setValue(CookieUtil.read("theme") != null ? CookieUtil.read("theme") : "light");
        theme.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                CookieUtil.write("theme", e.getValue());
                getUI().ifPresent(ui -> ui.getPage().executeJs(
                        "document.documentElement.setAttribute('theme', $0)", e.getValue()));
            }
        });
        HorizontalLayout themeContainer = new HorizontalLayout(theme);
        themeContainer.addClassName("option-container");

        add(themeContainer, new Hr());

        AuthContext context = AuthContext.get();
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setAlignItems(Alignment.CENTER);
        if (context.getUserId() != null) {
            content.add(new UserInfoSmall(context.getUserId().toString(), userClient, logicUrls));
        }

        content.add(new H3("Choose your profile picture"));
        MemoryBuffer buffer = new MemoryBuffer();
        Upload profilePictureUpload = new Upload(buffer);
        profilePictureUpload.setAcceptedFileTypes("image/*");
        profilePictureUpload.addSucceededListener(event -> {
            try {
                byte[] bytes = buffer.getInputStream().readAllBytes();
                var file = new ByteArrayMultipartFile("file", event.getFileName(), event.getMIMEType(), bytes);
                userClient.changeProfilePicture(context.bearerToken(), file);
                getUI().ifPresent(ui -> ui.navigate("settings"));
            } catch (IOException ex) {
                com.vaadin.flow.component.notification.Notification.show(
                        "Failed to upload profile picture", 3000,
                        com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER);
            }
        });
        content.add(profilePictureUpload, new Hr());

        content.add(new H3("Default sorting type"));
        ComboBox<SortMode> sortBox = new ComboBox<>("Sort by");
        sortBox.setItems(SortMode.points, SortMode.createdAt, SortMode.lastEditedAt);
        sortBox.setItemLabelGenerator(mode -> switch (mode) {
            case points -> "Popularity";
            case createdAt -> "Newest";
            case lastEditedAt -> "Recently edited";
        });
        String savedSort = CookieUtil.read("sort");
        sortBox.setValue(savedSort != null ? SortMode.valueOf(savedSort) : SortMode.points);
        sortBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                CookieUtil.write("sort", e.getValue().name());
            }
        });
        HorizontalLayout sortContainer = new HorizontalLayout(sortBox);
        sortContainer.setWidthFull();
        sortContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        content.add(sortContainer, new Hr());

        content.add(new H3("Delete account"));
        Button deleteButton = new Button("Delete", new Icon(VaadinIcon.ERASER));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(e -> {
            try {
                userClient.deleteSelf(context.bearerToken());
            } catch (Exception ignored) {
                // proceed to sign the user out even if the request failed
            }
            context.clear();
            getUI().ifPresent(ui -> ui.navigate("signup"));
        });
        HorizontalLayout deleteContainer = new HorizontalLayout(deleteButton);
        deleteContainer.setWidthFull();
        deleteContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        content.add(deleteContainer);

        add(content);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!AuthContext.get().isLoggedIn()) {
            event.forwardTo(SignupView.class);
        }
    }
}
