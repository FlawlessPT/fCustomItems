package pt.flawless.fcustomitems;

import java.util.Optional;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/** Marca o id do item custom no NBT do item. */
final class ItemMark {

    static final String KEY = "fcustomitems_id";

    private ItemMark() {
    }

    static ItemStack mark(ItemStack item, String id) {
        net.minecraft.server.v1_8_R1.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nms.getTag() != null ? nms.getTag() : new NBTTagCompound();
        tag.setString(KEY, id);
        nms.setTag(tag);
        return CraftItemStack.asBukkitCopy(nms);
    }

    static Optional<String> read(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return Optional.empty();
        }
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if (tag == null || !tag.hasKey(KEY)) {
            return Optional.empty();
        }
        return Optional.of(tag.getString(KEY));
    }
}
