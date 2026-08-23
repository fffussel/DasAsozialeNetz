package org.example.ui.components;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.example.ui.client.LogicUrls;
import org.example.ui.client.UserClient;
import org.example.ui.security.AuthContext;

public class UserInfoSmall extends HorizontalLayout {

    public UserInfoSmall(String userIdOrName, UserClient userClient, LogicUrls logicUrls) {
        String username = "placeholder";
        String profilePicture = "https://cdn-icons-png.flaticon.com/512/149/149071.png";

        if (userIdOrName != null && !userIdOrName.isBlank()) {
            try {
                var user = userClient.getUser(AuthContext.get().bearerToken(), userIdOrName);
                username = user.getUsername();
                String media = logicUrls.media(user.getProfilePicture());
                if (media != null) {
                    profilePicture = media;
                }
            } catch (Exception ignored) {
                // keep placeholder values if the user can't be resolved
            }
        }

        Avatar avatar = new Avatar(username, profilePicture);
        avatar.addClassName("mr-s");
        avatar.setColorIndex(5);

        Paragraph paragraph = new Paragraph();
        paragraph.add(avatar, new Text(" " + username));

        Anchor link = new Anchor("/u/" + username, paragraph);
        add(link);
    }
}
