package foundry.veil.api.quasar.emitters.module.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.quasar.fx.Trail;
import foundry.veil.api.util.CodecUtil;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.Optional;

public class TrailSettings implements EditorAttributeProvider {

    public static final Codec<TrailSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("trailFrequency", 1).forGetter(settings -> settings.trailFrequency),
            Codec.INT.optionalFieldOf("trailLength", 20).forGetter(settings -> settings.trailLength),
            CodecUtil.VECTOR4FC_CODEC.optionalFieldOf("trailColor", new Vector4f(1.0F)).forGetter(settings -> settings.trailColor),
            Codec.FLOAT.fieldOf("trailWidthModifier").forGetter(settings -> settings.trailWidthModifierFloat),
            ResourceLocation.CODEC.optionalFieldOf("trailTexture").forGetter(settings -> Optional.ofNullable(settings.trailTexture)),
            Codec.FLOAT.fieldOf("trailPointModifier").forGetter(settings -> 1.0F),
            Trail.TilingMode.CODEC.optionalFieldOf("tilingMode", Trail.TilingMode.STRETCH).forGetter(settings -> settings.tilingMode),
            Codec.BOOL.optionalFieldOf("billboard", true).forGetter(settings -> settings.billboard),
            Codec.BOOL.optionalFieldOf("additive", false).forGetter(settings -> settings.additive),
            Codec.BOOL.optionalFieldOf("parentRotation", false).forGetter(settings -> settings.parentRotation)
    ).apply(instance, TrailSettings::new));

    private int trailFrequency;
    private int trailLength;
    private Vector4f trailColor;
    private TrailWidthModifier trailWidthModifier;
    private TrailPointModifier trailPointModifier;
    private ResourceLocation trailTexture;
    private Trail.TilingMode tilingMode;
    private boolean billboard;
    private boolean additive;
    private boolean parentRotation;
    private float trailWidthModifierFloat = 1f;

    /**
     * @deprecated Use {@link TrailSettings#TrailSettings(int, int, Vector4fc, float, Optional, float, Trail.TilingMode, boolean, boolean, boolean)} instead
     */
    @Deprecated
    public TrailSettings(int trailFrequency, int trailLength, Vector4fc trailColor, TrailWidthModifier trailWidthModifier, @Nullable ResourceLocation trailTexture, TrailPointModifier trailPointModifier, Trail.TilingMode tilingMode, boolean billboard, boolean parentRotation) {
        this(trailFrequency, trailLength, trailColor, trailWidthModifier, trailTexture, trailPointModifier, tilingMode, billboard, false, parentRotation);
    }

    /**
     * @since 4.3.0
     */
    public TrailSettings(int trailFrequency, int trailLength, Vector4fc trailColor, TrailWidthModifier trailWidthModifier, @Nullable ResourceLocation trailTexture, TrailPointModifier trailPointModifier, Trail.TilingMode tilingMode, boolean billboard, boolean additive, boolean parentRotation) {
        this.trailFrequency = trailFrequency;
        this.trailLength = trailLength;
        this.trailColor = new Vector4f(trailColor);
        this.trailWidthModifier = trailWidthModifier;
        this.trailTexture = trailTexture;
        this.trailPointModifier = trailPointModifier;
        this.tilingMode = tilingMode;
        this.billboard = billboard;
        this.additive = additive;
        this.parentRotation = parentRotation;
    }

    private TrailSettings(int trailFrequency, int trailLength, Vector4fc trailColor, float trailWidthModifier, Optional<ResourceLocation> trailTexture, float trailPointModifier, Trail.TilingMode tilingMode, boolean billboard, boolean additive, boolean parentRotation) {
        this.trailFrequency = trailFrequency;
        this.trailLength = trailLength;
        this.trailColor = new Vector4f(trailColor);
        this.trailWidthModifier = (width, ageScale) -> ((float) Math.sin(width * 3.15) / 2f) * trailWidthModifier * this.trailWidthModifierFloat;
        this.trailTexture = trailTexture.orElse(null);
        this.trailPointModifier = (point, index, velocity) -> point;
        this.tilingMode = tilingMode;
        this.billboard = billboard;
        this.additive = additive;
        this.parentRotation = parentRotation;
    }

    public TrailSettings() {
        this(1, 20, new Vector4f(1), 1, Optional.empty(), 1, Trail.TilingMode.STRETCH, true, false, false);
    }

    public void setParentRotation(boolean parentRotation) {
        this.parentRotation = parentRotation;
    }

    public boolean getParentRotation() {
        return this.parentRotation;
    }

    public boolean isAdditive() {
        return this.additive;
    }

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public void setBillboard(boolean billboard) {
        this.billboard = billboard;
    }

    public boolean getBillboard() {
        return this.billboard;
    }

    public void setTilingMode(Trail.TilingMode tilingMode) {
        this.tilingMode = tilingMode;
    }

    public Trail.TilingMode getTilingMode() {
        return this.tilingMode;
    }

    public void setTrailPointModifier(TrailPointModifier trailPointModifier) {
        this.trailPointModifier = trailPointModifier;
    }

    public TrailPointModifier getTrailPointModifier() {
        return this.trailPointModifier;
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

    public void setTrailWidthModifier(TrailWidthModifier trailWidthModifier) {
        this.trailWidthModifier = trailWidthModifier;
    }

    public void setTrailTexture(@Nullable ResourceLocation trailTexture) {
        this.trailTexture = trailTexture;
    }

    public int getTrailFrequency() {
        return this.trailFrequency;
    }

    public int getTrailLength() {
        return this.trailLength;
    }

    public Vector4f getTrailColor() {
        return this.trailColor;
    }

    public TrailWidthModifier getTrailWidthModifier() {
        return this.trailWidthModifier;
    }

    public @Nullable ResourceLocation getTrailTexture() {
        return this.trailTexture;
    }

    public void renderImGuiAttributes() {
        ImString trailTextureString = new ImString(this.trailTexture == null ? "" : this.trailTexture.toString(), 999);

        if (ImGui.inputTextWithHint("Trail Texture", "namespace:path", trailTextureString)) {
            if (trailTextureString.isEmpty()) {
                this.trailTexture = null;
            }

            try {
                this.trailTexture = ResourceLocation.parse(trailTextureString.get());
            } catch (Exception ignored) {

            }
        }

        ImInt trailFrequencyInt = new ImInt(this.trailFrequency);
        ImGui.inputInt("Trail Frequency", trailFrequencyInt);
        this.trailFrequency = trailFrequencyInt.get();
        ImInt trailLengthInt = new ImInt(this.trailLength);
        ImGui.inputInt("Trail Length", trailLengthInt);
        this.trailLength = trailLengthInt.get();
        float[] trailColorVector4f = new float[]{this.trailColor.x(), this.trailColor.y(), this.trailColor.z(), this.trailColor.w()};
        ImGui.colorEdit4("Trail Color", trailColorVector4f, ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreview);
        this.trailColor = new Vector4f(trailColorVector4f[0], trailColorVector4f[1], trailColorVector4f[2], trailColorVector4f[3]);
        if (ImGui.beginCombo("Tiling Mode", this.tilingMode.name())) {
            ImGui.pushItemWidth(-1);
            Trail.TilingMode[] tilingModes = Trail.TilingMode.values();
            for (Trail.TilingMode tilingMode : tilingModes) {
                if (ImGui.selectable(tilingMode.name())) {
                    this.tilingMode = tilingMode;
                }
            }
            ImGui.popItemWidth();
            ImGui.endCombo();
        }
        ImBoolean billboardBoolean = new ImBoolean(this.billboard);
        ImGui.checkbox("Billboard", billboardBoolean);
        this.billboard = billboardBoolean.get();
        ImBoolean additiveBoolean = new ImBoolean(this.additive);
        ImGui.checkbox("Additive", additiveBoolean);
        this.additive = additiveBoolean.get();
        ImBoolean parentRotationBoolean = new ImBoolean(this.parentRotation);
        ImGui.checkbox("Parent Rotation", parentRotationBoolean);
        this.parentRotation = parentRotationBoolean.get();
        ImFloat trailWidthModifierFloat = new ImFloat(this.trailWidthModifierFloat);
        ImGui.inputFloat("Trail Width Modifier", trailWidthModifierFloat);
        this.trailWidthModifierFloat = trailWidthModifierFloat.get();
    }

    @FunctionalInterface
    public interface TrailPointModifier {
        Vector4f modify(Vector4f point, Integer index, Vec3 velocity);
    }

    @FunctionalInterface
    public interface TrailWidthModifier {
        float modify(float ageScale, double ageMultiplier);
    }
}
