package pt.flawless.fcustomitems;

import java.io.File;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fapi.items.ItemsApi;

public class FCustomItems extends JavaPlugin {

    @Override
    public void onEnable() {
        File dir = new File(getDataFolder(), "items");
        if (!dir.exists()) {
            saveResource("items/exemplo.yml", false);
        }
        ItemRegistry registry = new ItemRegistry(getLogger());
        registry.load(dir);
        getServer().getServicesManager().register(ItemsApi.class, registry, this, ServicePriority.Normal);
        getLogger().info(registry.size() + " itens carregados.");
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
    }
}
