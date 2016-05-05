package com.simon816.minecraft.tabchat.tabs;

import com.simon816.minecraft.tabchat.ITextDrawable;
import org.spongepowered.api.text.Text;

public abstract class Tab implements ITextDrawable {

    @Override
    public Text draw(int height) {
        Text.Builder builder = Text.builder();
        for (int i = 0; i < height; i++) {
            builder.append(Text.NEW_LINE);
        }
        return builder.build();
    }

    public abstract Text getTitle();

    public void onBlur() {
    }

    public void onFocus() {
    }

    public boolean hasCloseButton() {
        return true;
    }

    public void onClose() {
    }

}