package ru.xpbottle;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class XPBottleListener implements Listener {

    private final XPBottlePlugin plugin;

    // Временное хранилище: игрок бросил бутылку -> сколько опыта в ней было запечатано.
    // Заполняется в момент клика (PlayerInteractEvent) и считывается сразу же
    // при вылете снаряда (ProjectileLaunchEvent).
    private final Map<UUID, Integer> pendingThrows = new ConcurrentHashMap<>();

    public XPBottleListener(XPBottlePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.EXPERIENCE_BOTTLE) {
            return;
        }

        Integer storedXp = readStoredXp(item);
        if (storedXp == null) {
            return; // обычная ванильная бутылка опыта — не трогаем
        }

        pendingThrows.put(event.getPlayer().getUniqueId(), storedXp);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof ThrownExpBottle bottleEntity)) {
            return;
        }

        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player player)) {
            return;
        }

        Integer storedXp = pendingThrows.remove(player.getUniqueId());
        if (storedXp == null) {
            return;
        }

        NamespacedKey key = plugin.getStoredXpKey();
        bottleEntity.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, storedXp);
    }

    @EventHandler
    public void onExpBottleBreak(ExpBottleEvent event) {
        ThrownExpBottle bottleEntity = event.getEntity();
        NamespacedKey key = plugin.getStoredXpKey();

        Integer storedXp = bottleEntity.getPersistentDataContainer()
                .get(key, PersistentDataType.INTEGER);

        if (storedXp != null) {
            // Это наша "заряженная" бутылка — выдаём ровно столько опыта,
            // сколько было забрано у игрока при её создании.
            event.setExperience(storedXp);
        }
        // Если тега нет — это обычная ванильная бутылка, ничего не меняем.
    }

    private Integer readStoredXp(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        NamespacedKey key = plugin.getStoredXpKey();
        return meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
    }
}
