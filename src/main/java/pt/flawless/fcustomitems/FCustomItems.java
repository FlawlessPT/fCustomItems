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
        CustomItemCommand command = new CustomItemCommand(registry, dir);
        getCommand("customitem").setExecutor(command);
        getCommand("customitem").setTabCompleter(command);
        getServer().getPluginManager().registerEvents(new ItemsMenuListener(registry), this);
        getServer().getServicesManager().register(ItemsApi.class, registry, this, ServicePriority.Normal);
        getLogger().info(registry.size() + " itens carregados.");
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
    }
}
