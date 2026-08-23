package org.example.ui.views;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.example.ui.client.LogicUrls;
import org.example.ui.client.MediaClient;
import org.example.ui.client.PostClient;
import org.example.ui.client.UserClient;
import org.example.ui.components.PostCard;
import org.example.ui.components.UserInfoSmall;
import org.example.ui.security.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "search", layout = MainLayout.class)
@PageTitle("Search | WAVO")
public class SearchView extends VerticalLayout implements HasUrlParameter<String> {

    private final PostClient postClient;
    private final UserClient userClient;
    private final MediaClient mediaClient;
    private final LogicUrls logicUrls;

    private final TextField searchBar = new TextField();

    @Autowired
    public SearchView(PostClient postClient, UserClient userClient, MediaClient mediaClient, LogicUrls logicUrls) {
        this.postClient = postClient;
        this.userClient = userClient;
        this.mediaClient = mediaClient;
        this.logicUrls = logicUrls;

        setWidthFull();
        setAlignItems(Alignment.CENTER);

        searchBar.setPlaceholder("Search");
        searchBar.addClassName("search-bar");
        searchBar.setAllowedCharPattern("[a-zA-Z0-9_&]");
        searchBar.addKeyDownListener(com.vaadin.flow.component.Key.ENTER,
                e -> getUI().ifPresent(ui -> ui.navigate("search/" + searchBar.getValue())));

        add(searchBar);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String param) {
        removeAll();
        add(searchBar);

        Div results = new Div();
        results.addClassName("search_results");

        Accordion accordion = new Accordion();
        accordion.addClassName("full_width_accordion");

        VerticalLayout users = new VerticalLayout();
        users.setWidthFull();
        users.setAlignItems(Alignment.CENTER);
        if (param != null && !param.isBlank()) {
            try {
                var found = userClient.searchUser(AuthContext.get().bearerToken(), param);
                if (found.isEmpty()) {
                    users.add(new Paragraph("No users found"));
                } else {
                    found.forEach(user -> {
                        users.add(new UserInfoSmall(user.getId().toString(), userClient, logicUrls));
                        users.add(new Hr());
                    });
                }
            } catch (Exception ignored) {
                users.add(new Paragraph("No users found"));
            }
        } else {
            users.add(new Paragraph("No users found"));
        }

        VerticalLayout posts = new VerticalLayout();
        posts.setWidthFull();
        posts.setAlignItems(Alignment.CENTER);
        if (param != null && !param.isBlank()) {
            try {
                var found = postClient.getPostsForTimeline(AuthContext.get().bearerToken(), 10, 0, null, "points", true, param);
                if (found.isEmpty()) {
                    posts.add(new Paragraph("No posts found"));
                } else {
                    found.forEach(post -> posts.add(new PostCard(post.getId(), post.getAuthor(), post.getMedia(),
                            post.getMessage(), postClient, userClient, mediaClient, logicUrls)));
                }
            } catch (Exception ignored) {
                posts.add(new Paragraph("No posts found"));
            }
        } else {
            posts.add(new Paragraph("No posts found"));
        }

        accordion.add("Users", users).addThemeVariants(com.vaadin.flow.component.details.DetailsVariant.FILLED);
        accordion.add("Posts", posts).addThemeVariants(com.vaadin.flow.component.details.DetailsVariant.FILLED);
        results.add(accordion);

        add(results);
    }
}
