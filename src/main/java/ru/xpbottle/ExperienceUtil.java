package ru.xpbottle;

import org.bukkit.entity.Player;

/**
 * Формулы для перевода "уровень + прогресс" в абсолютное количество очков опыта
 * и обратно. Формулы соответствуют механике ванильного Minecraft.
 */
public final class ExperienceUtil {

    private ExperienceUtil() {
    }

    /**
     * Сколько очков опыта нужно, чтобы подняться с level на level+1.
     */
    public static int getExpToNextLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    /**
     * Сколько очков опыта нужно набрать с нуля, чтобы достичь указанного уровня.
     */
    public static int getExpToReachLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    /**
     * Полное количество очков опыта, которое сейчас есть у игрока
     * (учитывая и целые уровни, и дробный прогресс до следующего уровня).
     */
    public static int getTotalExperience(Player player) {
        int level = player.getLevel();
        float progress = player.getExp(); // от 0.0 до 1.0

        int base = getExpToReachLevel(level);
        int partial = Math.round(progress * getExpToNextLevel(level));

        return base + partial;
    }

    /**
     * Полностью обнуляет опыт и уровень игрока.
     */
    public static void clearExperience(Player player) {
        player.setLevel(0);
        player.setExp(0f);
        player.setTotalExperience(0);
    }
}
