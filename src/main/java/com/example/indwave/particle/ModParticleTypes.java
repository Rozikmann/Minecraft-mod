package com.example.indwave.particle;

import com.example.indwave.IndwaveMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, IndwaveMod.MODID);

    public static final RegistryObject<SimpleParticleType> GLITCH_SQUARE =
            PARTICLE_TYPES.register("glitch_square", () -> new SimpleParticleType(false));
}