package foundry.veil.quasar.emitters.modules.particle.update.fields;

import foundry.veil.quasar.util.FastNoiseLite;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import imgui.ImGui;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.function.Function;

/**
 * A 3D vector field implementation. This is used to apply a force to a particle based on its position.
 * <p>
 *     The vector field is defined by a noise function, a strength, and a vector function.
 *     The noise function is used to generate a noise value at a given position.
 *     The strength is used to scale the noise value.
 *     The vector function is used to generate a vector if a custom vector field is desired.
 *     If no vector function is provided, a default one is used that generates a vector based on the noise value.
 */
public class VectorField {
    public static Codec<VectorField> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FastNoiseLite.CODEC.fieldOf("noise").forGetter(VectorField::getNoise),
                    Codec.FLOAT.fieldOf("strength").forGetter(VectorField::getStrength)
            ).apply(instance, VectorField::new)
    );
    public FastNoiseLite noise;
    public FastNoiseLite getNoise() {
        return noise;
    }
    public float strength;
    public float getStrength() {
        return strength;
    }
    public Function<Vec3, Vec3> vectorFunction;
    private VectorField(FastNoiseLite noise, float strength) {
        this(noise, strength, null);
    }

    public VectorField(FastNoiseLite noise, float strength, Function<Vec3, Vec3> vectorFunction) {
        this.noise = noise;
        this.strength = strength;
        this.vectorFunction = Objects.requireNonNullElseGet(vectorFunction, () -> (Vec3 pos) -> {
            float x = (float) pos.x();
            float y = (float) pos.y();
            float z = (float) pos.z();
            float xNoise = noise.GetNoise(x, y, z);
            float yNoise = noise.GetNoise(x + 100, y + 100, z + 100);
            float zNoise = noise.GetNoise(x + 200, y + 200, z + 200);
            return new Vec3(xNoise, yNoise, zNoise).normalize().scale(strength);
        });
    }

    public void renderImGuiSettings() {
        if(ImGui.collapsingHeader("Vector Field")){
            ImGui.text("Vector Field Settings");
            float[] strength = new float[]{this.strength};
            ImGui.text("Strength");
            ImGui.sameLine();
            ImGui.dragFloat("##Strength" + this.hashCode(), strength, 0.1f, 0, 100);
            this.strength = strength[0];
            noise.renderImGuiSettings();
        }
    }

    public Vec3 getVector(Vec3 position) {
        return vectorFunction.apply(position);
    }
}
