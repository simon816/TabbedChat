package com.simon816.chatui.tabs.config;

import com.simon816.chatui.ChatUI;
import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.ui.table.TableScrollHelper;
import com.simon816.chatui.ui.table.TableUI;
import com.simon816.chatui.util.TextUtils;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.BooleanSupplier;

public class ConfigEditTab extends Tab {

    boolean isTabActive(CommandSource src) {
        return ChatUI.getActiveView(src).getWindow().getActiveTab() == this;
    }

    ClickAction<?> clickAction(Runnable action) {
        return clickAction(() -> {
            action.run();
            return true;
        });
    }

    ClickAction<?> clickAction(BooleanSupplier action) {
        return ChatUI.execClick(src -> {
            if (!isTabActive(src)) {
                return;
            }
            if (action.getAsBoolean()) {
                ChatUI.getView(src).update();
            }
        });
    }

    private class ButtonBar implements UIComponent {

        public ButtonBar() {
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            Text.Builder builder = Text.builder();
            if (ConfigEditTab.this.control.getActiveEntry() == null) {
                builder.append(Text.of(clickAction(ConfigEditTab.this.scroll::scrollUp),
                        ConfigEditTab.this.scroll.canScrollUp() ? TextColors.WHITE : TextColors.DARK_GRAY, "[Scroll Up] "));
                builder.append(Text.of(clickAction(ConfigEditTab.this.scroll::scrollDown),
                        ConfigEditTab.this.scroll.canScrollDown() ? TextColors.WHITE : TextColors.DARK_GRAY, "[Scroll Down] "));
                if (ConfigEditTab.this.control.options.canAdd && !ConfigEditTab.this.control.inDeleteMode()) {
                    builder.append(Text.of(clickAction(() -> {
                        ConfigEditTab.this.nodeBuilder = NodeBuilder.forNode(ConfigEditTab.this.control.getNode(), ConfigEditTab.this);
                    }), TextColors.GREEN, "[Add]"));
                }
            } else {
                builder.append(Text.of(clickAction(ConfigEditTab.this.control::closeActiveEntry), TextColors.RED, "[Close]"));
            }
            if (ConfigEditTab.this.control.options.canDelete) {
                builder.append(Text.of(clickAction(ConfigEditTab.this.control::setDeleteModeOrDeleteNode), TextColors.RED, " [Delete]"));
            }
            lineFactory.appendNewLine(builder.build(), ctx.forceUnicode);
        }

    }

    private class Breadcrumb implements UIComponent {

        public Breadcrumb() {
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            Text.Builder builder = Text.builder();
            Object[] path = ConfigEditTab.this.control.getPath();
            for (int i = 0; i < path.length; i++) {
                Text.Builder part = Text.builder(path[i].toString());
                final int distance = path.length - i - 1;
                part.color(TextColors.BLUE).onClick(clickAction(() -> {
                    int dist = distance;
                    ConfigurationNode newNode = ConfigEditTab.this.control.getNode();
                    while (dist-- > 0) {
                        newNode = newNode.getParent();
                    }
                    ConfigEditTab.this.control.setNode(newNode);
                }));
                builder.append(part.build());
                if (i < path.length - 1) {
                    builder.append(Text.of("->"));
                }
            }
            lineFactory.addAll(TextUtils.splitLines(builder.build(), ctx.width, ctx.forceUnicode), ctx.forceUnicode);
        }

    }

    public static class Options {

        public static final Options DEFAULTS = new Options(true, true, true, null);
        public final boolean canAdd;
        public final boolean canDelete;
        public final boolean canEdit;
        public final String rootNodeName;

        public Options(boolean add, boolean edit, boolean delete, String rootName) {
            this.canAdd = add;
            this.canEdit = edit;
            this.canDelete = delete;
            this.rootNodeName = rootName;
        }
    }

    public static abstract class ActionHandler {

        public static final ActionHandler NONE = new ActionHandler() {
        };

        public void onNodeChanged(ConfigEditTab tab, ConfigurationNode node) {
        }

        public void onNodeRemoved(ConfigEditTab tab, ConfigurationNode parent, Object key) {
        }

        public void onNodeAdded(ConfigEditTab tab, ConfigurationNode node) {
        }
    }

    final ConfigTabControl control;
    final TableScrollHelper scroll;

    NodeBuilder nodeBuilder;

    public ConfigEditTab(ConfigurationNode node, Text title) {
        this(node, title, Options.DEFAULTS, ActionHandler.NONE);
    }

    public ConfigEditTab(ConfigurationNode rootNode, Text title, Options options, ActionHandler handler) {
        super(title, new AnchorPaneUI());
        this.control = new ConfigTabControl(this, rootNode, options, handler);
        ConfigTableModel model = this.control.createTableModel();
        this.scroll = new TableScrollHelper(model);
        AnchorPaneUI pane = (AnchorPaneUI) getRoot();
        pane.addWithConstraint(new Breadcrumb(), AnchorPaneUI.ANCHOR_TOP);
        pane.getChildren().add(new TableUI(model, this.control.createTableRenderer()));
        pane.addWithConstraint(new ButtonBar(), AnchorPaneUI.ANCHOR_BOTTOM);
    }

    @Override
    public void draw(PlayerContext ctx, LineFactory lineFactory) {
        if (this.nodeBuilder != null) {
            Text rendered = this.nodeBuilder.draw(ctx);
            lineFactory.addAll(TextUtils.splitLines(rendered, ctx.width, ctx.forceUnicode), ctx.forceUnicode);
            return;
        }
        super.draw(ctx, lineFactory);
    }


    @Override
    public void onTextInput(PlayerChatView view, Text input) {
        if (this.nodeBuilder != null) {
            this.nodeBuilder.recieveInput(view, input.toPlain());
            return;
        }
        if (this.control.getActiveEntry() == null) {
            return;
        }
        ConfigurationNode node = this.control.getNode().getNode(this.control.getActiveEntry().key);
        Object value = this.control.getActiveEntry().value.onSetValue(input.toPlain());
        node.setValue(value);
        this.control.onNodeChanged(node);
        this.control.closeActiveEntry();
        view.update();
    }

    public void reloadRootNode(ConfigurationNode root) {
        this.control.reloadRoot(root.getNode(this.control.getNode().getPath()));
    }


}
