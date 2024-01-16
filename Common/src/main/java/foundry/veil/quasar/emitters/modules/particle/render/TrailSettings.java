package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.quasar.fx.Trail;
import foundry.veil.quasar.util.CodecUtil;
import foundry.veil.quasar.util.TriFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static foundry.veil.quasar.client.particle.data.SpriteData.BLANK_TEXTURE;

public class TrailSettings {
    public static final Codec<TrailSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("trailFrequency").forGetter(settings -> settings.trailFrequency),
                    Codec.INT.fieldOf("trailLength").forGetter(settings -> settings.trailLength),
                    CodecUtil.VECTOR4F_CODEC.fieldOf("trailColor").xmap(
                            s -> s == null ? new Vector4f(0.0f, 0.0f, 0.0f, 1.0f) : s,
                            s -> s == null ? new Vector4f(0.0f, 0.0f, 0.0f, 1.0f) : s).forGetter(settings -> settings.trailColor),
                    Codec.FLOAT.fieldOf("trailWidthModifier").forGetter(settings -> 1f),
                    ResourceLocation.CODEC.fieldOf("trailTexture").forGetter(settings -> settings.trailTexture),
                    Codec.FLOAT.fieldOf("trailPointModifier").forGetter(settings -> 1f),
                    Codec.STRING.fieldOf("tilingMode").orElse("STRETCH").xmap(
                            Trail.TilingMode::valueOf,
                            Enum::name
                    ).forGetter(settings -> settings.tilingMode),
                    Codec.BOOL.fieldOf("billboard").orElse(true).forGetter(settings -> settings.billboard),
                    Codec.BOOL.fieldOf("parentRotation").orElse(false).forGetter(settings -> settings.parentRotation)
            ).apply(instance, TrailSettings::new)
    );
    protected int trailFrequency = 1;
    protected int trailLength = 20;
    protected Vector4f trailColor = new Vector4f(1, 1, 1, 1);
    protected BiFunction<Float, Float, Float> trailWidthModifier = (width, ageScale) -> 1f;
    protected TriFunction<Vector4f, Integer, Vec3, Vector4f> trailPointModifier = (point, index, velocity) -> point;
    protected ResourceLocation trailTexture = BLANK_TEXTURE;
    protected Trail.TilingMode tilingMode = Trail.TilingMode.STRETCH;
    protected boolean billboard = true;
    protected boolean parentRotation = false;
    protected float trailWidthModifierFloat = 1f;

    public TrailSettings(int trailFrequency, int trailLength, Vector4f trailColor, BiFunction<Float, Float, Float> trailWidthModifier, ResourceLocation trailTexture, TriFunction<Vector4f, Integer, Vec3, Vector4f> trailPointModifier) {
        this.trailFrequency = trailFrequency;
        this.trailLength = trailLength;
        this.trailColor = trailColor;
        this.trailWidthModifier = trailWidthModifier;
        this.trailTexture = trailTexture;
        this.trailPointModifier = trailPointModifier;
    }

    private TrailSettings(int trailFrequency, int trailLength, Vector4f trailColor, float trailWidthModifier, ResourceLocation trailTexture, float trailPointModifier, Trail.TilingMode tilingMode, boolean billboard, boolean parentRotation) {
        this.trailFrequency = trailFrequency;
        this.trailLength = trailLength;
        this.trailColor = trailColor;
        this.trailWidthModifier = (width, ageScale) -> ((float)Math.sin(width * 3.15)/2f) * trailWidthModifier * trailWidthModifierFloat;
        this.trailTexture = trailTexture;
        this.trailPointModifier = (point, index, velocity) -> point;
        this.tilingMode = tilingMode;
        this.billboard = billboard;
        this.parentRotation = parentRotation;
    }

    public void setParentRotation(boolean parentRotation) {
        this.parentRotation = parentRotation;
    }

    public boolean getParentRotation() {
        return parentRotation;
    }

    public void setBillboard(boolean billboard) {
        this.billboard = billboard;
    }

    public boolean getBillboard() {
        return billboard;
    }

    public void setTilingMode(Trail.TilingMode tilingMode) {
        this.tilingMode = tilingMode;
    }

    public Trail.TilingMode getTilingMode() {
        return tilingMode;
    }

    public void setTrailPointModifier(TriFunction<Vector4f, Integer, Vec3, Vector4f> trailPointModifier) {
        this.trailPointModifier = trailPointModifier;
    }

    public TriFunction<Vector4f, Integer, Vec3, Vector4f> getTrailPointModifier() {
        return trailPointModifier;
    }

    public void setTrailFrequency(int trailFrequency) {
        this.trailFrequency = trailFrequency;
    }

    public void setTrailLength(int trailLength) {
        this.trailLength = trailLength;
    }

    public void setTrailColor(Vector4f trailColor) {
        this.trailColor = trailColor;
    }

    public void setTrailWidthModifier(BiFunction<Float, Float, Float> trailWidthModifier) {
        this.trailWidthModifier = trailWidthModifier;
    }

    public void setTrailTexture(ResourceLocation trailTexture) {
        this.trailTexture = trailTexture;
    }

    public int getTrailFrequency() {
        return trailFrequency;
    }

    public int getTrailLength() {
        return trailLength;
    }

    public Vector4f getTrailColor() {
        return trailColor;
    }

    public BiFunction<Float, Float, Float> getTrailWidthModifier() {
        return trailWidthModifier;
    }

    public ResourceLocation getTrailTexture() {
        return trailTexture;
    }

    public void renderImGuiSettings() {
        ImString trailTextureString = new ImString(trailTexture.toString());
        ImGui.inputText("Trail Texture" + this.hashCode(), trailTextureString);
        trailTexture = new ResourceLocation(trailTextureString.get());
        ImInt trailFrequencyInt = new ImInt(trailFrequency);
        ImGui.inputInt("Trail Frequency" + this.hashCode(), trailFrequencyInt);
        trailFrequency = trailFrequencyInt.get();
        ImInt trailLengthInt = new ImInt(trailLength);
        ImGui.inputInt("Trail Length" + this.hashCode(), trailLengthInt);
        trailLength = trailLengthInt.get();
        float[] trailColorVector4f = new float[]{trailColor.x(), trailColor.y(), trailColor.z(), trailColor.w()};
        ImGui.colorEdit4("Trail Color" + this.hashCode(), trailColorVector4f, ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreview);
        trailColor = new Vector4f(trailColorVector4f[0], trailColorVector4f[1], trailColorVector4f[2], trailColorVector4f[3]);
        if(ImGui.beginCombo("Tiling Mode" + this.hashCode(), tilingMode.name())){
            ImGui.pushItemWidth(-1);
            List<Trail.TilingMode> tilingModes = Arrays.asList(Trail.TilingMode.values());
            for(Trail.TilingMode tilingMode : tilingModes){
                if(ImGui.selectable(tilingMode.name() + this.hashCode())){
                    this.tilingMode = tilingMode;
                }
            }
            ImGui.popItemWidth();
            ImGui.endCombo();
        }
        ImBoolean billboardBoolean = new ImBoolean(billboard);
        ImGui.checkbox("Billboard" + this.hashCode(), billboardBoolean);
        billboard = billboardBoolean.get();
        ImBoolean parentRotationBoolean = new ImBoolean(parentRotation);
        ImGui.checkbox("Parent Rotation" + this.hashCode(), parentRotationBoolean);
        parentRotation = parentRotationBoolean.get();
        ImFloat trailWidthModifierFloat = new ImFloat(this.trailWidthModifierFloat);
        ImGui.inputFloat("Trail Width Modifier" + this.hashCode(), trailWidthModifierFloat);
        this.trailWidthModifierFloat = trailWidthModifierFloat.get();
    }
}
