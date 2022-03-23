package me.Fupery.ArtMap.Listeners;

import me.Fupery.ArtMap.Easel.Easel;
import me.Fupery.ArtMap.Easel.EaselPart;
import net.countercraft.movecraft.events.CraftTeleportEntityEvent;
import org.bukkit.event.EventHandler;

public class MovecraftListener implements RegisteredListener {

    @EventHandler
    public void onCraftTeleportEntity(CraftTeleportEntityEvent event) {
        EaselPart part = EaselPart.getPartType(event.getEntity());
        if (part == null) {
            return;
        }

        Easel easel = Easel.getEasel(event.getEntity().getLocation(), part);
        if (easel == null) {
            return;
        }

        event.setCancelled(true);
    }

    @Override
    public void unregister() {
        CraftTeleportEntityEvent.getHandlerList().unregister(this);
    }
}
