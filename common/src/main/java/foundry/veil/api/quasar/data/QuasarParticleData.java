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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

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
    private final List<Holder<ParticleModuleData>> modulesView;
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
        this.modulesView = Collections.unmodifiableList(modules);
        this.spriteData = spriteData;
        this.additive = additive;
        this.renderStyle = renderStyle;
    }

    public @Nullable ResourceLocation getRegistryId() {
        return QuasarParticles.registryAccess().registry(QuasarParticles.PARTICLE_DATA).map(registry -> registry.getKey(this)).orElse(null);
    }

    public boolean shouldCollide() {
        return this.shouldCollide;
    }

    /**
     * @since 4.3.0
     */
    public void setShouldCollide(boolean shouldCollide) {
        this.shouldCollide = shouldCollide;
    }

    public boolean faceVelocity() {
        return this.faceVelocity;
    }

    /**
     * @since 4.3.0
     */
    public void setFaceVelocity(boolean faceVelocity) {
        this.faceVelocity = faceVelocity;
    }

    public float velocityStretchFactor() {
        return this.velocityStretchFactor;
    }

    /**
     * @since 4.3.0
     */
    public void setVelocityStretchFactor(float velocityStretchFactor) {
        this.velocityStretchFactor = velocityStretchFactor;
    }

    /**
     * @return A list containing all modules in the particle
     * @since 1.3.0
     * @deprecated Use {@link #modules()} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    public List<Holder<ParticleModuleData>> getAllModules() {
        return new ArrayList<>(this.modules);
    }

    @UnmodifiableView
    public List<Holder<ParticleModuleData>> modules() {
        return this.modulesView;
    }

    public @Nullable SpriteData spriteData() {
        return this.spriteData;
    }

    /**
     * @since 4.3.0
     */
    public void setSpriteData(@Nullable SpriteData data) {
        this.spriteData = data;
    }

    public boolean additive() {
        return this.additive;
    }

    /**
     * @since 4.3.0
     */
    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public RenderStyle renderStyle() {
        return this.renderStyle;
    }

    /**
     * @since 4.3.0
     */
    public void setRenderStyle(RenderStyle renderStyle) {
        this.renderStyle = renderStyle;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
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
        int result = Boolean.hashCode(this.shouldCollide);
        result = 31 * result + Boolean.hashCode(this.faceVelocity);
        result = 31 * result + Float.hashCode(this.velocityStretchFactor);
        result = 31 * result + this.modules.hashCode();
        result = 31 * result + this.modulesView.hashCode();
        result = 31 * result + Objects.hashCode(this.spriteData);
        result = 31 * result + Boolean.hashCode(this.additive);
        result = 31 * result + this.renderStyle.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "QuasarParticleData[" +
                "shouldCollide=" + this.shouldCollide + ", " +
                "faceVelocity=" + this.faceVelocity + ", " +
                "velocityStretchFactor=" + this.velocityStretchFactor + ", " +
                "modules=" + this.modules + ", " +
                "spriteData=" + this.spriteData + ", " +
                "additive=" + this.additive + ", " +
                "renderStyle=" + this.renderStyle + ']';
    }

}