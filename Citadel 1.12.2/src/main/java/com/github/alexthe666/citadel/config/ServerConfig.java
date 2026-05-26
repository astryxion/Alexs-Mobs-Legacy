package com.github.alexthe666.citadel.config;

public class ServerConfig {
   public static boolean citadelEntityTrack;
   public static double chunkGenSpawnModifierVal = (double)1.0F;
   public final boolean citadelEntityTracker;
   public final double chunkGenSpawnModifier;

   public ServerConfig() {
      this.citadelEntityTracker = true;
      this.chunkGenSpawnModifier = (double)1.0F;
      citadelEntityTrack = this.citadelEntityTracker;
      chunkGenSpawnModifierVal = this.chunkGenSpawnModifier;
   }
}
