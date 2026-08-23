package org.example.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.dom.Element;

public class NativeVideo extends Component implements HasStyle {

    public NativeVideo(String src) {
        super(new Element("video"));
        getElement().setAttribute("src", src);
        getElement().setAttribute("controls", true);
        getElement().setAttribute("width", "90%");
        getElement().setAttribute("height", "90%");
    }

    public void setLoop(boolean loop) {
        getElement().setAttribute("loop", loop);
    }
}
