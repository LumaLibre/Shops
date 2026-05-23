package dev.lumas.shops.listeners;

import com.google.common.base.Preconditions;
import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.components.data.KeyConsumerRegistry;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Register(Autowire.LISTENER)
@SuppressWarnings("UnstableApiUsage")
public class DialogListener implements Listener {

    @EventHandler
    public void onHandleCustomClick(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerGameConnection connection)) return;
        DialogResponseView view = Preconditions.checkNotNull(event.getDialogResponseView(), "DialogResponseView is null");
        KeyConsumerRegistry.INSTANCE.dispatch(connection.getPlayer(), event.getIdentifier(), view);
    }
}
