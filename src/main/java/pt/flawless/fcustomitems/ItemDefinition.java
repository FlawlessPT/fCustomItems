package pt.flawless.fcustomitems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

final class ItemDefinition {

    private final String id;
    private final ItemStack base;
    private final int defaultAmount;

    private ItemDefinition(String id, ItemStack base, int defaultAmount) {
        this.id = id;
        this.base = base;
        this.defaultAmount = defaultAmount;
    }

    String id() {
        return id;
    }

    int defaultAmount() {
        return defaultAmount;
    }

    ItemStack build(int amount) {
        ItemStack copy = base.clone();
        copy.setAmount(amount);
        return ItemMark.mark(copy, id);
    }

    /** Devolve null (com aviso) se o item for inválido. */
    static ItemDefinition parse(String id, ConfigurationSection s, Logger log) {
        int amount = Math.max(1, s.getInt("quantidade", 1));
        if (s.isItemStack("item")) {
            ItemStack raw = s.getItemStack("item");
            if (raw == null || raw.getType() == Material.AIR) {
                log.warning("fCustomItems: item '" + id + "' ignorado: item inválido (ar)");
                return null;
            }
            return new ItemDefinition(id, raw, amount);
        }
        String name = s.getString("material");
        if (name == null) {
            log.warning("fCustomItems: item '" + id + "' ignorado: falta 'material'");
            return null;
        }
        Material material = Material.matchMaterial(name);
        if (material == null) {
            log.warning("fCustomItems: item '" + id + "' ignorado: material '" + name + "' desconhecido");
            return null;
        }
        if (material == Material.AIR) {
            log.warning("fCustomItems: item '" + id + "' ignorado: material não pode ser AIR");
            return null;
        }
        ItemStack item = new ItemStack(material, 1, (short) s.getInt("data", 0));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (s.isString("nome")) {
                meta.setDisplayName(color(s.getString("nome")));
            }
            if (s.isList("lore")) {
                List<String> lore = new ArrayList<>();
                for (String line : s.getStringList("lore")) {
                    lore.add(color(line));
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        ConfigurationSection enchants = s.getConfigurationSection("encantamentos");
        if (enchants != null) {
            for (String key : enchants.getKeys(false)) {
                Enchantment e = Enchantment.getByName(key.toUpperCase());
                if (e == null) {
                    log.warning("fCustomItems: encantamento '" + key + "' inválido em '" + id + "'");
                    continue;
                }
                item.addUnsafeEnchantment(e, enchants.getInt(key));
            }
        }
        return new ItemDefinition(id, item, amount);
    }

    /** Escreve o item na secção: campos legíveis, ou {@code item:} se tiver meta especial. */
    static void write(ConfigurationSection target, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        boolean special = meta instanceof PotionMeta || meta instanceof BookMeta || meta instanceof SkullMeta
                || meta instanceof EnchantmentStorageMeta || meta instanceof LeatherArmorMeta
                || meta instanceof FireworkMeta || meta instanceof MapMeta;
        if (special) {
            target.set("item", item);
            return;
        }
        target.set("material", item.getType().name());
        target.set("quantidade", item.getAmount());
        target.set("data", (int) item.getDurability());
        if (meta != null) {
            if (meta.hasDisplayName()) {
                target.set("nome", uncolor(meta.getDisplayName()));
            }
            if (meta.hasLore()) {
                List<String> lore = new ArrayList<>();
                for (String line : meta.getLore()) {
                    lore.add(uncolor(line));
                }
                target.set("lore", lore);
            }
        }
        for (Map.Entry<Enchantment, Integer> e : item.getEnchantments().entrySet()) {
            target.set("encantamentos." + e.getKey().getName(), e.getValue());
        }
    }

    private static String uncolor(String s) {
        return s.replace('§', '&');
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
