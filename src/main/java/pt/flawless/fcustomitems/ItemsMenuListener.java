package pt.flawless.fcustomitems;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

final class ItemsMenuListener implements Listener {

    private final ItemRegistry registry;

    ItemsMenuListener(ItemRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof ItemsMenu menu)) {
            return;
        }
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player player) || e.getRawSlot() != e.getSlot()) {
            return;
        }
        if (e.getSlot() == ItemsMenu.PREV_SLOT && menu.hasPrev()) {
            new ItemsMenu(registry, menu.page() - 1).open(player);
        } else if (e.getSlot() == ItemsMenu.NEXT_SLOT && menu.hasNext()) {
            new ItemsMenu(registry, menu.page() + 1).open(player);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof ItemsMenu) {
            e.setCancelled(true);
        }
    }
}
