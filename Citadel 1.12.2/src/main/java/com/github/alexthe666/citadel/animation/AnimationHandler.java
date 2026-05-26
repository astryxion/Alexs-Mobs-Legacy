package com.github.alexthe666.citadel.animation;

import com.github.alexthe666.citadel.Citadel;
import com.github.alexthe666.citadel.server.message.AnimationMessage;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.ArrayUtils;

public enum AnimationHandler {
   INSTANCE;

   public <T extends Entity & IAnimatedEntity> void sendAnimationMessage(T entity, Animation animation) {
      if (!entity.world.isRemote) {
         ((IAnimatedEntity)entity).setAnimation(animation);
         Citadel.sendMSGToAll(new AnimationMessage(entity.getEntityId(), ArrayUtils.indexOf(((IAnimatedEntity)entity).getAnimations(), animation)));
      }
   }

   public <T extends Entity & IAnimatedEntity> void updateAnimations(T entity) {
      if (((IAnimatedEntity)entity).getAnimation() == null) {
         ((IAnimatedEntity)entity).setAnimation(IAnimatedEntity.NO_ANIMATION);
      } else if (((IAnimatedEntity)entity).getAnimation() != IAnimatedEntity.NO_ANIMATION) {
         if (((IAnimatedEntity)entity).getAnimationTick() == 0) {
            AnimationEvent event = new AnimationEvent.Start(entity, ((IAnimatedEntity)entity).getAnimation());
            if (!MinecraftForge.EVENT_BUS.post(event)) {
               this.sendAnimationMessage(entity, event.getAnimation());
            }
         }

         if (((IAnimatedEntity)entity).getAnimationTick() < ((IAnimatedEntity)entity).getAnimation().getDuration()) {
            ((IAnimatedEntity)entity).setAnimationTick(((IAnimatedEntity)entity).getAnimationTick() + 1);
            MinecraftForge.EVENT_BUS.post(new AnimationEvent.Tick(entity, ((IAnimatedEntity)entity).getAnimation(), ((IAnimatedEntity)entity).getAnimationTick()));
         }

         if (((IAnimatedEntity)entity).getAnimationTick() == ((IAnimatedEntity)entity).getAnimation().getDuration()) {
            ((IAnimatedEntity)entity).setAnimationTick(0);
            ((IAnimatedEntity)entity).setAnimation(IAnimatedEntity.NO_ANIMATION);
         }
      }

   }
}
