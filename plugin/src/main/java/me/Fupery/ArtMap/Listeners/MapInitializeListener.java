package me.Fupery.ArtMap.Listeners;

import java.util.logging.Level;

import me.Fupery.ArtMap.IO.MapArt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.MapInitializeEvent;

import me.Fupery.ArtMap.ArtMap;
import me.Fupery.ArtMap.IO.Database.Map;

public class MapInitializeListener implements RegisteredListener {

    @EventHandler
    public void onMapInitialize(MapInitializeEvent event) {
        try {
            int mapId = event.getMap().getId();
            ArtMap.instance().getScheduler().ASYNC.run(() -> {
                try {
                    if (!ArtMap.instance().getArtDatabase().containsArtwork(mapId))
                        return;
                    Map map = new Map(mapId);
                    MapArt art = ArtMap.instance().getArtDatabase().getArtwork(mapId);
                    int resolution = 4;
                    if (art != null)
                        resolution = art.getResolution();
                    ArtMap.instance().getArtDatabase().restoreMap(map, true, false, resolution);
                } catch (Exception e) {
                    ArtMap.instance().getLogger().log(Level.SEVERE, "Error with map restore!", e);
                }
            });
        } catch (Exception e) {
            ArtMap.instance().getLogger().log(Level.SEVERE, "Error with map restore!", e);
        }
    }

    @Override
    public void unregister() {
        MapInitializeEvent.getHandlerList().unregister(this);
    }
}
