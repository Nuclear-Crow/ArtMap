package me.Fupery.ArtMap.Easel;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import me.Fupery.ArtMap.Recipe.ArtMaterial;
import org.bukkit.Bukkit;
import me.Fupery.ArtMap.Recipe.ArtMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;

import me.Fupery.ArtMap.ArtMap;
import me.Fupery.ArtMap.Exception.ArtMapException;
import me.Fupery.ArtMap.IO.MapArt;
import me.Fupery.ArtMap.IO.Database.Map;
import me.Fupery.ArtMap.Recipe.ArtItem;
import me.Fupery.ArtMap.Recipe.ArtItem.InProgressArtworkItem;
import me.Fupery.ArtMap.api.Config.Lang;

/**
 * Represents a painting canvas..
 *
 */
public class Canvas {

	protected int mapId;
	protected String artist;
	protected int resolution = 4;

	public Canvas(Map map, String artist, int resolution) {
		this.mapId = map.getMapId();
		this.artist = artist;
		this.resolution = resolution;
	}

	protected Canvas(Map map, String artist, ArtMaterial material) {
		this.mapId = map.getMapId();
		this.artist = artist;
		this.resolution = getResolutionFactorFromArtMaterial(material);
	}

	protected Canvas(int mapId, String artist, ArtMaterial material) {
		this.mapId = mapId;
		this.artist = artist;
		this.resolution = getResolutionFactorFromArtMaterial(material);
	}

	protected Canvas(int mapId, String artist, int resolution) {
		this.mapId = mapId;
		this.artist = artist;
		this.resolution = resolution;
	}

	public static Canvas getCanvas(ItemStack item) throws SQLException, ArtMapException {
		if (item == null || item.getType() != Material.FILLED_MAP)
			return null;

		//Is this an unfinished artwork?
		if(ArtItem.isUnfinishedArtwork(item)) {
			//extract artist and id
			MapMeta meta = (MapMeta) item.getItemMeta();
			int mapId = meta.getMapView().getId();
			return new Canvas(mapId, parseArtist(meta.getLore()), getResolutionFactorFromMap(item));
		}

		//Is this a copy artwork?
		if(ArtItem.isCopyArtwork(item)) {
			//Extract id, artist, and original title
			MapMeta meta = (MapMeta) item.getItemMeta();
			int mapId = meta.getMapView().getId();
			MapArt original = ArtMap.instance().getArtDatabase().getArtwork(meta.getDisplayName());
			return new CanvasCopy(new Map(mapId), original);
		}

		//Check if this is an artmap tracked piece. Legacy check.
		MapMeta meta = (MapMeta) item.getItemMeta();
		int mapId = meta.getMapView().getId();
		//unsaved
		if(ArtMap.instance().getArtDatabase().containsUnsavedArtwork(mapId)){
			return new Canvas(mapId, "unknown", getResolutionFactorFromMap(item));
		}
		//previously saved but missing tags
		MapArt art = ArtMap.instance().getArtDatabase().getArtwork(mapId);
		if(art != null) {
			return new CanvasCopy(art.getMap(), art);
		}
		return null;

	}

	/**
	 * Parse the artist name out of the lore String.
	 * @param meta List of Strings that is the item meta
	 * @return The Artist name.
	 */
	public static String parseArtist(List<String> meta) {
		String key = Lang.RECIPE_ARTWORK_ARTIST.get().replace("%s", "").trim();
		Optional<String> artistName = meta.stream().filter(s -> s.contains(key)).findFirst();
		if(artistName.isPresent()) {
			return artistName.get().replace(key, "").trim();
		}
		return null;
	}

	public ItemStack getEaselItem() {
		return new InProgressArtworkItem(this.mapId, artist, resolution).toItemStack();
	}

	public int getMapId() {
		return this.mapId;
	}

	/**
	 *
	 */
	public int getResolution() {
		return this.resolution;
	}

	public static int getResolutionFactorFromArtMaterial(ArtMaterial material) {
		switch (material) {
			case MEDIUM_CANVAS:
				return 2;
			case LARGE_CANVAS:
				return 1;
			default:
				return 4;
		}
	}

	public static int getResolutionFactorFromMap(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasLore()) {
			return 4;
		}
		List<String> lore = meta.getLore();
		if (lore.contains(ArtItem.MEDIUM_CANVAS_KEY)) {
			return 2;
		}
		else if (lore.contains(ArtItem.LARGE_CANVAS_KEY)) {
			return 1;
		}
		else {
			return 4;
		}
	}

	public static class CanvasCopy extends Canvas {

		private MapArt original;

		public CanvasCopy(Map map, MapArt original) {
			super(map,original.getArtistName(), original.getResolution());
			this.original = original;
		}

		@Override
		public ItemStack getEaselItem() {
			return new ArtItem.CopyArtworkItem(this.mapId, original.getTitle(), original.getArtistName(), original.getDate(), original).toItemStack();
		}

		/**
		 * @return The original map id.
		 */
		public int getOriginalId() {
			return this.original.getMapId();
		}
    }
}
