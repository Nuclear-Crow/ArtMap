package me.Fupery.ArtMap.Easel;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.Fupery.ArtMap.Recipe.ArtMaterial;
import org.bukkit.Bukkit;
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
import me.Fupery.ArtMap.Utils.ItemUtils;

/**
 * Represents a painting canvas. Extends ItemStack so that information can be
 * retrieved when it is pulled off the easel.
 *
 */
public class Canvas {

	protected int mapId;
	protected int resolution = 4;

	public Canvas(Map map, ArtMaterial material) {
		this(map.getMapId(), getResolutionFactorFromArtMaterial(material));
	}

	public Canvas(int mapId, ArtMaterial material) {
		this(mapId, getResolutionFactorFromArtMaterial(material));
	}

	public Canvas(Map map, int resolution) {
		this(map.getMapId(), resolution);
	}

	public Canvas(int mapId, int resolution) {
		this.mapId = mapId;
		this.resolution = resolution;
	}

	public static Canvas getCanvas(ItemStack item) throws SQLException, ArtMapException {
		if (item == null || item.getType() != Material.FILLED_MAP)
			return null;

		MapMeta meta = (MapMeta) item.getItemMeta();
		int mapId = meta.getMapView().getId();
		if (item.getItemMeta() != null && item.getItemMeta().getLore() != null
				&& item.getItemMeta().getLore().contains(ArtItem.COPY_KEY)) {
			return new CanvasCopy(item, getResolutionFactorFromMap(item));
		}
		return new Canvas(mapId, getResolutionFactorFromMap(item));
	}

	public ItemStack getEaselItem() {
		ItemStack item = new InProgressArtworkItem(this.mapId).toItemStack();
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		if (isMedium()) {
			lore.add(ArtItem.MEDIUM_CANVAS_KEY);
		}
		else if (isLarge()) {
			lore.add(ArtItem.LARGE_CANVAS_KEY);
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public int getMapId() {
		return this.mapId;
	}

	public int getResolution() {
		return this.resolution;
	}

	public boolean isMedium() {
		return this.resolution == 2;
	}

	public boolean isLarge() {
		return this.resolution == 1;
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

		public CanvasCopy(Map map, MapArt orginal) {
			super(map, orginal.getResolution());
			this.original = orginal;
		}

		public CanvasCopy(ItemStack map, int resolution) throws SQLException, ArtMapException {
			super(ItemUtils.getMapID(map), resolution);
			ItemMeta meta = map.getItemMeta();
			List<String> lore = meta.getLore();
			if (lore == null || !lore.contains(ArtItem.COPY_KEY)) {
				throw new ArtMapException("The Copied canvas is missing the copy key!");
			}
			String originalName = lore.get(lore.indexOf(ArtItem.COPY_KEY) + 1);
			this.original = ArtMap.instance().getArtDatabase().getArtwork(originalName);
		}

		/*
		@Override
		public ItemStack getDroppedItem() {
			return this.original.getMapItem();
		}
		*/

		@Override
		public ItemStack getEaselItem() {
			ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
			MapMeta meta = (MapMeta) mapItem.getItemMeta();
			meta.setMapView(ArtMap.getMap(this.mapId));
			// Set copy lore
			if (isMedium()) {
				meta.setLore(Arrays.asList(ArtItem.MEDIUM_CANVAS_KEY, ArtItem.COPY_KEY, this.original.getTitle()));
			}
			else if (isLarge()) {
				meta.setLore(Arrays.asList(ArtItem.LARGE_CANVAS_KEY, ArtItem.COPY_KEY, this.original.getTitle()));
			}
			else {
				meta.setLore(Arrays.asList(ArtItem.COPY_KEY, this.original.getTitle()));
			}
			mapItem.setItemMeta(meta);
			return mapItem;
		}

		/**
		 * @return The original map id.
		 */
		public int getOriginalId() {
			return this.original.getMapId();
		}
    }
}
