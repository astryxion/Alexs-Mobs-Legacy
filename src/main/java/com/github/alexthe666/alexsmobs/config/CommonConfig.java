package com.github.alexthe666.alexsmobs.config;

/** Forge 1.12.2; mirrors original 1.16 common config keys and defaults. */
public class CommonConfig {

    private static final String G = "general";
    private static final String S = "spawning";
    private static final String U = "uniqueSpawning";

    public final ConfigHolder.FCDouble lavaOpacity = d("G", "lavaOpacity", 0.65, 0.01, 1, "Lava Opacity for the Lava Vision Potion.");
    public final ConfigHolder.FCBool shadersCompat = b("G", "shadersCompat", false, "Whether to disable certain aspects of the Lava Vision Potion. Enable if issues with shaders persist.");
    public final ConfigHolder.FCBool neutralBoneSerpents = b("G", "neutralBoneSerpents", false, "Whether bone serpents are neutral or hostile.");
    public final ConfigHolder.FCBool lavaBottleEnabled = b("G", "lavaBottleEnabled", true, "Whether lava can be bottled with a right click of a glass bottle.");
    public final ConfigHolder.FCBool spidersAttackFlies = b("G", "spidersAttackFlies", true, "Whether spiders should target fly mobs.");
    public final ConfigHolder.FCBool wolvesAttackMoose = b("G", "wolvesAttackMoose", true, "Whether wolves should target moose mobs.");
    public final ConfigHolder.FCBool polarBearsAttackSeals = b("G", "polarBearsAttackSeals", true, "Whether polar bears should target seal mobs.");
    public final ConfigHolder.FCBool bananasDropFromLeaves = b("G", "bananasDropFromLeaves", true, "Whether bananas should drop from blocks tagged with #alexsmobs:drops_bananas");
    public final ConfigHolder.FCInt bananaChance = i("G", "bananaChance", 200, 0, 2147483647, "1 out of this number chance for leaves to drop a banana when broken. Fortune is automatically factored in");
    public final ConfigHolder.FCInt grizzlyBearSpawnWeight = i("S", "grizzlyBearSpawnWeight", 8, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt grizzlyBearSpawnRolls = i("S", "grizzlyBearSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt roadrunnerSpawnWeight = i("S", "roadrunnerSpawnWeight", 9, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt roadrunnerSpawnRolls = i("S", "roadrunnerSpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt boneSerpentSpawnWeight = i("S", "boneSerpentSpawnWeight", 8, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt boneSeprentSpawnRolls = i("S", "boneSeprentSpawnRolls", 40, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt gazelleSpawnWeight = i("S", "gazelleSpawnWeight", 40, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt gazelleSpawnRolls = i("S", "gazelleSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt crocodileSpawnWeight = i("S", "crocodileSpawnWeight", 40, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt crocSpawnRolls = i("S", "crocSpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt flySpawnWeight = i("S", "flySpawnWeight", 3, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt flySpawnRolls = i("S", "flySpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt hummingbirdSpawnWeight = i("S", "hummingbirdSpawnWeight", 39, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt hummingbirdSpawnRolls = i("S", "hummingbirdSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt orcaSpawnWeight = i("S", "orcaSpawnWeight", 2, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt orcaSpawnRolls = i("S", "orcaSpawnRolls", 6, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt sunbirdSpawnWeight = i("S", "sunbirdSpawnWeight", 2, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt sunbirdSpawnRolls = i("S", "sunbirdSpawnRolls", 15, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt gorillaSpawnWeight = i("S", "gorillaSpawnWeight", 50, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt gorillaSpawnRolls = i("S", "gorillaSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt crimsonMosquitoSpawnWeight = i("S", "crimsonMosquitoSpawnWeight", 15, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt crimsonMosquitoSpawnRolls = i("S", "crimsonMosquitoSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt rattlesnakeSpawnWeight = i("S", "rattlesnakeSpawnWeight", 12, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt rattlesnakeSpawnRolls = i("S", "rattlesnakeSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt endergradeSpawnWeight = i("S", "endergradeSpawnWeight", 10, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt endergradeSpawnRolls = i("S", "endergradeSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt hammerheadSharkSpawnWeight = i("S", "hammerheadSharkSpawnWeight", 8, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt hammerheadSharkSpawnRolls = i("S", "hammerheadSharkSpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt lobsterSpawnWeight = i("S", "lobsterSpawnWeight", 7, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt lobsterSpawnRolls = i("S", "lobsterSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt komodoDragonSpawnWeight = i("S", "komodoDragonSpawnWeight", 4, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt komodoDragonSpawnRolls = i("S", "komodoDragonSpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt capuchinMonkeySpawnWeight = i("S", "capuchinMonkeySpawnWeight", 55, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt capuchinMonkeySpawnRolls = i("S", "capuchinMonkeySpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt caveCentipedeSpawnWeight = i("S", "caveCentipedeSpawnWeight", 8, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt caveCentipedeSpawnRolls = i("S", "caveCentipedeSpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt caveCentipedeSpawnHeight = i("G", "caveCentipedeSpawnHeight", 30, 0, 256, "Maximum world y-level that cave centipedes can spawn at");
    public final ConfigHolder.FCInt warpedToadSpawnWeight = i("S", "warpedToadSpawnWeight", 80, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt warpedToadSpawnRolls = i("S", "warpedToadSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt mooseSpawnWeight = i("S", "mooseSpawnWeight", 9, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt mooseSpawnRolls = i("S", "mooseSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt bisonSpawnWeight = i("S", "bisonSpawnWeight", 9, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt bisonSpawnRolls = i("S", "bisonSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt rhinocerosSpawnWeight = i("S", "rhinocerosSpawnWeight", 24, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt rhinocerosSpawnRolls = i("S", "rhinocerosSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt skunkSpawnWeight = i("S", "skunkSpawnWeight", 7, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt skunkSpawnRolls = i("S", "skunkSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt tusklinSpawnWeight = i("S", "tusklinSpawnWeight", 18, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt tusklinSpawnRolls = i("S", "tusklinSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt anteaterSpawnWeight = i("S", "anteaterSpawnWeight", 7, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt anteaterSpawnRolls = i("S", "anteaterSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt mimicubeSpawnWeight = i("S", "mimicubeSpawnWeight", 40, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt mimicubeSpawnRolls = i("S", "mimicubeSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt raccoonSpawnWeight = i("S", "raccoonSpawnWeight", 10, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt raccoonSpawnRolls = i("S", "raccoonSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt blobfishSpawnWeight = i("S", "blobfishSpawnWeight", 30, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt blobfishSpawnRolls = i("S", "blobfishSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt blobfishSpawnHeight = i("G", "blobfishSpawnHeight", 38, 0, 256, "Maximum world y-level that blobfish can spawn at");
    public final ConfigHolder.FCInt sealSpawnWeight = i("S", "sealSpawnWeight", 30, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt sealSpawnRolls = i("S", "sealSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt cockroachSpawnWeight = i("S", "cockroachSpawnWeight", 4, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt cockroachSpawnRolls = i("S", "cockroachSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt shoebillSpawnWeight = i("S", "shoebillSpawnWeight", 10, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt shoebillSpawnRolls = i("S", "shoebillSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt elephantSpawnWeight = i("S", "elephantSpawnWeight", 30, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt elephantSpawnRolls = i("S", "elephantSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt soulVultureSpawnWeight = i("S", "soulVultureSpawnWeight", 30, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt soulVultureSpawnRolls = i("S", "soulVultureSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt snowLeopardSpawnWeight = i("S", "snowLeopardSpawnWeight", 18, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt snowLeopardSpawnRolls = i("S", "snowLeopardSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt spectreSpawnWeight = i("S", "spectreSpawnWeight", 10, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt spectreSpawnRolls = i("S", "spectreSpawnRolls", 5, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt crowSpawnWeight = i("S", "crowSpawnWeight", 10, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt crowSpawnRolls = i("S", "crowSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt alligatorSnappingTurtleSpawnWeight = i("S", "alligatorSnappingTurtleSpawnWeight", 20, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt alligatorSnappingTurtleSpawnRolls = i("S", "alligatorSnappingTurtleSpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt mungusSpawnWeight = i("S", "mungusSpawnWeight", 4, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt mungusSpawnRolls = i("S", "mungusSpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt mantisShrimpSpawnWeight = i("S", "mantisShrimpSpawnWeight", 15, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt mantisShrimpSpawnRolls = i("S", "mantisShrimpSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt gusterSpawnWeight = i("S", "gusterSpawnWeight", 35, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt gusterSpawnRolls = i("S", "gusterSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt warpedMoscoSpawnWeight = i("S", "warpedMoscoSpawnWeight", 1, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn NOTE: By default the warped mosco does not spawn in any biomes.");
    public final ConfigHolder.FCInt warpedMoscoSpawnRolls = i("S", "warpedMoscoSpawnRolls", 1000, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt straddlerSpawnWeight = i("S", "straddlerSpawnWeight", 85, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt straddlerSpawnRolls = i("S", "straddlerSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt stradpoleSpawnWeight = i("S", "stradpoleSpawnWeight", 10, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt stradpoleSpawnRolls = i("S", "stradpoleSpawnRolls", 3, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt emuSpawnWeight = i("S", "emuSpawnWeight", 20, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt emuSpawnRolls = i("S", "emuSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt platypusSpawnWeight = i("S", "platypusSpawnWeight", 30, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt platypusSpawnRolls = i("S", "platypusSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt dropbearSpawnWeight = i("S", "dropbearSpawnWeight", 19, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt dropbearSpawnRolls = i("S", "dropbearSpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt tasmanianDevilSpawnWeight = i("S", "tasmanianDevilSpawnWeight", 10, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt tasmanianDevilSpawnRolls = i("S", "tasmanianDevilSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt kangarooSpawnWeight = i("S", "kangarooSpawnWeight", 25, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt kangarooSpawnRolls = i("S", "kangarooSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt cachalotWhaleSpawnWeight = i("S", "cachalotWhaleSpawnWeight", 2, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt cachalotWhaleSpawnRolls = i("S", "cachalotWhaleSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt enderiophageSpawnWeight = i("S", "enderiophageSpawnWeight", 4, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt enderiophageSpawnRolls = i("S", "enderiophageSpawnRolls", 2, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt baldEagleSpawnWeight = i("S", "baldEagleSpawnWeight", 15, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt baldEagleSpawnRolls = i("S", "baldEagleSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt tigerSpawnWeight = i("S", "tigerSpawnWeight", 30, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt tigerSpawnRolls = i("S", "tigerSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt tarantulaHawkSpawnWeight = i("S", "tarantulaHawkSpawnWeight", 6, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt tarantulaHawkSpawnRolls = i("S", "tarantulaHawkSpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt voidWormSpawnWeight = i("S", "voidWormSpawnWeight", 0, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt voidWormSpawnRolls = i("S", "voidWormSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt frilledSharkSpawnWeight = i("S", "frilledSharkSpawnWeight", 11, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt frilledSharkSpawnRolls = i("S", "frilledSharkSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt mimicOctopusSpawnWeight = i("S", "mimicOctopusSpawnWeight", 9, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt mimicOctopusSpawnRolls = i("S", "mimicOctopusSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt seagullSpawnWeight = i("S", "seagullSpawnWeight", 21, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt seagullSpawnRolls = i("S", "seagullSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt blueJaySpawnWeight = i("S", "blueJaySpawnWeight", 16, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt blueJaySpawnRolls = i("S", "blueJaySpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt bananaSlugSpawnWeight = i("S", "bananaSlugSpawnWeight", 14, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt bananaSlugSpawnRolls = i("S", "bananaSlugSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt anacondaSpawnWeight = i("S", "anacondaSpawnWeight", 12, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt anacondaSpawnRolls = i("S", "anacondaSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt laviathanSpawnWeight = i("S", "laviathanSpawnWeight", 15, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt laviathanSpawnRolls = i("S", "laviathanSpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt giantSquidSpawnWeight = i("S", "giantSquidSpawnWeight", 3, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt giantSquidSpawnRolls = i("S", "giantSquidSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt jerboaSpawnWeight = i("S", "jerboaSpawnWeight", 12, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt jerboaSpawnRolls = i("S", "jerboaSpawnRolls", 2, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt triopsSpawnWeight = i("S", "triopsSpawnWeight", 8, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt triopsSpawnRolls = i("S", "triopsSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt terrapinSpawnWeight = i("S", "terrapinSpawnWeight", 4, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt terrapinSpawnRolls = i("S", "terrapinSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt combJellySpawnWeight = i("S", "combJellySpawnWeight", 5, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt combJellySpawnRolls = i("S", "combJellySpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt cosmicCodSpawnWeight = i("S", "cosmicCodSpawnWeight", 5, 0, 1000, "Spawn enable gate for Cosmic Cod. 0 = disable spawn");
    public final ConfigHolder.FCInt cosmicCodSpawnRolls = i("S", "cosmicCodSpawnRolls", 6, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt pupfishSpawnWeight = i("S", "pupfishSpawnWeight", 23, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn (NOTE: this mob spawn is restricted exclusively to one chunk, see below)");
    public final ConfigHolder.FCInt pupfishSpawnRolls = i("S", "pupfishSpawnRolls", 2, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt rainFrogSpawnWeight = i("S", "rainFrogSpawnWeight", 10, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt rainFrogSpawnRolls = i("S", "rainFrogSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt potooSpawnWeight = i("S", "potooSpawnWeight", 15, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt potooSpawnRolls = i("S", "potooSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt toucanSpawnWeight = i("S", "toucanSpawnWeight", 23, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt toucanSpawnRolls = i("S", "toucanSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt flyingFishSpawnWeight = i("S", "flyingFishSpawnWeight", 8, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt flyingFishSpawnRolls = i("S", "flyingFishSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt catfishSpawnWeight = i("S", "catfishSpawnWeight", 4, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt catfishSpawnRolls = i("S", "catfishSpawnRolls", 1, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt sugarGliderSpawnWeight = i("S", "sugarGliderSpawnWeight", 15, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt sugarGliderSpawnRolls = i("S", "sugarGliderSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt geladaMonkeySpawnWeight = i("S", "geladaMonkeySpawnWeight", 5, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt geladaMonkeySpawnRolls = i("S", "geladaMonkeySpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt manedWolfSpawnWeight = i("S", "manedWolfSpawnWeight", 8, 0, 1000, "Spawn enable gate for Maned Wolf. 0 = disable spawn");
    public final ConfigHolder.FCInt manedWolfSpawnRolls = i("S", "manedWolfSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt caimanSpawnWeight = i("S", "caimanSpawnWeight", 29, 0, 1000, "Spawn Weight, added to a pool of other mobs for each biome. Higher number = higher chance of spawning. 0 = disable spawn");
    public final ConfigHolder.FCInt caimanSpawnRolls = i("S", "caimanSpawnRolls", 0, 0, 2147483647, "Random roll chance to enable mob spawning. Higher number = lower chance of spawning");
    public final ConfigHolder.FCInt geladaMonkeySpawnHeight = i("G", "geladaMonkeySpawnHeight", 80, -64, 256, "Minimum world y-level that gelada monkeys can spawn at");
    public final ConfigHolder.FCBool restrictPupfishSpawns = b("G", "restrictPupfishSpawns", true, "Whether to restrict all pupfish spawns to one chunk (similar to real life) or have them only obey their spawn config.");
    public final ConfigHolder.FCInt pupfishChunkSpawnDistance = i("G", "pupfishChunkSpawnDistance", 2000, 2, 1000000000, "The maximum distance a pupfish spawn chunk is from world spawn (0, 0) in blocks.");
    public final ConfigHolder.FCBool catsAndFoxesAttackJerboas = b("G", "catsAndFoxesAttackJerboas", true, "Whether cats, ocelots and foxes should target jerboa mobs.");
    public final ConfigHolder.FCBool giveBookOnStartup = b("G", "giveBookOnStartup", true, "Whether all players should get an Animal Dictionary when joining the world for the first time.");
    public final ConfigHolder.FCBool mimicubeSpawnInEndCity = b("G", "mimicubeSpawnInEndCity", true, "Whether mimicubes spawns should be restricted solely to the end city structure or to whatever biome is specified in their respective biome config.");
    public final ConfigHolder.FCBool mimicreamRepair = b("G", "mimicreamRepair", true, "Whether mimicream can be used to duplicate items.");
    public final ConfigHolder.FCStringList mimicreamBlacklist = sl("G", "mimicreamBlacklist", new String[] { "alexsmobs:blood_sprayer", "alexsmobs:hemolymph_blaster" }, "Blacklist for items that mimicream cannot make a copy of. Ex: \"minecraft:stone_sword\", \"alexsmobs:blood_sprayer\"");
    public final ConfigHolder.FCBool raccoonStealFromChests = b("G", "raccoonStealFromChests", true, "Whether wild raccoons steal food from chests.");
    public final ConfigHolder.FCBool fishOilMeme = b("G", "fishOilMeme", true, "Whether fish oil gives players a special levitation effect.");
    public final ConfigHolder.FCBool soulVultureSpawnOnFossil = b("G", "soulVultureSpawnOnFossil", true, "Whether soul vulture spawns should be restricted solely to the nether fossil structure or to whatever biome is specified in their respective biome config.");
    public final ConfigHolder.FCBool acaciaBlossomsDropFromLeaves = b("G", "acaciaBlossomsDropFromLeaves", true, "Whether acacia blossoms should drop from blocks tagged with #alexsmobs:drops_acacia_blossoms");
    public final ConfigHolder.FCBool wanderingTraderOffers = b("G", "wanderingTraderOffers", true, "Whether wandering traders offer items like acacia blossoms, mosquito larva, crocodile egg, etc.");
    public final ConfigHolder.FCInt mungusBiomeTransformationType = i("G", "mungusBiomeTransformationType", 2, 0, 2, "0 = no mungus biome transformation. 1 = mungus changes blocks, but not chunk biome. 2 = mungus transforms blocks and biome of chunk.");
    public final ConfigHolder.FCStringList mungusBiomeMatches = sl("G", "mungusBiomeMatches", new String[] { "minecraft:red_mushroom|minecraft:mushroom_fields|minecraft:mycelium", "minecraft:brown_mushroom|minecraft:mushroom_fields|minecraft:mycelium", "minecraft:crimson_fungus|minecraft:crimson_forest|minecraft:crimson_nylium", "minecraft:warped_fungus|minecraft:warped_forest|minecraft:warped_nylium" }, "List of all mungus mushrooms, biome transformations and surface blocks. Each is seperated by a |. Add an entry with a block registry name, biome registry name, and block registry name(for the ground).");
    public final ConfigHolder.FCBool limitGusterSpawnsToWeather = b("G", "limitGusterSpawnsToWeather", true, "Whether guster spawns are limited to when it is raining/thundering.");
    public final ConfigHolder.FCBool warpedMoscoTransformation = b("G", "warpedMoscoTransformation", true, "Whether Crimson Mosquitoes can transform into Warped Moscos if attacking a Mungus or any listed creature.");
    public final ConfigHolder.FCStringList warpedMoscoMobTriggers = sl("G", "warpedMoscoMobTriggers", new String[] { "" }, "List of extra(non mungus) mobs that will trigger a crimson mosquito to become a warped mosquito. Ex: \"minecraft:mooshroom\", \"alexsmobs:warped_toad\"");
    public final ConfigHolder.FCBool straddleboardEnchants = b("G", "straddleboardEnchants", true, "True if straddleboard enchants are enabled.");
    public final ConfigHolder.FCBool emuTargetSkeletons = b("G", "emuTargetSkeletons", true, "Whether emu should target skeletons.");
    public final ConfigHolder.FCDouble emuPantsDodgeChance = d("G", "emuPantsDodgeChance", 0.45, 0, 1, "Percent chance for emu leggings to dodge projectile attacks.");
    public final ConfigHolder.FCDouble leafcutterAntFungusGrowChance = d("G", "leafcutterAntFungusGrowChance", 0.3, 0, 1, "Percent chance for fungus to grow per each leaf a leafcutter ant returns to the colony.");
    public final ConfigHolder.FCInt leafcutterAntRepopulateFeedings = i("G", "leafcutterAntRepopulateFeedings", 25, 2, 100000, "How many feedings of leaves does a leafcutter colony need in order to regain a worker ant, if below half the max members.");
    public final ConfigHolder.FCInt leafcutterAntColonySize = i("G", "leafcutterAntColonySize", 20, 2, 100000, "Max number of ant entities allowed inside a leafcutter anthill.");
    public final ConfigHolder.FCDouble leafcutterAntBreakLeavesChance = d("G", "leafcutterAntBreakLeavesChance", 0.2, 0, 1, "Percent chance for leafcutter ants to break leaves blocks when harvesting. Set to zero so that they can not break any blocks.");
    public final ConfigHolder.FCBool beachedCachalotWhales = b("U", "beachedCachalotWhales", true, "Whether to enable beached cachalot whales to spawn on beaches during thunder storms.");
    public final ConfigHolder.FCDouble cachalotVolume = d("G", "cachalotVolume", 3, 0, 10, "Relative volume of cachalot whales compared to other animals. Note that irl they are the loudest animal. Turn this down if you find their clicks annoying.");
    public final ConfigHolder.FCInt beachedCachalotWhaleSpawnChance = i("U", "beachedCachalotWhaleSpawnChance", 5, 0, 100, "Percent chance increase for each failed attempt to spawn a beached cachalot whale. Higher value = more spawns.");
    public final ConfigHolder.FCInt beachedCachalotWhaleSpawnDelay = i("U", "beachedCachalotWhaleSpawnDelay", 24000, 0, 2147483647, "Delay (in ticks) between attempts to spawn beached cachalot whales. Default is a single day. Works like wandering traders.");
    public final ConfigHolder.FCDouble leafcutterAnthillSpawnChance = d("U", "leafcutterAnthillSpawnChance", 0.005, 0, 1, "Percent chance for leafcutter anthills to spawn as world gen in each chunk. Set to zero to disable spawning.");
    public final ConfigHolder.FCBool falconryTeleportsBack = b("G", "falconryTeleportsBack", false, "Makes eagles teleport back to their owner if they get stuck during controlled flight. Useful for when playing with the Optifine mod, since this mod is the fault of many issues with the falconry system.");
    public final ConfigHolder.FCBool fireproofTarantulaHawk = b("G", "fireproofTarantulaHawk", false, "Makes Tarantula Hawks fireproof, perfect if you also want these guys to spawn in the nether.");
    public final ConfigHolder.FCBool voidWormSummonable = b("G", "voidWormSummonable", true, "Whether the void worm boss is summonable or not, via the mysterious worm item.");
    public final ConfigHolder.FCStringList voidWormSpawnDimensions = sl("G", "voidWormSpawnDimensions", new String[] { "minecraft:the_end" }, "List of dimensions in which spawning void worms via mysterious worm items is allowed.");
    public final ConfigHolder.FCDouble voidWormDamageModifier = d("G", "voidWormDamageModifier", 1, 0, 100, "All void worm damage is scaled to this.");
    public final ConfigHolder.FCDouble voidWormMaxHealth = d("G", "voidWormMaxHealth", 160, 0, 1000000, "Max Health of the void worm boss.");
    public final ConfigHolder.FCBool seagullStealing = b("G", "seagullStealing", true, "Whether seagulls should steal food out of players hotbar slots.");
    public final ConfigHolder.FCStringList seagullStealingBlacklist = sl("G", "seagullStealingBlacklist", new String[] {  }, "List of items that seagulls cannot take from players.");
    public final ConfigHolder.FCBool clingingFlipEffect = b("G", "clingingFlipEffect", false, "Whether the Clinging Potion effect should flip the screen. Warning: may cause nausea.");
    public final ConfigHolder.FCDouble rainbowGlassFidelity = d("G", "rainbowGlassFidelity", 16.0, 1.0, 10000.0, "The visual zoom of the rainbow pattern on the rainbow glass block. Higher number = bigger pattern.");

    private static ConfigHolder.FCInt i(String catConst, String key, int def, int min, int max, String comment) {
        return new ConfigHolder.FCInt(mapCat(catConst), key, def, min, max, comment);
    }

    private static ConfigHolder.FCDouble d(String catConst, String key, double def, double min, double max, String comment) {
        return new ConfigHolder.FCDouble(mapCat(catConst), key, def, min, max, comment);
    }

    private static ConfigHolder.FCBool b(String catConst, String key, boolean def, String comment) {
        return new ConfigHolder.FCBool(mapCat(catConst), key, def, comment);
    }

    private static ConfigHolder.FCStringList sl(String catConst, String key, String[] def, String comment) {
        return new ConfigHolder.FCStringList(mapCat(catConst), key, def, comment);
    }

    private static String mapCat(String c) {
        if ("G".equals(c)) return G;
        if ("U".equals(c)) return U;
        return S;
    }

    public CommonConfig() {
    }
}
