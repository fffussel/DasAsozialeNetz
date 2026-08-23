package org.example.ui.components;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.example.ui.client.LogicUrls;
import org.example.ui.client.UserClient;
import org.example.ui.security.AuthContext;

import java.util.List;

public class UserInfoBig extends HorizontalLayout {

    private final UserClient userClient;
    private final String username;

    private final Button followButton = new Button();
    private final Paragraph followerCount = new Paragraph();

    private boolean following;

    public UserInfoBig(String username, UserClient userClient, LogicUrls logicUrls) {
        this.userClient = userClient;
        this.username = username;

        addClassName("user-info");

        Avatar avatar = new Avatar(username);
        avatar.addClassName("user-info-avatar");
        avatar.addThemeVariants(AvatarVariant.LUMO_XLARGE);

        try {
            var user = userClient.getUser(AuthContext.get().bearerToken(), username);
            avatar.setImage(logicUrls.media(user.getProfilePicture()));
        } catch (Exception ignored) {
            // fall back to the initials-only avatar
        }

        H2 heading = new H2(username);

        followButton.addClickListener(e -> toggleFollow());

        AuthContext context = AuthContext.get();
        if (context.isLoggedIn()) {
            try {
                List<java.util.UUID> followers = userClient.getUserFollowers(username);
                following = followers.contains(context.getUserId());
                followerCount.setText("followers: " + followers.size());
            } catch (Exception ignored) {
                followerCount.setText("followers: 0");
            }
        } else {
            followButton.setEnabled(false);
            followerCount.setText("followers: 0");
        }
        applyFollowState();
        followerCount.addClassName("user-info-followers");

        add(avatar, heading, followButton, followerCount);
    }

    private void toggleFollow() {
        AuthContext context = AuthContext.get();
        if (!context.isLoggedIn()) {
            return;
        }
        try {
            userClient.toggleFollowUser(context.bearerToken(), username);
            List<java.util.UUID> followers = userClient.getUserFollowers(username);
            following = followers.contains(context.getUserId());
            followerCount.setText("followers: " + followers.size());
            applyFollowState();
        } catch (Exception ignored) {
            // leave state unchanged if the toggle request fails
        }
    }

    private void applyFollowState() {
        followButton.setText(following ? "unfollow" : "follow");
        followButton.removeClassName("follow-button");
        followButton.removeClassName("unfollow-button");
        followButton.addClassName(following ? "unfollow-button" : "follow-button");
    }
}
