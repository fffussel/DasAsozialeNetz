package org.example.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.example.ui.client.PostClient;
import org.example.ui.client.UserClient;
import org.example.ui.components.NativeVideo;
import org.example.ui.components.UserInfoSmall;
import org.example.ui.client.LogicUrls;
import org.example.ui.security.AuthContext;
import org.example.ui.util.ByteArrayMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Route(value = "create", layout = MainLayout.class)
@PageTitle("Create post | WAVO")
public class CreatePostView extends VerticalLayout implements BeforeEnterObserver {

    private final PostClient postClient;

    private final TextArea caption = new TextArea();
    private final Div mediaPreview = new Div();
    private final MemoryBuffer buffer = new MemoryBuffer();

    private byte[] selectedFileBytes;
    private String selectedFileName;
    private String selectedContentType;

    @Autowired
    public CreatePostView(PostClient postClient, UserClient userClient, LogicUrls logicUrls) {
        this.postClient = postClient;

        setWidthFull();
        setAlignItems(Alignment.CENTER);

        Button upload = new Button("Upload", e -> submit());
        upload.setAutofocus(true);
        upload.addClassName("custom-button");

        Div postDiv = new Div();
        postDiv.addClassName("post");

        Div userDiv = new Div();
        userDiv.addClassName("post-user");
        AuthContext context = AuthContext.get();
        if (context.getUserId() != null) {
            userDiv.add(new UserInfoSmall(context.getUserId().toString(), userClient, logicUrls));
        }

        caption.addClassName("width-90p");

        Upload fileUpload = new Upload(buffer);
        fileUpload.setAcceptedFileTypes("image/*", "video/mp4");
        fileUpload.addSucceededListener(event -> {
            try {
                selectedFileBytes = buffer.getInputStream().readAllBytes();
                selectedFileName = event.getFileName();
                selectedContentType = event.getMIMEType();
                showPreview();
            } catch (IOException ex) {
                Notification.show("Could not read the uploaded file", 3000, Notification.Position.TOP_CENTER);
            }
        });

        Paragraph textWrapper = new Paragraph(caption);
        textWrapper.addClassName("post-text");

        postDiv.add(userDiv, textWrapper, fileUpload, mediaPreview);

        Paragraph note = new Paragraph("Please note that our service has a file size limit of 50MB");

        add(upload, postDiv, note);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!AuthContext.get().isLoggedIn()) {
            event.forwardTo(SignupView.class);
        }
    }

    private void showPreview() {
        mediaPreview.removeAll();
        String previewSrc = "data:" + selectedContentType + ";base64," +
                java.util.Base64.getEncoder().encodeToString(selectedFileBytes);
        if (selectedFileName != null && selectedFileName.endsWith(".mp4")) {
            NativeVideo video = new NativeVideo(previewSrc);
            video.addClassName("post-media");
            mediaPreview.add(video);
        } else {
            Image image = new Image(previewSrc, selectedFileName);
            image.addClassName("post-media");
            mediaPreview.add(image);
        }
    }

    private void submit() {
        AuthContext context = AuthContext.get();
        if (!context.isLoggedIn()) {
            return;
        }
        try {
            var media = selectedFileBytes != null
                    ? new ByteArrayMultipartFile("media", selectedFileName, selectedContentType, selectedFileBytes)
                    : null;
            postClient.newPost(context.bearerToken(), caption.getValue(), media);
            getUI().ifPresent(ui -> ui.navigate(""));
        } catch (Exception e) {
            Notification notification = Notification.show("Failed to create post: " + e.getMessage(), 4000, Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
