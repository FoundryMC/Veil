package foundry.veil.quasar.emitters.modules.emitter.settings;

import foundry.veil.quasar.emitters.modules.emitter.settings.shapes.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.function.Supplier;

public class EmissionShapeSettings {
    public static final BiMap<String, AbstractEmitterShape> SHAPES = HashBiMap.create(Map.of(
            "point", new Point(),
            "hemisphere", new Hemisphere(),
            "cylinder", new Cylinder(),
            "sphere", new Sphere(),
            "cube", new Cube(),
            "torus", new Torus(),
            "disc", new Disc(),
            "plane", new Plane()
    ));
    public static final Codec<EmissionShapeSettings> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.fieldOf("shape").xmap(
                        s -> SHAPES.getOrDefault(s.toLowerCase(), new Point()),
                       s -> SHAPES.inverse().get(s)).forGetter(EmissionShapeSettings::getShape),
                Vec3.CODEC.fieldOf("dimensions").forGetter(EmissionShapeSettings::getDimensions),
                Vec3.CODEC.fieldOf("rotation").forGetter(EmissionShapeSettings::getRotation),
                Codec.BOOL.fieldOf("from_surface").forGetter(EmissionShapeSettings::isFromSurface)
        ).apply(instance, EmissionShapeSettings::new);
    });
    public ResourceLocation registryName;
    Supplier<Vec3> dimensions;
    Supplier<Vec3> position;
    Supplier<Vec3> rotation;
    RandomSource randomSource;
    boolean fromSurface;
    AbstractEmitterShape shape;
    private EmissionShapeSettings(AbstractEmitterShape shape, Vec3 dimensions, Vec3 position, Vec3 rotation, RandomSource randomSource, boolean fromSurface) {
        this.dimensions = () -> dimensions;
        this.position = () -> position;
        this.randomSource = randomSource;
        this.fromSurface = fromSurface;
        this.rotation = () -> rotation;
        this.shape = shape;
    }

    private EmissionShapeSettings(AbstractEmitterShape shape, Vec3 dimensions, Vec3 rotation, boolean fromSurface) {
        this.dimensions = () -> dimensions;
        this.fromSurface = fromSurface;
        this.rotation = () -> rotation;
        this.shape = shape;
    }

    public EmissionShapeSettings(AbstractEmitterShape shape, Supplier<Vec3> dimensions, Supplier<Vec3> position, Supplier<Vec3> rotation, RandomSource randomSource, boolean fromSurface) {
        this.dimensions = dimensions;
        this.position = position;
        this.randomSource = randomSource;
        this.fromSurface = fromSurface;
        this.rotation = rotation;
        this.shape = shape;
    }

    public EmissionShapeSettings instance(){
        EmissionShapeSettings instance = new EmissionShapeSettings(shape, dimensions, position, rotation, randomSource, fromSurface);
        instance.registryName = registryName;
        return instance;
    }

    public ResourceLocation getRegistryId() {
        return registryName;
    }

    public Vec3 getPos(){
        return shape.getPoint(this.randomSource, this.dimensions.get(), this.rotation.get(), this.position.get(), this.fromSurface);
    }
    public AbstractEmitterShape getShape(){
        return shape;
    }

    public Vec3 getDimensions(){
        return dimensions.get();
    }

    public Vec3 getRotation(){
        return rotation.get();
    }

    public boolean isFromSurface(){
        return fromSurface;
    }

    public void setRandomSource(RandomSource randomSource) {
        this.randomSource = randomSource;
    }
    public void setPosition(Supplier<Vec3> position) {
        this.position = position;
    }
    public void setPosition(Vec3 position) {
        this.position = () -> position;
    }

    public void setShape(AbstractEmitterShape shape) {
        this.shape = shape;
    }

    public void setDimensions(Vec3 dimensions) {
        this.dimensions = () -> dimensions;
    }

    public void setRotation(Vec3 rotation) {
        this.rotation = () -> rotation;
    }

    public void setFromSurface(boolean surface) {
        this.fromSurface = surface;
    }

    public static class Builder {
        private Supplier<Vec3> dimensions;
        private Supplier<Vec3> position;
        private Supplier<Vec3> rotation;
        private RandomSource randomSource;
        private boolean fromSurface;
        private AbstractEmitterShape shape;

        public Builder setDimensions(Supplier<Vec3> dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        public Builder setPosition(Supplier<Vec3> position) {
            this.position = position;
            return this;
        }

        public Builder setRotation(Supplier<Vec3> rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder setRandomSource(RandomSource randomSource) {
            this.randomSource = randomSource;
            return this;
        }

        public Builder setFromSurface(boolean fromSurface) {
            this.fromSurface = fromSurface;
            return this;
        }

        public Builder setShape(AbstractEmitterShape shape) {
            this.shape = shape;
            return this;
        }

        public Builder setDimensions(Vec3 dimensions) {
            this.dimensions = () -> dimensions;
            return this;
        }

        public Builder setPosition(Vec3 position) {
            this.position = () -> position;
            return this;
        }

        public Builder setRotation(Vec3 rotation) {
            this.rotation = () -> rotation;
            return this;
        }

        public EmissionShapeSettings build() {
            return new EmissionShapeSettings(shape, dimensions, position, rotation, randomSource, fromSurface);
        }
    }
}
