package pt.flawless.fcustomitems;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fapi.messages.Messages;

final class CustomItemCommand implements TabExecutor {

    private static final String USE_HELP = "Uso: /customitem help";
    private static final List<String> SUBS = List.of("give", "criar", "listar", "help");

    private final ItemRegistry registry;
    private final File itemsDir;

    CustomItemCommand(ItemRegistry registry, File itemsDir) {
        this.registry = registry;
        this.itemsDir = itemsDir;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("fcustomitems.admin")) {
            Messages.noPermission(sender);
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§c" + USE_HELP);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help" -> {
                sender.sendMessage("§e/customitem give <jogador> <id> [qtd] §7- Dá um item custom a um jogador");
                sender.sendMessage("§e/customitem criar <id> §7- Guarda o item da mão como item custom");
                sender.sendMessage("§e/customitem listar [página] §7- Abre o menu dos itens custom");
                sender.sendMessage("§e/customitem help §7- Mostra esta lista");
            }
            case "give" -> give(sender, args);
            case "criar", "create" -> criar(sender, args);
            case "listar", "list" -> listar(sender, args);
            default -> sender.sendMessage("§c" + USE_HELP);
        }
        return true;
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length < 3 || args.length > 4) {
            sender.sendMessage("§cUso: /customitem give <jogador> <id> [qtd]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cJogador '" + args[1] + "' não está online.");
            return;
        }
        if (!registry.exists(args[2])) {
            sender.sendMessage("§cItem '" + args[2] + "' não existe.");
            return;
        }
        int qty = 0;
        if (args.length == 4) {
            try {
                qty = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                qty = 0;
            }
            if (qty < 1 || qty > 2304) {
                sender.sendMessage("§cA quantidade tem de ser um inteiro entre 1 e 2304.");
                return;
            }
        }
        // qtd omitida: usa a quantidade configurada do item
        ItemStack first = (qty == 0 ? registry.create(args[2]) : registry.create(args[2], 1)).orElse(null);
        if (first == null) {
            sender.sendMessage("§cItem '" + args[2] + "' não existe.");
            return;
        }
        int remaining = qty == 0 ? first.getAmount() : qty;
        int total = remaining;
        int max = Math.max(1, first.getMaxStackSize());
        while (remaining > 0) {
            int n = Math.min(max, remaining);
            ItemStack stack = registry.create(args[2], n).orElse(null);
            if (stack == null) {
                break;
            }
            Map<Integer, ItemStack> left = target.getInventory().addItem(stack);
            for (ItemStack rest : left.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), rest);
            }
            remaining -= n;
        }
        sender.sendMessage("§aDeste " + total + "x '" + args[2] + "' a " + target.getName() + ".");
    }

    private void criar(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSó jogadores podem usar este comando.");
            return;
        }
        if (args.length != 2) {
            sender.sendMessage("§cUso: /customitem criar <id>");
            return;
        }
        String id = args[1];
        if (!id.matches("[A-Za-z0-9_]+")) {
            sender.sendMessage("§cO id só pode ter letras, números e _.");
            return;
        }
        if (registry.exists(id)) {
            sender.sendMessage("§cJá existe um item com o id '" + id + "'.");
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            sender.sendMessage("§cSegura um item na mão.");
            return;
        }
        File file = new File(itemsDir, "guardados.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        if (file.exists()) {
            try {
                yaml.load(file);
            } catch (Exception e) {
                sender.sendMessage("§cguardados.yml inválido, nada foi escrito: " + e.getMessage());
                return;
            }
        }
        if (yaml.contains(id)) {
            sender.sendMessage("§cO id '" + id + "' já está em guardados.yml (inválido?). Escolhe outro.");
            return;
        }
        ConfigurationSection section = yaml.createSection(id);
        ItemDefinition.write(section, hand);
        try {
            itemsDir.mkdirs();
            File tmp = new File(itemsDir, "guardados.yml.tmp");
            yaml.save(tmp);
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            sender.sendMessage("§cErro ao gravar guardados.yml: " + e.getMessage());
            return;
        }
        ItemDefinition def = ItemDefinition.parse(id, section, Bukkit.getLogger());
        if (def == null) {
            sender.sendMessage("§cItem gravado mas inválido; vê a consola.");
            return;
        }
        registry.register(def);
        sender.sendMessage("§aItem '" + id + "' guardado.");
    }

    private void listar(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSó jogadores podem usar este comando.");
            return;
        }
        if (args.length > 2) {
            sender.sendMessage("§cUso: /customitem listar [página]");
            return;
        }
        int page = 1;
        if (args.length == 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                page = 0;
            }
            if (page < 1) {
                sender.sendMessage("§cA página tem de ser um inteiro a partir de 1.");
                return;
            }
        }
        new ItemsMenu(registry, page).open(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("fcustomitems.admin")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return filter(SUBS, args[0]);
        }
        if (args[0].equalsIgnoreCase("give")) {
            if (args.length == 2) {
                List<String> names = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    names.add(p.getName());
                }
                return filter(names, args[1]);
            }
            if (args.length == 3) {
                return filter(new ArrayList<>(registry.ids()), args[2]);
            }
        }
        return Collections.emptyList();
    }

    private static List<String> filter(List<String> options, String prefix) {
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
                out.add(o);
            }
        }
        return out;
    }
}
