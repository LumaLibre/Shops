package dev.lumas.shops.listeners;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.components.data.KeyConsumer;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import org.bukkit.event.Listener;

@Register(Autowire.LISTENER)
public class DialogListener implements Listener {

    public void onHandleCustomClick(PlayerCustomClickEvent event) {
        if (event.getIdentifier() instanceof KeyConsumer<?> keyConsumer) {
            // TODO
            //keyConsumer.call();
        }
    }
}
