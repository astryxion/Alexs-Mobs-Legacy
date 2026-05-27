package com.github.alexthe666.alexsmobs.client.particle;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 1.12.2 particle ids + atlas sprites (1.16 used {@link net.minecraft.particles.BasicParticleType} + factory registration).
 */
@Mod.EventBusSubscriber(modid = AlexsMobs.MODID)
public class AMParticleRegistry {

    private static int nextParticleId = EnumParticleTypes.values().length;

    public static int GUSTER_SAND_SPIN;
    public static int GUSTER_SAND_SHOT;
    public static int GUSTER_SAND_SPIN_RED;
    public static int GUSTER_SAND_SHOT_RED;
    public static int GUSTER_SAND_SPIN_SOUL;
    public static int GUSTER_SAND_SHOT_SOUL;
    public static int HEMOLYMPH;
    public static int PLATYPUS_SENSE;
    public static int WHALE_SPLASH;
    public static int DNA;
    public static int SHOCKED;
    public static int WORM_PORTAL;
    public static int INVERT_DIG;
    public static int TEETH_GLINT;
    public static int SMELLY;
    public static int BIRD_SONG;

    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] GENERIC_0_7;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] SPLASH_0_3;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite HEMOLYMPH_SPRITE;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] DNA_SPRITES;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] PLATYPUS_SENSE_SPRITES;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite SHOCKED_SPRITE;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite WORM_PORTAL_SPRITE;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] INVERT_DIG_SPRITES;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite TEETH_GLINT_SPRITE;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] SMELLY_SPRITES;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] BIRD_SONG_SPRITES;
    @SideOnly(Side.CLIENT)
    private static final Map<Integer, IParticleFactory> PARTICLE_FACTORIES = new HashMap<>();
    @SideOnly(Side.CLIENT)
    private static boolean factoriesRegistered;

    public static void spawnParticle(World world, int particleId, double x, double y, double z, double motionX, double motionY, double motionZ) {
        if (world.isRemote) {
            spawnParticleClient(world, particleId, x, y, z, motionX, motionY, motionZ);
        }
    }

    @SideOnly(Side.CLIENT)
    private static void spawnParticleClient(World world, int particleId, double x, double y, double z, double motionX, double motionY, double motionZ) {
        if (!factoriesRegistered) {
            return;
        }
        IParticleFactory factory = PARTICLE_FACTORIES.get(particleId);
        if (factory != null) {
            Particle particle = factory.createParticle(particleId, world, x, y, z, motionX, motionY, motionZ, new int[0]);
            if (particle != null) {
                Minecraft.getMinecraft().effectRenderer.addEffect(particle);
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        GENERIC_0_7 = registerVanillaSprites(map, "generic", 0, 7);
        SPLASH_0_3 = registerVanillaSprites(map, "splash", 0, 3);
        HEMOLYMPH_SPRITE = map.registerSprite(new ResourceLocation(AlexsMobs.MODID, "entity/hemolymph"));
        DNA_SPRITES = registerAlexSprites(map, "dna_0", "dna_1", "dna_2", "dna_3", "dna_4");
        PLATYPUS_SENSE_SPRITES = registerAlexSprites(map, "platypus_sense_0", "platypus_sense_1", "platypus_sense_2");
        SHOCKED_SPRITE = map.registerSprite(new ResourceLocation(AlexsMobs.MODID, "particle/shocked"));
        WORM_PORTAL_SPRITE = map.registerSprite(new ResourceLocation(AlexsMobs.MODID, "particle/worm_portal"));
        INVERT_DIG_SPRITES = registerAlexSprites(map, "invert_dig_0", "invert_dig_1", "invert_dig_2", "invert_dig_3", "invert_dig_4", "invert_dig_5", "invert_dig_6", "invert_dig_7", "invert_dig_8", "invert_dig_9");
        TEETH_GLINT_SPRITE = map.registerSprite(new ResourceLocation(AlexsMobs.MODID, "particle/teeth_glint"));
        SMELLY_SPRITES = registerAlexSprites(map, "smelly_0", "smelly_1", "smelly_2", "smelly_3", "smelly_4", "smelly_5", "smelly_6", "smelly_7");
        BIRD_SONG_SPRITES = registerAlexSprites(map, "note_2");
    }

    @SideOnly(Side.CLIENT)
    public static void registerFactories() {
        if (factoriesRegistered) {
            return;
        }
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().effectRenderer == null) {
            AlexsMobs.LOGGER.error("Cannot register Alex's Mobs particles: ParticleManager not ready yet");
            return;
        }
        ParticleManager manager = Minecraft.getMinecraft().effectRenderer;
        GUSTER_SAND_SPIN = nextParticleId++;
        registerFactory(manager, GUSTER_SAND_SPIN, factorySpin(0));
        GUSTER_SAND_SHOT = nextParticleId++;
        registerFactory(manager, GUSTER_SAND_SHOT, factoryShot(0));
        GUSTER_SAND_SPIN_RED = nextParticleId++;
        registerFactory(manager, GUSTER_SAND_SPIN_RED, factorySpin(1));
        GUSTER_SAND_SHOT_RED = nextParticleId++;
        registerFactory(manager, GUSTER_SAND_SHOT_RED, factoryShot(1));
        GUSTER_SAND_SPIN_SOUL = nextParticleId++;
        registerFactory(manager, GUSTER_SAND_SPIN_SOUL, factorySpin(2));
        GUSTER_SAND_SHOT_SOUL = nextParticleId++;
        registerFactory(manager, GUSTER_SAND_SHOT_SOUL, factoryShot(2));
        HEMOLYMPH = nextParticleId++;
        registerFactory(manager, HEMOLYMPH, factoryHemolymph());
        PLATYPUS_SENSE = nextParticleId++;
        registerFactory(manager, PLATYPUS_SENSE, factoryPlatypus());
        WHALE_SPLASH = nextParticleId++;
        registerFactory(manager, WHALE_SPLASH, factoryWhaleSplash());
        DNA = nextParticleId++;
        registerFactory(manager, DNA, factoryDna());
        SHOCKED = nextParticleId++;
        registerFactory(manager, SHOCKED, factoryShocked());
        WORM_PORTAL = nextParticleId++;
        registerFactory(manager, WORM_PORTAL, factoryWormPortal());
        INVERT_DIG = nextParticleId++;
        registerFactory(manager, INVERT_DIG, factoryInvertDig());
        TEETH_GLINT = nextParticleId++;
        registerFactory(manager, TEETH_GLINT, factoryTeethGlint());
        SMELLY = nextParticleId++;
        registerFactory(manager, SMELLY, factorySmelly());
        BIRD_SONG = nextParticleId++;
        registerFactory(manager, BIRD_SONG, factoryBirdSong());
        factoriesRegistered = true;
        AlexsMobs.LOGGER.debug("Registered Alex's Mobs particle factories");
    }

    @SideOnly(Side.CLIENT)
    private static void registerFactory(ParticleManager manager, int id, IParticleFactory factory) {
        manager.registerParticle(id, factory);
        PARTICLE_FACTORIES.put(id, factory);
    }

    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite spriteByAge(TextureAtlasSprite[] sprites, int age, int maxAge) {
        if (sprites == null || sprites.length == 0) {
            return null;
        }
        int idx = Math.min(sprites.length - 1, age * sprites.length / Math.max(1, maxAge));
        return sprites[idx];
    }

    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite randomSprite(TextureAtlasSprite[] sprites, Random rand) {
        return sprites[rand.nextInt(sprites.length)];
    }

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite[] registerVanillaSprites(TextureMap map, String prefix, int from, int to) {
        // 1.12.2 has no generic_0..7 / splash_0..3 particle PNGs (1.14+); use stitched block atlas sprites for the same visual role.
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[to - from + 1];
        TextureAtlasSprite sprite = map.getAtlasSprite("generic".equals(prefix) ? "minecraft:blocks/soul_sand" : "minecraft:blocks/water_still");
        for (int i = from; i <= to; i++) {
            sprites[i - from] = sprite;
        }
        return sprites;
    }

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite[] registerAlexSprites(TextureMap map, String... names) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[names.length];
        for (int i = 0; i < names.length; i++) {
            sprites[i] = map.registerSprite(new ResourceLocation(AlexsMobs.MODID, "particle/" + names[i]));
        }
        return sprites;
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factorySpin(final int variant) {
        return (id, world, x, y, z, mx, my, mz, params) -> new ParticleGusterSandSpin(world, x, y, z, mx, my, mz, variant);
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factoryShot(final int variant) {
        return (id, world, x, y, z, mx, my, mz, params) -> new ParticleGusterSandShot(world, x, y, z, mx, my, mz, variant);
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factoryHemolymph() {
        return (id, world, x, y, z, mx, my, mz, params) -> new ParticleHemolymph(world, x, y, z, mx, my, mz);
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factoryPlatypus() {
        return (id, world, x, y, z, mx, my, mz, params) -> new ParticlePlatypus(world, x, y, z, mx, my, mz);
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factoryWhaleSplash() {
        return (id, world, x, y, z, mx, my, mz, params) -> new ParticleWhaleSplash(world, x, y, z, mx, my, mz);
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factoryDna() {
        return (id, world, x, y, z, mx, my, mz, params) -> new ParticleDna(world, x, y, z, mx, my, mz);
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factoryShocked() {
        return (id, world, x, y, z, mx, my, mz, params) -> new ParticleSimpleHeart(world, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factoryWormPortal() {
        return (id, world, x, y, z, mx, my, mz, params) -> new ParticleWormPortal(world, x, y, z, mx, my, mz);
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factoryInvertDig() {
        return (id, world, x, y, z, mx, my, mz, params) -> new ParticleInvertDig(world, x, y, z, mx);
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factoryTeethGlint() {
        return (id, world, x, y, z, mx, my, mz, params) -> new ParticleTeethGlint(world, x, y, z, mx, my, mz);
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factorySmelly() {
        return new ParticleSmelly.Factory();
    }

    @SideOnly(Side.CLIENT)
    private static IParticleFactory factoryBirdSong() {
        return (id, world, x, y, z, mx, my, mz, params) -> {
            TextureAtlasSprite sprite = BIRD_SONG_SPRITES != null && BIRD_SONG_SPRITES.length > 0
                    ? BIRD_SONG_SPRITES[0] : SHOCKED_SPRITE;
            return new ParticleBirdSong(world, x, y, z, mx, my, mz, sprite);
        };
    }
}
