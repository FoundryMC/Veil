package foundry.veil.quasar.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

public record EmitterShapeSettings(EmitterShape shape,
                                   Vector3fc dimensions,
                                   Vector3fc rotation,
                                   boolean fromSurface) {

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
                    s -> SHAPES.inverse().get(s)).forGetter(EmitterShapeSettings::shape),
            CodecUtil.VECTOR3F_CODEC.fieldOf("dimensions").forGetter(EmitterShapeSettings::dimensions),
            CodecUtil.VECTOR3F_CODEC.fieldOf("rotation").forGetter(EmitterShapeSettings::rotation),
            Codec.BOOL.fieldOf("from_surface").forGetter(EmitterShapeSettings::fromSurface)
    ).apply(instance, EmitterShapeSettings::new));
    public static final Codec<Holder<EmitterShapeSettings>> CODEC = RegistryFileCodec.create(QuasarParticles.EMITTER_SHAPE_SETTINGS, DIRECT_CODEC);

    public Vector3d getPos(RandomSource randomSource, Vector3dc pos) {
        return this.shape.getPoint(randomSource, this.dimensions, this.rotation, pos, this.fromSurface);
    }

    public @Nullable ResourceLocation getRegistryId() {
        return QuasarParticles.registryAccess().registryOrThrow(QuasarParticles.EMITTER_SHAPE_SETTINGS).getKey(this);
    }
}
