package foundry.veil.quasar.emitters.modules.emitter.settings;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.data.DynamicParticleDataRegistry;
import foundry.veil.quasar.emitters.modules.emitter.settings.shapes.*;
import foundry.veil.quasar.util.CodecUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

import java.util.Map;

public class EmitterShapeSettings {

    // FIXME
    public static final BiMap<String, EmitterShape> SHAPES = HashBiMap.create(Map.of(
            "point", new Point(),
            "hemisphere", new Hemisphere(),
            "cylinder", new Cylinder(),
            "sphere", new Sphere(),
            "cube", new Cube(),
            "torus", new Torus(),
            "disc", new Disc(),
            "plane", new Plane()
    ));
    public static final Codec<EmitterShapeSettings> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("shape").xmap(
                    s -> SHAPES.getOrDefault(s.toLowerCase(), new Point()),
                    s -> SHAPES.inverse().get(s)).forGetter(EmitterShapeSettings::getShape),
            CodecUtil.VECTOR3F_CODEC.fieldOf("dimensions").forGetter(EmitterShapeSettings::getDimensions),
            CodecUtil.VECTOR3F_CODEC.fieldOf("rotation").forGetter(EmitterShapeSettings::getRotation),
            Codec.BOOL.fieldOf("from_surface").forGetter(EmitterShapeSettings::isFromSurface)
    ).apply(instance, EmitterShapeSettings::new));
    public static final Codec<Holder<EmitterShapeSettings>> CODEC = RegistryFileCodec.create(DynamicParticleDataRegistry.EMITTER_SHAPE_SETTINGS, DIRECT_CODEC);

    private final EmitterShape shape;
    private final Vector3fc dimensions;
    private final Vector3fc rotation;
    private final boolean fromSurface;

    private EmitterShapeSettings(EmitterShape shape, Vector3fc dimensions, Vector3fc rotation, boolean fromSurface) {
        this.shape = shape;
        this.dimensions = dimensions;
        this.rotation = rotation;
        this.fromSurface = fromSurface;
    }

    public @Nullable ResourceLocation getRegistryId() {
        return EmitterSettingsRegistry.getShapeSettingsId(this);
    }

    public Vector3d getPos(RandomSource randomSource, Vector3dc pos) {
        return this.shape.getPoint(randomSource, this.dimensions, this.rotation, pos, this.fromSurface);
    }

    public EmitterShape getShape() {
        return this.shape;
    }

    public Vector3fc getDimensions() {
        return this.dimensions;
    }

    public Vector3fc getRotation() {
        return this.rotation;
    }

    public boolean isFromSurface() {
        return this.fromSurface;
    }
}
