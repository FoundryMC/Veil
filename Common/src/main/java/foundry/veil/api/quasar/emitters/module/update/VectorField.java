package foundry.veil.api.quasar.emitters.module.update;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.util.FastNoiseLite;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * A 3D vector field implementation. This is used to apply a force to a particle based on its position.
 * <p>
 * The vector field is defined by a noise function, a strength, and a vector function.
 * The noise function is used to generate a noise value at a given position.
 * The strength is used to scale the noise value.
 * The vector function is used to generate a vector if a custom vector field is desired.
 * If no vector function is provided, a default one is used that generates a vector based on the noise value.
 */
public record VectorField(FastNoiseLite noise, float strength) {

    public static Codec<VectorField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FastNoiseLite.CODEC.fieldOf("noise").forGetter(VectorField::noise),
            Codec.FLOAT.fieldOf("strength").forGetter(VectorField::strength)
    ).apply(instance, VectorField::new));

    public Vector3d getVector(Vector3dc position, Vector3d result) {
        float x = (float) position.x();
        float y = (float) position.y();
        float z = (float) position.z();
        float xNoise = this.noise.GetNoise(x, y, z);
        float yNoise = this.noise.GetNoise(x + 100, y + 100, z + 100);
        float zNoise = this.noise.GetNoise(x + 200, y + 200, z + 200);
        return result.set(xNoise, yNoise, zNoise).normalize(this.strength);
    }

    public Vector3d getVector(Vector3dc position) {
        return this.getVector(position, new Vector3d());
    }
}
