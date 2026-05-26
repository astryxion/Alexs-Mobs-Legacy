package com.github.alexthe666.alexsmobs.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Forge 1.12.2 configuration. Replaces 1.16 {@code ForgeConfigSpec} with {@link Configuration}.
 */
public final class ConfigHolder {

    public static Configuration COMMON_CONFIGURATION;
    public static CommonConfig COMMON;

    private ConfigHolder() {
    }

    public static void initCommonConfiguration(File configFile) {
        COMMON_CONFIGURATION = new Configuration(configFile);
        COMMON_CONFIGURATION.load();
        COMMON = new CommonConfig();
        AMConfig.bake();
        if (COMMON_CONFIGURATION.hasChanged()) {
            COMMON_CONFIGURATION.save();
        }
    }

    public static void reloadCommonConfiguration() {
        if (COMMON_CONFIGURATION == null) {
            return;
        }
        COMMON_CONFIGURATION.load();
        AMConfig.bake();
        if (COMMON_CONFIGURATION.hasChanged()) {
            COMMON_CONFIGURATION.save();
        }
    }

    public static final class FCInt {
        private final String category;
        private final String key;
        private final int def;
        private final int min;
        private final int max;
        private final String comment;

        public FCInt(String category, String key, int def, int min, int max, String comment) {
            this.category = category;
            this.key = key;
            this.def = def;
            this.min = min;
            this.max = max;
            this.comment = comment;
        }

        public int get() {
            if (COMMON_CONFIGURATION == null) {
                return def;
            }
            return COMMON_CONFIGURATION.get(category, key, def, comment, min, max).getInt();
        }
    }

    public static final class FCDouble {
        private final String category;
        private final String key;
        private final float def;
        private final float min;
        private final float max;
        private final String comment;

        public FCDouble(String category, String key, double def, double min, double max, String comment) {
            this.category = category;
            this.key = key;
            this.def = (float) def;
            this.min = (float) min;
            this.max = (float) max;
            this.comment = comment;
        }

        public double get() {
            if (COMMON_CONFIGURATION == null) {
                return def;
            }
            return COMMON_CONFIGURATION.get(category, key, (double) def, comment, (double) min, (double) max).getDouble();
        }
    }

    public static final class FCBool {
        private final String category;
        private final String key;
        private final boolean def;
        private final String comment;

        public FCBool(String category, String key, boolean def, String comment) {
            this.category = category;
            this.key = key;
            this.def = def;
            this.comment = comment;
        }

        public boolean get() {
            if (COMMON_CONFIGURATION == null) {
                return def;
            }
            return COMMON_CONFIGURATION.get(category, key, def, comment).getBoolean();
        }
    }

    public static final class FCStringList {
        private final String category;
        private final String key;
        private final String[] def;
        private final String comment;

        public FCStringList(String category, String key, String[] def, String comment) {
            this.category = category;
            this.key = key;
            this.def = def != null ? def.clone() : new String[0];
            this.comment = comment;
        }

        public List<? extends String> get() {
            if (COMMON_CONFIGURATION == null) {
                return Arrays.asList(def);
            }
            return Arrays.asList(COMMON_CONFIGURATION.get(category, key, def, comment).getStringList());
        }
    }
}
