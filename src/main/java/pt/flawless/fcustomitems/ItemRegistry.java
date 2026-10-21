package pt.flawless.fcustomitems;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fapi.items.ItemsApi;

final class ItemRegistry implements ItemsApi {

    private final Logger log;
    private final Map<String, ItemDefinition> items = new LinkedHashMap<>();

    ItemRegistry(Logger log) {
        this.log = log;
    }

    void load(File dir) {
        File[] files = dir.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) {
            return;
        }
        Arrays.sort(files);
        for (File file : files) {
            YamlConfiguration yaml = new YamlConfiguration();
            try {
                yaml.load(file);
            } catch (Exception e) {
                log.warning("fCustomItems: ficheiro " + file.getName() + " inválido: " + e.getMessage());
                continue;
            }
            for (String id : yaml.getKeys(false)) {
                ConfigurationSection section = yaml.getConfigurationSection(id);
                if (section == null) {
                    log.warning("fCustomItems: item '" + id + "' ignorado: não é uma secção (" + file.getName() + ")");
                    continue;
                }
                if (items.containsKey(id.toLowerCase(Locale.ROOT))) {
                    log.warning("fCustomItems: id '" + id + "' repetido em " + file.getName() + ", fica o primeiro");
                    continue;
                }
                ItemDefinition def = ItemDefinition.parse(id, section, log);
                if (def != null) {
                    register(def);
                }
            }
        }
    }

    void register(ItemDefinition d) {
        items.putIfAbsent(d.id().toLowerCase(Locale.ROOT), d);
    }

    int size() {
        return items.size();
    }

    @Override
    public Optional<ItemStack> create(String id) {
        ItemDefinition d = find(id);
        return d == null ? Optional.empty() : Optional.of(d.build(d.defaultAmount()));
    }

    @Override
    public Optional<ItemStack> create(String id, int amount) {
        ItemDefinition d = find(id);
        return d == null || amount < 1 ? Optional.empty() : Optional.of(d.build(amount));
    }

    @Override
    public boolean exists(String id) {
        return find(id) != null;
    }

    @Override
    public Optional<String> idOf(ItemStack item) {
        // devolve a grafia original do registo, não a da marca
        return ItemMark.read(item).map(this::find).map(ItemDefinition::id);
    }

    @Override
    public Set<String> ids() {
        Set<String> out = new LinkedHashSet<>();
        for (ItemDefinition d : items.values()) {
            out.add(d.id());
        }
        return Collections.unmodifiableSet(out);
    }

    private ItemDefinition find(String id) {
        return id == null ? null : items.get(id.toLowerCase(Locale.ROOT));
    }
}
