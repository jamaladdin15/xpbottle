package ru.xpbottle;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class XPCommand implements CommandExecutor {

    private final XPBottlePlugin plugin;

    public XPCommand(XPBottlePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эта команда доступна только игрокам.");
            return true;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack itemInHand = inventory.getItemInMainHand();

        // 1. Бутылка должна быть в руке и быть именно пустой стеклянной бутылкой
        if (itemInHand.getType() != Material.GLASS_BOTTLE) {
            player.sendMessage(ChatColor.RED + "Возьмите в руку пустую стеклянную бутылку.");
            return true;
        }

        // 2. У игрока должен быть хотя бы 1 уровень опыта
        if (player.getLevel() < 1) {
            player.sendMessage(ChatColor.RED + "Недостаточно опыта. Нужен как минимум 1 уровень.");
            return true;
        }

        // 3. Разбираем аргумент: сколько уровней перенести (по умолчанию — 1)
        int levelsToTake;
        if (args.length == 0) {
            levelsToTake = 1;
        } else {
            try {
                levelsToTake = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Укажите целое число уровней, например: /px 5");
                return true;
            }
        }

        if (levelsToTake < 1) {
            player.sendMessage(ChatColor.RED + "Количество уровней должно быть больше нуля.");
            return true;
        }

        if (levelsToTake > player.getLevel()) {
            player.sendMessage(ChatColor.RED + "У вас нет столько уровней. Сейчас у вас: "
                    + player.getLevel() + ".");
            return true;
        }

        // 4. Считаем, сколько очков опыта составляют указанные уровни, и забираем их
        int currentLevel = player.getLevel();
        int totalXp = ExperienceUtil.getExpToReachLevel(currentLevel)
                - ExperienceUtil.getExpToReachLevel(currentLevel - levelsToTake);

        player.giveExpLevels(-levelsToTake);

        // 4. Убираем одну бутылку из руки
        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        } else {
            inventory.setItemInMainHand(null);
        }

        // 5. Создаём "бутылку опыта" с сохранённым количеством XP в NBT
        ItemStack xpBottle = createStoredXpBottle(totalXp);

        // Отдаём предмет игроку (в инвентарь, либо роняем под ноги, если инвентарь полон)
        var leftover = inventory.addItem(xpBottle);
        leftover.values().forEach(item ->
                player.getWorld().dropItemNaturally(player.getLocation(), item));

        player.sendMessage(ChatColor.GREEN + "Вы перелили " + ChatColor.YELLOW + totalXp
                + ChatColor.GREEN + " опыта в бутылку.");

        return true;
    }

    private ItemStack createStoredXpBottle(int amount) {
        ItemStack bottle = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        ItemMeta meta = bottle.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "Бутылка опыта" + ChatColor.GRAY + " (" + amount + " XP)");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "Содержит " + amount + " очков опыта.");
        lore.add(ChatColor.DARK_GRAY + "Разбейте, чтобы получить опыт обратно.");
        meta.setLore(lore);

        NamespacedKey key = plugin.getStoredXpKey();
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, amount);

        bottle.setItemMeta(meta);
        return bottle;
    }
}
