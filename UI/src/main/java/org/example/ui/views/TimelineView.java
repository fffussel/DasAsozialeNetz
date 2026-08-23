package org.example.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.example.ui.client.LogicUrls;
import org.example.ui.client.MediaClient;
import org.example.ui.client.PostClient;
import org.example.ui.client.UserClient;
import org.example.ui.components.PostCard;
import org.example.ui.security.AuthContext;
import org.example.ui.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "", layout = MainLayout.class)
@PageTitle("WAVO")
public class TimelineView extends VerticalLayout implements BeforeEnterObserver {

    private final PostClient postClient;
    private final UserClient userClient;
    private final MediaClient mediaClient;
    private final LogicUrls logicUrls;

    private final Div postList = new Div();
    private int page = 0;
    private SortMode sortMode;

    @Autowired
    public TimelineView(PostClient postClient, UserClient userClient, MediaClient mediaClient, LogicUrls logicUrls) {
        this.postClient = postClient;
        this.userClient = userClient;
        this.mediaClient = mediaClient;
        this.logicUrls = logicUrls;

        setWidthFull();
        setAlignItems(Alignment.CENTER);

        String savedSort = CookieUtil.read("sort");
        sortMode = savedSort != null ? SortMode.valueOf(savedSort) : SortMode.points;

        ComboBox<SortMode> sortBox = new ComboBox<>("Sort by");
        sortBox.setItems(SortMode.points, SortMode.createdAt, SortMode.lastEditedAt);
        sortBox.setItemLabelGenerator(mode -> switch (mode) {
            case points -> "Popularity";
            case createdAt -> "Newest";
            case lastEditedAt -> "Recently edited";
        });
        sortBox.setValue(sortMode);
        sortBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                sortMode = e.getValue();
                page = 0;
                postList.removeAll();
                loadPage();
            }
        });

        Button loadMore = new Button("Load more", e -> {
            page++;
            loadPage();
        });
        loadMore.addClassName("post-list-end");

        HorizontalLayout sortRow = new HorizontalLayout(sortBox);
        HorizontalLayout buttonWrapper = new HorizontalLayout(loadMore);
        buttonWrapper.addClassName("button-wrapper");

        add(sortRow, postList, buttonWrapper);
        loadPage();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!AuthContext.get().isLoggedIn()) {
            event.forwardTo(SignupView.class);
        }
    }

    private void loadPage() {
        try {
            var posts = postClient.getPostsForTimeline(AuthContext.get().bearerToken(), 10, page, null, sortMode.name(), true, "");
            posts.forEach(post -> postList.add(new PostCard(
                    post.getId(), post.getAuthor(), post.getMedia(), post.getMessage(),
                    postClient, userClient, mediaClient, logicUrls)));
        } catch (Exception ignored) {
            // no more posts to show, or the timeline request failed
        }
    }
}
