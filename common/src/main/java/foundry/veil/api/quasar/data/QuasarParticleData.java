package foundry.veil.api.quasar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.particle.QuasarParticle;
import foundry.veil.api.quasar.particle.RenderStyle;
import foundry.veil.api.quasar.particle.SpriteData;
import foundry.veil.api.quasar.registry.RenderStyleRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * <p>Data passed to each particle when it is created.</p>
 *
 * <p>This class is used to store all the data that is passed to each particle when it is created.
 * This includes the particle settings, whether or not the particle should collide with blocks,
 * whether or not the particle should face its velocity, and the list of sub emitters.</p>
 *
 * <p>This class also stores the list of particle modules that are applied to each particle.
 * These modules are used to modify the particle's behavior. The following are valid particle modules:</p>
 *
 * <ul>
 *   <li>Init Modules - Applied when a particle is created</li>
 *   <li>Update Modules - Applied at the beginning of the particle tick</li>
 *   <li>Collision Modules - Applied when the particle collides with a block or entity</li>
 *   <li>Force Modules - Applied each physics tick to update velocity</li>
 *   <li>Render Modules - Applied when the particle is rendered</li>
 * </ul>
 *
 * @author amo
 * @see QuasarParticle
 */
public final class QuasarParticleData {

    public static final Codec<QuasarParticleData> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("should_collide", true).forGetter(QuasarParticleData::shouldCollide),
            Codec.BOOL.optionalFieldOf("face_velocity", false).forGetter(QuasarParticleData::faceVelocity),
            Codec.FLOAT.optionalFieldOf("velocity_stretch_factor", 0.0F).forGetter(QuasarParticleData::velocityStretchFactor),
            ParticleModuleData.CODEC.listOf().optionalFieldOf("modules", Collections.emptyList()).forGetter(QuasarParticleData::modules),
            SpriteData.CODEC.optionalFieldOf("sprite_data").forGetter(data -> Optional.ofNullable(data.spriteData())),
            Codec.BOOL.optionalFieldOf("additive", false).forGetter(QuasarParticleData::additive),
            RenderStyle.CODEC.optionalFieldOf("render_style").forGetter(particleData -> Optional.of(particleData.renderStyle()))
    ).apply(instance, (shouldCollide, faceVelocity, velocityStretchFactor, modules, spriteData, additive, renderStyle) -> new QuasarParticleData(shouldCollide, faceVelocity, velocityStretchFactor, modules, spriteData.orElse(null), additive, renderStyle.orElseGet(RenderStyleRegistry.BILLBOARD))));
    public static final Codec<Holder<QuasarParticleData>> CODEC = RegistryFileCodec.create(QuasarParticles.PARTICLE_DATA, DIRECT_CODEC);
    private boolean shouldCollide;
    private boolean faceVelocity;
    private float velocityStretchFactor;
    private final List<Holder<ParticleModuleData>> modules;
    private @Nullable SpriteData spriteData;
    private boolean additive;
    private RenderStyle renderStyle;


    public QuasarParticleData(boolean shouldCollide,
                              boolean faceVelocity,
                              float velocityStretchFactor,
                              List<Holder<ParticleModuleData>> modules,
                              @Nullable SpriteData spriteData,
                              boolean additive,
                              RenderStyle renderStyle) {
        this.shouldCollide = shouldCollide;
        this.faceVelocity = faceVelocity;
        this.velocityStretchFactor = velocityStretchFactor;
        this.modules = new ArrayList<>(modules);
        this.spriteData = spriteData;
        this.additive = additive;
        this.renderStyle = renderStyle;
    }

    public @Nullable ResourceLocation getRegistryId() {
        return QuasarParticles.registryAccess().registry(QuasarParticles.PARTICLE_DATA).map(registry -> registry.getKey(this)).orElse(null);
    }

    public boolean shouldCollide() {
        return shouldCollide;
    }

    public void setShouldCollide(boolean shouldCollide) {
        this.shouldCollide = shouldCollide;
    }

    public boolean faceVelocity() {
        return faceVelocity;
    }

    public void setFaceVelocity(boolean faceVelocity) {
        this.faceVelocity = faceVelocity;
    }

    public float velocityStretchFactor() {
        return velocityStretchFactor;
    }

    public void setVelocityStretchFactor(float velocityStretchFactor) {
        this.velocityStretchFactor = velocityStretchFactor;
    }

    public List<Holder<ParticleModuleData>> modules() {
        return modules;
    }

    public @Nullable SpriteData spriteData() {
        return spriteData;
    }

    public void setSpriteData(@Nullable SpriteData data) {
        this.spriteData = data;
    }

    public boolean additive() {
        return additive;
    }

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public RenderStyle renderStyle() {
        return renderStyle;
    }

    public void setRenderStyle(RenderStyle renderStyle) {
        this.renderStyle = renderStyle;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (QuasarParticleData) obj;
        return this.shouldCollide == that.shouldCollide &&
                this.faceVelocity == that.faceVelocity &&
                Float.floatToIntBits(this.velocityStretchFactor) == Float.floatToIntBits(that.velocityStretchFactor) &&
                Objects.equals(this.modules, that.modules) &&
                Objects.equals(this.spriteData, that.spriteData) &&
                this.additive == that.additive &&
                Objects.equals(this.renderStyle, that.renderStyle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shouldCollide, faceVelocity, velocityStretchFactor, modules, spriteData, additive, renderStyle);
    }

    @Override
    public String toString() {
        return "QuasarParticleData[" +
                "shouldCollide=" + shouldCollide + ", " +
                "faceVelocity=" + faceVelocity + ", " +
                "velocityStretchFactor=" + velocityStretchFactor + ", " +
                "modules=" + modules + ", " +
                "spriteData=" + spriteData + ", " +
                "additive=" + additive + ", " +
                "renderStyle=" + renderStyle + ']';
    }

}