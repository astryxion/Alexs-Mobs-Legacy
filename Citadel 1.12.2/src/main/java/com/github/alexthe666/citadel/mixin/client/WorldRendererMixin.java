package com.github.alexthe666.citadel.mixin.client;

import com.github.alexthe666.citadel.client.event.EventGetOutlineColor;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class WorldRendererMixin {
   public static int citadel_getTeamColor(Entity entity) {
      int defaultColor = entity.getDisplayName().getStyle().getColor() != null ? entity.getDisplayName().getStyle().getColor().getColorIndex() : 16777215;
      EventGetOutlineColor event = new EventGetOutlineColor(entity, defaultColor);
      MinecraftForge.EVENT_BUS.post(event);
      int color = defaultColor;
      if (event.getResult() == Event.Result.ALLOW) {
         color = event.getColor();
      }

      return color;
   }
}
