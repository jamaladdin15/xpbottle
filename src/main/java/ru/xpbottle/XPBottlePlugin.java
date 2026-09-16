package ru.xpbottle;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class XPBottlePlugin extends JavaPlugin {

    // Ключ, под которым в NBT (PersistentDataContainer) предмета и сущности
    // хранится количество опыта, "запечатанное" в бутылке.
    private NamespacedKey storedXpKey;

    @Override
    public void onEnable() {
        this.storedXpKey = new NamespacedKey(this, "stored_xp");

        getCommand("px").setExecutor(new XPCommand(this));

        XPBottleListener listener = new XPBottleListener(this);
        getServer().getPluginManager().registerEvents(listener, this);

        getLogger().info("XPBottle успешно запущен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("XPBottle выключен.");
    }

    public NamespacedKey getStoredXpKey() {
        return storedXpKey;
    }
}
