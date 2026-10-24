package pt.flawless.fcustomitems;

import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/** Marca o id do item custom no PersistentDataContainer da meta do item. */
final class ItemMark {

    static final NamespacedKey KEY = new NamespacedKey("fcustomitems", "id");

    private ItemMark() {
    }

    static ItemStack mark(ItemStack item, String id) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) {
            return copy;
        }
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, id);
        copy.setItemMeta(meta);
        return copy;
    }

    static Optional<String> read(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return Optional.empty();
        }
        return Optional.ofNullable(item.getItemMeta().getPersistentDataContainer().get(KEY, PersistentDataType.STRING));
    }
}
