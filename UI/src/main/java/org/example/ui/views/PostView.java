package org.example.ui.views;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.example.ui.client.LogicUrls;
import org.example.ui.client.MediaClient;
import org.example.ui.client.PostClient;
import org.example.ui.client.UserClient;
import org.example.ui.components.NewComment;
import org.example.ui.components.PostCard;
import org.example.ui.security.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Route(value = "post", layout = MainLayout.class)
@PageTitle("Post | WAVO")
public class PostView extends VerticalLayout implements HasUrlParameter<String> {

    private final PostClient postClient;
    private final UserClient userClient;
    private final MediaClient mediaClient;
    private final LogicUrls logicUrls;

    private final Div postContainer = new Div();
    private final Div commentsContainer = new Div();
    private final Paragraph noComments = new Paragraph("nothing to see here");

    private UUID postId;

    @Autowired
    public PostView(PostClient postClient, UserClient userClient, MediaClient mediaClient, LogicUrls logicUrls) {
        this.postClient = postClient;
        this.userClient = userClient;
        this.mediaClient = mediaClient;
        this.logicUrls = logicUrls;

        setWidthFull();
        setAlignItems(Alignment.CENTER);
        noComments.addClassName("post-list-end");
        commentsContainer.addClassName("post-list-end");
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            postId = UUID.fromString(parameter);
        } catch (IllegalArgumentException e) {
            event.forwardTo(TimelineView.class);
            return;
        }
        removeAll();

        loadPost();

        add(postContainer, new Hr(), new NewComment(postId, postClient, this::loadComments), new Hr(),
                new H4(new Text("comments "), new Icon(VaadinIcon.COMMENTS_O)));

        loadComments();
        add(commentsContainer, noComments);
    }

    private void loadPost() {
        postContainer.removeAll();
        try {
            var post = postClient.getPost(AuthContext.get().bearerToken(), postId);
            postContainer.add(new PostCard(post.getId(), post.getAuthor(), post.getMedia(), post.getMessage(),
                    postClient, userClient, mediaClient, logicUrls));
        } catch (Exception ignored) {
            // the post could not be loaded
        }
    }

    private void loadComments() {
        commentsContainer.removeAll();
        try {
            var comments = postClient.getCommentsForPost(AuthContext.get().bearerToken(), postId, 20, null, "points", true, "");
            comments.forEach(comment -> commentsContainer.add(new PostCard(
                    comment.getId(), comment.getAuthor(), comment.getMedia(), comment.getMessage(),
                    postClient, userClient, mediaClient, logicUrls)));
            noComments.setVisible(comments.isEmpty());
            commentsContainer.setVisible(!comments.isEmpty());
        } catch (Exception ignored) {
            noComments.setVisible(true);
            commentsContainer.setVisible(false);
        }
    }
}
