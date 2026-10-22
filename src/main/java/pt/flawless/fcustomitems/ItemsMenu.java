package pt.flawless.fcustomitems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pt.flawless.fapi.enums.FInventorySize;
import pt.flawless.fapi.inventory.FInventory;
import pt.flawless.fapi.items.FItemStack;

/** Menu {@code /customitem listar}: 54 slots, moldura de vidro, 28 itens por página e setas na linha de baixo. */
final class ItemsMenu implements InventoryHolder {

    static final int PREV_SLOT = 48;
    static final int NEXT_SLOT = 50;
    private static final int SIZE = 54;
    private static final List<Integer> INNER = new ArrayList<>();

    static {
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                INNER.add(row * 9 + col);
            }
        }
    }

    private final int page;
    private final int pages;
    private final Inventory inventory;

    ItemsMenu(ItemRegistry registry, int requestedPage) {
        List<String> ids = new ArrayList<>(registry.ids());
        Collections.sort(ids, String.CASE_INSENSITIVE_ORDER);
        this.pages = Math.max(1, (ids.size() + INNER.size() - 1) / INNER.size());
        this.page = Math.max(1, Math.min(requestedPage, pages));
        this.inventory = new FInventory(this, FInventorySize.SIZE_54, "§8Itens custom " + page + "/" + pages).getInventory();
        // nome " " (espaço): o Bukkit 1.8 aceita e o tooltip fica vazio
        ItemStack glass = new FItemStack(Material.STAINED_GLASS_PANE, 1).setDisplayName(" ").setDurability((short) 7).createItem();
        for (int i = 0; i < SIZE; i++) {
            if (!INNER.contains(i)) {
                inventory.setItem(i, glass);
            }
        }
        int from = (page - 1) * INNER.size();
        for (int i = 0; i < INNER.size() && from + i < ids.size(); i++) {
            String id = ids.get(from + i);
            ItemStack item = registry.create(id).orElse(null);
            if (item == null) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("§8" + "-".repeat(10));
            lore.add("§7Id: §f" + id);
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(INNER.get(i), item);
        }
        if (page > 1) {
            inventory.setItem(PREV_SLOT, new FItemStack(Material.ARROW, 1).setDisplayName("§ePágina anterior").createItem());
        }
        if (page < pages) {
            inventory.setItem(NEXT_SLOT, new FItemStack(Material.ARROW, 1).setDisplayName("§ePágina seguinte").createItem());
        }
    }

    int page() {
        return page;
    }

    boolean hasPrev() {
        return page > 1;
    }

    boolean hasNext() {
        return page < pages;
    }

    void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
