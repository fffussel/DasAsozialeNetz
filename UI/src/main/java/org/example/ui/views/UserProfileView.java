package org.example.ui.views;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.example.ui.client.LogicUrls;
import org.example.ui.client.MediaClient;
import org.example.ui.client.PostClient;
import org.example.ui.client.UserClient;
import org.example.ui.components.PostCard;
import org.example.ui.components.UserInfoBig;
import org.example.ui.security.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "u", layout = MainLayout.class)
@PageTitle("Profile | WAVO")
public class UserProfileView extends VerticalLayout implements HasUrlParameter<String> {

    private final PostClient postClient;
    private final UserClient userClient;
    private final MediaClient mediaClient;
    private final LogicUrls logicUrls;

    @Autowired
    public UserProfileView(PostClient postClient, UserClient userClient, MediaClient mediaClient, LogicUrls logicUrls) {
        this.postClient = postClient;
        this.userClient = userClient;
        this.mediaClient = mediaClient;
        this.logicUrls = logicUrls;
    }

    @Override
    public void setParameter(BeforeEvent event, String username) {
        if (!AuthContext.get().isLoggedIn()) {
            event.forwardTo(SignupView.class);
            return;
        }

        removeAll();
        setWidthFull();
        setAlignItems(Alignment.CENTER);

        add(new UserInfoBig(username, userClient, logicUrls));

        VerticalLayout postsLayout = new VerticalLayout();
        postsLayout.setWidthFull();
        postsLayout.setAlignItems(Alignment.CENTER);

        Div postList = new Div();
        postList.addClassName("post-list-end");
        try {
            var posts = postClient.getPostsForTimeline(AuthContext.get().bearerToken(), 10, 0, username, "createdAt", true, "");
            posts.forEach(post -> postList.add(new PostCard(post.getId(), post.getAuthor(), post.getMedia(),
                    post.getMessage(), postClient, userClient, mediaClient, logicUrls)));
        } catch (Exception ignored) {
            // no posts to show for this user
        }
        postsLayout.add(postList);

        add(postsLayout);
    }
}
