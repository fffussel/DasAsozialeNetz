package org.example.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import org.example.ui.client.PostClient;
import org.example.ui.security.AuthContext;
import org.example.ui.util.ByteArrayMultipartFile;

import java.io.IOException;
import java.util.UUID;

public class NewComment extends HorizontalLayout {

    private final MemoryBuffer buffer = new MemoryBuffer();
    private byte[] selectedFileBytes;
    private String selectedFileName;
    private String selectedContentType;

    public NewComment(UUID postId, PostClient postClient, Runnable onSubmitted) {
        addClassName("message-container");

        Button showUpload = new Button(new Icon(VaadinIcon.PLUS));
        showUpload.addThemeVariants(ButtonVariant.LUMO_ICON);
        showUpload.addClassName("message-button");
        showUpload.setId("show-upload-" + postId);

        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/*", "video/mp4");
        upload.addSucceededListener(event -> {
            try {
                selectedFileBytes = buffer.getInputStream().readAllBytes();
                selectedFileName = event.getFileName();
                selectedContentType = event.getMIMEType();
            } catch (IOException ex) {
                Notification.show("Could not read the uploaded file", 3000, Notification.Position.TOP_CENTER);
            }
        });

        Popover popover = new Popover(new VerticalLayout(upload));
        popover.setFor(showUpload.getId().orElse(null));
        popover.setPosition(com.vaadin.flow.component.popover.PopoverPosition.TOP);

        TextField message = new TextField();
        message.addClassName("message-input");
        message.setPlaceholder("Write a comment...");
        message.setMaxLength(1000);

        Button send = new Button(new Icon(VaadinIcon.PAPERPLANE_O));
        send.addThemeVariants(ButtonVariant.LUMO_ICON);
        send.addClassName("message-button");
        send.setAutofocus(true);
        send.addClickListener(e -> {
            AuthContext context = AuthContext.get();
            if (!context.isLoggedIn()) {
                return;
            }
            try {
                var media = selectedFileBytes != null
                        ? new ByteArrayMultipartFile("media", selectedFileName, selectedContentType, selectedFileBytes)
                        : null;
                postClient.newComment(context.bearerToken(), postId, message.getValue(), media);
                message.clear();
                selectedFileBytes = null;
                onSubmitted.run();
            } catch (Exception ex) {
                Notification.show("Failed to add comment: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });

        add(showUpload, popover, message, send);
    }
}
