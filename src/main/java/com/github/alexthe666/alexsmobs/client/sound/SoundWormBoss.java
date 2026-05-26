package com.github.alexthe666.alexsmobs.client.sound;

import com.github.alexthe666.alexsmobs.ClientProxy;
import com.github.alexthe666.alexsmobs.entity.EntityVoidWorm;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;

import java.util.Map;

public class SoundWormBoss extends MovingSound {
    private final EntityVoidWorm voidWorm;
    private int ticksExisted = 0;

    public SoundWormBoss(EntityVoidWorm worm) {
        super(AMSoundRegistry.MUSIC_WORMBOSS, SoundCategory.RECORDS);
        this.voidWorm = worm;
        this.attenuationType = AttenuationType.NONE;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 1.0F;
        this.xPosF = (float) this.voidWorm.posX;
        this.yPosF = (float) this.voidWorm.posY;
        this.zPosF = (float) this.voidWorm.posZ;
    }

    public boolean shouldPlaySound() {
        return !this.voidWorm.isSilent() && ClientProxy.WORMBOSS_SOUND_MAP.get(this.voidWorm.getEntityId()) == this;
    }

    public boolean isNearest() {
        float dist = 400;
        for (Map.Entry<Integer, SoundWormBoss> entry : ClientProxy.WORMBOSS_SOUND_MAP.entrySet()) {
            SoundWormBoss wormBoss = entry.getValue();
            if (wormBoss != this && distanceSq(wormBoss.xPosF, wormBoss.yPosF, wormBoss.zPosF) < dist * dist && wormBoss.shouldPlaySound()) {
                return false;
            }
        }
        return true;
    }

    public double distanceSq(double p_218140_1_, double p_218140_3_, double p_218140_5_) {
        double lvt_10_1_ = (double) this.xPosF - p_218140_1_;
        double lvt_12_1_ = (double) this.yPosF - p_218140_3_;
        double lvt_14_1_ = (double) this.zPosF - p_218140_5_;
        return lvt_10_1_ * lvt_10_1_ + lvt_12_1_ * lvt_12_1_ + lvt_14_1_ * lvt_14_1_;
    }

    @Override
    public void update() {
        if (ticksExisted % 100 == 0) {
            Minecraft.getMinecraft().getMusicTicker().playMusic(net.minecraft.client.audio.MusicTicker.MusicType.GAME);
        }
        if (this.voidWorm.isEntityAlive()) {
            this.volume = 1;
            this.pitch = 1;
            this.xPosF = (float) this.voidWorm.posX;
            this.yPosF = (float) this.voidWorm.posY;
            this.zPosF = (float) this.voidWorm.posZ;
        } else {
            this.donePlaying = true;
            ClientProxy.WORMBOSS_SOUND_MAP.remove(voidWorm.getEntityId());
        }
        ticksExisted++;
    }

}
