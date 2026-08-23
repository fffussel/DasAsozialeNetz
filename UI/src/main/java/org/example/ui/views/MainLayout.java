package org.example.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.example.ui.client.LogicUrls;
import org.example.ui.client.UserClient;
import org.example.ui.security.AuthContext;
import org.example.ui.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class MainLayout extends AppLayout {

    private final UserClient userClient;
    private final LogicUrls logicUrls;

    @Autowired
    public MainLayout(UserClient userClient, LogicUrls logicUrls) {
        this.userClient = userClient;
        this.logicUrls = logicUrls;

        setPrimarySection(Section.NAVBAR);
        setDrawerOpened(false);
        addToNavbar(buildHeader());
        getElement().appendChild(buildHud().getElement());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        String theme = CookieUtil.read("theme");
        attachEvent.getUI().getPage().executeJs(
                "document.documentElement.setAttribute('theme', $0)", theme != null ? theme : "light");
    }

    private HorizontalLayout buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("navbar");
        header.getElement().setAttribute("slot", "navbar");

        AuthContext context = AuthContext.get();

        Avatar avatar = new Avatar();
        avatar.addClassName("navbar-item");
        Icon settingsIcon = new Icon(VaadinIcon.COG_O);
        settingsIcon.addClassName("navbar-item");
        Icon exitIcon = new Icon(VaadinIcon.EXIT_O);
        exitIcon.addClassName("navbar-item");

        if (context.isLoggedIn()) {
            try {
                var self = userClient.getSelf(context.bearerToken());
                avatar.setName(self.getUsername());
                avatar.setImage(logicUrls.media(self.getProfilePicture()));
                avatar.getElement().addEventListener("click",
                        e -> getUI().ifPresent(ui -> ui.navigate("u/" + self.getUsername())));
            } catch (Exception ignored) {
                // stay on the generic avatar if the profile can't be loaded
            }
        }

        Anchor homeLink = new Anchor("/", new H1("WAVO"));
        homeLink.getStyle().set("text-decoration", "none");

        settingsIcon.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("settings")));
        exitIcon.addClickListener(e -> {
            AuthContext.get().clear();
            getUI().ifPresent(ui -> ui.navigate("login"));
        });

        Div icons = new Div(settingsIcon, exitIcon);

        header.add(avatar, homeLink, icons);
        return header;
    }

    private Div buildHud() {
        Div hud = new Div();
        hud.addClassName("hud");

        HorizontalLayout navBoard = new HorizontalLayout();
        navBoard.addClassName("nav-board");

        Anchor home = new Anchor("/", new Icon(VaadinIcon.HOME));
        Anchor search = new Anchor("/search", new Icon(VaadinIcon.SEARCH));
        Anchor create = new Anchor("/create", new Icon(VaadinIcon.PLUS_CIRCLE_O));

        navBoard.add(home, search, create);
        hud.add(navBoard);
        return hud;
    }
}
