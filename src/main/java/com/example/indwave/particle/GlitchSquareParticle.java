package com.example.indwave.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Частица-квадрат для визуала стены участка.
 * Живёт недолго (0.5-1 сек), почти не двигается, плавно гаснет.
 * Количество и место спавна контролируются на стороне сервера/адаптера,
 * а не здесь - этот класс только описывает, как частица выглядит и живёт.
 */
public class GlitchSquareParticle extends TextureSheetParticle {

    protected GlitchSquareParticle(ClientLevel level, double x, double y, double z,
                                   double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        // Почти неподвижна, лёгкий дрейф, без гравитации (это не падающая пыль, а эффект стены)
        this.gravity = 0.0f;
        this.xd = xSpeed * 0.02;
        this.yd = ySpeed * 0.02 + 0.01; // едва заметно вверх
        this.zd = zSpeed * 0.02;

        float roll = this.random.nextFloat();

        if (roll < 0.70f) {
            // маленькие
            this.quadSize = 0.15f + this.random.nextFloat() * 0.25f;
        }
        else if (roll < 0.95f) {
            // средние
            this.quadSize = 0.75f + this.random.nextFloat() * 1.0f;
        }
        else {
            // большие
            this.quadSize = 2.0f + this.random.nextFloat() * 2.0f;
        }

        if (this.quadSize < 0.5f) {
            this.lifetime = 10 + this.random.nextInt(10);
        }
        else if (this.quadSize < 2.0f) {
            this.lifetime = 20 + this.random.nextInt(20);
        }
        else {
            this.lifetime = 40 + this.random.nextInt(40);
        }

        this.alpha = 0.7f + this.random.nextFloat() * 0.3f;
        this.hasPhysics = false; // не взаимодействует с блоками, чтобы не тормозить на границе
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        // Плавное угасание к концу жизни, а не резкое исчезновение
        if (this.age > this.lifetime / 2) {
            float remaining = 1.0f - (float) (this.age - this.lifetime / 2) / (this.lifetime / 2f);
            this.alpha = Math.max(0f, remaining);
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            GlitchSquareParticle particle = new GlitchSquareParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}