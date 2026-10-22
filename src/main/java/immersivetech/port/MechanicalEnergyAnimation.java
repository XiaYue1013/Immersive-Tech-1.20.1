package immersivetech.port;

import net.minecraft.nbt.CompoundTag;

public final class MechanicalEnergyAnimation {
    private int animationFadeIn;
    private int animationFadeOut;
    private float animationRotation;
    private float animationStep;

    public MechanicalEnergyAnimation() {
        this(0, 0, 0f, 0f);
    }

    public MechanicalEnergyAnimation(int fadeIn, int fadeOut, float rotation, float step) {
        this.animationFadeIn = fadeIn;
        this.animationFadeOut = fadeOut;
        this.animationRotation = rotation;
        this.animationStep = step;
    }

    public void update(boolean active) {
        if (active || animationFadeIn > 0 || animationFadeOut > 0) {
            float base = 18.0f;
            float step = active ? base : 0.0f;

            if (animationFadeIn > 0) {
                step -= (animationFadeIn / 80.0f) * base;
                animationFadeIn--;
            }
            if (animationFadeOut > 0) {
                step += (animationFadeOut / 80.0f) * base;
                animationFadeOut--;
            }

            animationStep = step;
            animationRotation = (animationRotation + step) % 360.0f;
        } else {
            animationStep = 0.0f;
        }
    }

    public void triggerActive() {
        this.animationFadeIn = 80;
    }

    public void triggerInactive() {
        this.animationFadeOut = 160;
    }

    public float getRotation(float partialTick) {
        return (animationRotation + animationStep * partialTick) % 360.0f;
    }

    public float getAnimationRotation() { return animationRotation; }
    public float getAnimationStep() { return animationStep; }
    public int getAnimationFadeIn() { return animationFadeIn; }
    public int getAnimationFadeOut() { return animationFadeOut; }

    public void save(CompoundTag tag) {
        tag.putInt("fadeIn", animationFadeIn);
        tag.putInt("fadeOut", animationFadeOut);
        tag.putFloat("rotation", animationRotation);
        tag.putFloat("step", animationStep);
    }

    public void load(CompoundTag tag) {
        animationFadeIn = tag.getInt("fadeIn");
        animationFadeOut = tag.getInt("fadeOut");
        animationRotation = tag.getFloat("rotation");
        animationStep = tag.getFloat("step");
    }
}
