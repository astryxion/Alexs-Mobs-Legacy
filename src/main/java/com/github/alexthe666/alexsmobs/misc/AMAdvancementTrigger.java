package com.github.alexthe666.alexsmobs.misc;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Forge 1.12.2 custom advancement trigger (1.16 used {@code AbstractCriterionTrigger} / loot condition serializers).
 */
public class AMAdvancementTrigger implements ICriterionTrigger<AMAdvancementTrigger.Instance> {

    public final ResourceLocation resourceLocation;
    private final Map<PlayerAdvancements, Listeners> listeners = new HashMap<>();

    public AMAdvancementTrigger(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    @Override
    public ResourceLocation getId() {
        return resourceLocation;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        Listeners listeners = this.listeners.computeIfAbsent(playerAdvancementsIn, Listeners::new);
        listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        Listeners listeners = this.listeners.get(playerAdvancementsIn);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(resourceLocation);
    }

    public void trigger(EntityPlayerMP player) {
        Listeners listeners = this.listeners.get(player.getAdvancements());
        if (listeners != null) {
            listeners.trigger();
        }
    }

    public static class Instance extends AbstractCriterionInstance {

        public Instance(ResourceLocation id) {
            super(id);
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listeners = new HashSet<>();

        Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public void add(Listener<Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(Listener<Instance> listener) {
            this.listeners.remove(listener);
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void trigger() {
            List<Listener<Instance>> list = null;
            for (Listener<Instance> listener : this.listeners) {
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(listener);
            }
            if (list != null) {
                for (Listener<Instance> listener : list) {
                    listener.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }
}
