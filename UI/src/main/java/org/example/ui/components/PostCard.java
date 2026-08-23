package org.example.ui.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.example.ui.client.LogicUrls;
import org.example.ui.client.MediaClient;
import org.example.ui.client.PostClient;
import org.example.ui.client.UserClient;
import org.example.ui.security.AuthContext;

import java.util.UUID;

public class PostCard extends VerticalLayout {

    public PostCard(UUID id, UUID author, UUID media, String caption,
                     PostClient postClient, UserClient userClient, MediaClient mediaClient, LogicUrls logicUrls) {
        addClassName("post");

        Div userDiv = new Div();
        userDiv.addClassName("post-user");
        userDiv.add(new UserInfoSmall(author != null ? author.toString() : null, userClient, logicUrls));

        Paragraph text = new Paragraph(caption);
        text.addClassName("post-text");

        add(userDiv, text);

        if (media != null) {
            String src = logicUrls.media(media);
            boolean isVideo = false;
            try {
                isVideo = mediaClient.getMediaType(media.toString()).endsWith(".mp4");
            } catch (Exception ignored) {
                // fall back to rendering as an image
            }

            if (isVideo) {
                NativeVideo video = new NativeVideo(src);
                video.setLoop(true);
                video.addClassName("post-media");
                video.getElement().addEventListener("click", e -> navigateToPost(id));
                add(video);
            } else {
                Image image = new Image(src, caption);
                image.addClassName("post-media");
                image.addClickListener(e -> navigateToPost(id));
                add(image);
            }
        }

        int likeCount = 0;
        int commentCount = 0;
        boolean liked = false;
        try {
            var post = postClient.getPost(AuthContext.get().bearerToken(), id);
            likeCount = post.getLikes().size();
            commentCount = post.getComments().size();
            liked = AuthContext.get().getUserId() != null && post.getLikes().contains(AuthContext.get().getUserId());
        } catch (Exception ignored) {
            // show zero counts if the post can't be reloaded
        }

        Icon likeIcon = new Icon(liked ? VaadinIcon.THUMBS_UP : VaadinIcon.THUMBS_UP_O);
        likeIcon.addClassName("post-interaction");
        Icon commentIcon = new Icon(VaadinIcon.COMMENT_O);
        commentIcon.addClassName("post-interaction");

        Span likeCountLabel = new Span(String.valueOf(likeCount));

        boolean[] likedHolder = {liked};
        int[] likeCountHolder = {likeCount};
        likeIcon.addClickListener(e -> {
            AuthContext context = AuthContext.get();
            if (!context.isLoggedIn()) {
                return;
            }
            likedHolder[0] = !likedHolder[0];
            likeCountHolder[0] += likedHolder[0] ? 1 : -1;
            VaadinIcon nextIcon = likedHolder[0] ? VaadinIcon.THUMBS_UP : VaadinIcon.THUMBS_UP_O;
            likeIcon.getElement().setAttribute("icon", nextIcon.create().getElement().getAttribute("icon"));
            likeCountLabel.setText(String.valueOf(likeCountHolder[0]));
            try {
                postClient.toggleLike(context.bearerToken(), id);
            } catch (Exception ignored) {
                // optimistic update stays even if the request fails silently
            }
        });

        HorizontalLayout interactions = new HorizontalLayout();
        interactions.addClassName("post-interaction-container");
        interactions.add(likeIcon, likeCountLabel, commentIcon, new Span(String.valueOf(commentCount)));

        add(interactions);
    }

    private void navigateToPost(UUID id) {
        getUI().ifPresent(ui -> ui.navigate("post/" + id));
    }
}
