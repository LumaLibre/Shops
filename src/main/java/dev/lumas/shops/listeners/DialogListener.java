package dev.lumas.shops.listeners;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.components.data.KeyConsumerRegistry;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Register(Autowire.LISTENER)
@SuppressWarnings("UnstableApiUsage")
public class DialogListener implements Listener {

    @EventHandler
    public void onHandleCustomClick(PlayerCustomClickEvent event) {
        PlayerGameConnection connection = (PlayerGameConnection) event.getCommonConnection();
        KeyConsumerRegistry.INSTANCE.dispatch(connection.getPlayer(), event.getIdentifier());
    }
}
