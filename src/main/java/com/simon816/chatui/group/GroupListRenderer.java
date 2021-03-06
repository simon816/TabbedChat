package com.simon816.chatui.group;

import com.simon816.chatui.ChatUI;
import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.ui.table.DefaultColumnRenderer;
import com.simon816.chatui.ui.table.DefaultTableRenderer;
import com.simon816.chatui.ui.table.TableColumnRenderer;
import com.simon816.chatui.util.Utils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.List;

import javax.swing.SwingConstants;

class GroupListRenderer extends DefaultTableRenderer {

    private final ChatGroupFeature feature;
    private final PlayerChatView view;

    GroupListRenderer(PlayerChatView view, ChatGroupFeature feature) {
        this.view = view;
        this.feature = feature;
    }

    @Override
    protected int getCellAlignment(int row, int column) {
        if (column == 1) {
            return SwingConstants.CENTER;
        }
        return super.getCellAlignment(row, column);
    }

    @Override
    public TableColumnRenderer createColumnRenderer(int columnIndex) {
        if (columnIndex == 0) {
            return GroupNameColumnRenderer.INSTANCE;
        } else if (columnIndex == 1) {
            return new DeleteButtonColumnRenderer(this.view, this.feature);
        }
        return super.createColumnRenderer(columnIndex);
    }

    private static class GroupNameColumnRenderer extends DefaultColumnRenderer {

        static final GroupNameColumnRenderer INSTANCE = new GroupNameColumnRenderer();

        private GroupNameColumnRenderer() {
        }

        @Override
        public List<Text> renderCell(Object value, int row, int tableWidth, PlayerContext ctx) {
            ChatGroup group = (ChatGroup) value;
            return ctx.utils().splitLines(Text.of(Utils.execClick(view -> {
                ChatUI.getActiveView(view).getWindow().addTab(group.getTab(view.getPlayer()), true);
                view.update();
            }), group.getName()), getPrefWidth());
        }
    }

    private static class DeleteButtonColumnRenderer extends DefaultColumnRenderer {

        private final PlayerChatView view;
        private final ChatGroupFeature feature;

        DeleteButtonColumnRenderer(PlayerChatView view, ChatGroupFeature feature) {
            this.view = view;
            this.feature = feature;
        }

        @Override
        public List<Text> renderCell(Object value, int row, int tableWidth, PlayerContext ctx) {
            ChatGroup group = (ChatGroup) value;
            Text.Builder builder = Text.builder("X");
            builder.onHover(TextActions.showText(Text.of("Delete Group")));
            if (this.feature.canDeleteGroup(group, this.view.getPlayer())) {
                builder.color(TextColors.RED);
                builder.onClick(Utils.execClick(view -> {
                    this.feature.removeGroup(group);
                    view.update();
                }));
            } else {
                builder.color(TextColors.GRAY);
            }
            return Collections.singletonList(builder.build());
        }
    }
}
