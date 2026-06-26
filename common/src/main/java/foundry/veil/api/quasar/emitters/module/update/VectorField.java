package foundry.veil.api.quasar.emitters.module.update;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.util.FastNoiseLite;
import imgui.ImGui;
import imgui.type.ImInt;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A 3D vector field implementation. This is used to apply a force to a particle based on its position.
 * <p>
 * The vector field is defined by a noise function, a strength, and a vector function.
 * The noise function is used to generate a noise value at a given position.
 * The strength is used to scale the noise value.
 * The vector function is used to generate a vector if a custom vector field is desired.
 * If no vector function is provided, a default one is used that generates a vector based on the noise value.
 */
public final class VectorField implements EditorAttributeProvider {

    public static Codec<VectorField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FastNoiseLite.CODEC.fieldOf("noise").forGetter(VectorField::noise),
            Codec.FLOAT.fieldOf("strength").forGetter(VectorField::strength)
    ).apply(instance, VectorField::new));
    private final FastNoiseLite noise;
    private float strength;

    public VectorField(FastNoiseLite noise, float strength) {
        this.noise = noise;
        this.strength = strength;
    }

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

    @Override
    public void renderImGuiAttributes() {
        float[] editFrequency = new float[]{this.noise.GetFrequency()};
        if (ImGui.dragFloat("frequency", editFrequency, 0.02f, 0)) {
            this.noise.SetFrequency(editFrequency[0]);
        }

        if (ImGui.button("Randomize Seed")) {
            this.noise.SetSeed((int) (Math.random() * Integer.MAX_VALUE));
        }
        ImGui.sameLine();
        ImGui.text(String.valueOf(this.noise.GetSeed()));

        FastNoiseLite.FractalType fractalType = enumCombo("fractal_type", FastNoiseLite.FractalType.values(), this.noise.GetFractalType());
        if (fractalType != null) {
            this.noise.SetFractalType(fractalType);
        }

        int[] editOctaves = new int[]{this.noise.GetFractalOctaves()};
        if (ImGui.dragInt("octaves", editOctaves, 0.03F)) {
            this.noise.SetFractalOctaves(editOctaves[0]);
        }

        float[] editLacunarity = new float[]{this.noise.GetFractalLacunarity()};
        if (ImGui.dragFloat("lacunarity", editLacunarity, 0.01F)) {
            this.noise.SetFractalLacunarity(editLacunarity[0]);
        }

        float[] editGain = new float[]{this.noise.GetFractalGain()};
        if (ImGui.dragFloat("gain", editGain, 0.015F)) {
            this.noise.SetFractalGain(editGain[0]);
        }

        FastNoiseLite.CellularDistanceFunction cellularDistanceFunction = enumCombo("cellular_distance_function", FastNoiseLite.CellularDistanceFunction.values(), this.noise.GetCellularDistanceFunction());
        if (cellularDistanceFunction != null) {
            this.noise.SetCellularDistanceFunction(cellularDistanceFunction);
        }

        FastNoiseLite.CellularReturnType cellularReturnType = enumCombo("cellular_return_type", FastNoiseLite.CellularReturnType.values(), this.noise.GetCellularReturnType());
        if (cellularReturnType != null) {
            this.noise.SetCellularReturnType(cellularReturnType);
        }

        FastNoiseLite.NoiseType noiseType = enumCombo("noise_type", FastNoiseLite.NoiseType.values(), this.noise.GetNoiseType());
        if (noiseType != null) {
            this.noise.SetNoiseType(noiseType);
        }

        FastNoiseLite.RotationType3D rotationType3D = enumCombo("rotation_type_3d", FastNoiseLite.RotationType3D.values(), this.noise.GetRotationType3D());
        if (rotationType3D != null) {
            this.noise.SetRotationType3D(rotationType3D);
        }

        FastNoiseLite.DomainWarpType domainWarpType = enumCombo("domain_warp_type", FastNoiseLite.DomainWarpType.values(), this.noise.GetDomainWarpType());
        if (domainWarpType != null) {
            this.noise.SetDomainWarpType(domainWarpType);
        }

        float[] editDomainWarpAmp = new float[]{this.noise.GetDomainWarpAmp()};
        if (ImGui.dragFloat("domain_warp_amp", editDomainWarpAmp, 0.01F)) {
            this.noise.SetDomainWarpAmp(editDomainWarpAmp[0]);
        }

        float[] noiseStrength = new float[]{this.strength};
        if (ImGui.dragFloat("noise_strength", noiseStrength, 0.01F)) {
            this.strength = noiseStrength[0];
        }
    }

    public FastNoiseLite noise() {
        return this.noise;
    }

    public float strength() {
        return this.strength;
    }

    @Nullable
    private static <T extends Enum<T>> T enumCombo(String label, T[] enumArray, T currentValue) {
        Object[] enumObjectArray = Stream.of(enumArray).map(Enum::name).toArray();
        String[] nameArray = Arrays.copyOf(enumObjectArray, enumObjectArray.length, String[].class);

        ImInt selectedType = new ImInt();
        for (int i = 0; i < nameArray.length; i++) {
            if (currentValue == enumArray[i]) {
                selectedType.set(i);
            }
        }

        if (ImGui.combo(label, selectedType, nameArray)) {
            return enumArray[selectedType.get()];
        }
        return null;
    }
}
