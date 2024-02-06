package foundry.veil.quasar.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderType;
import foundry.veil.quasar.emitters.modules.particle.render.TrailSettings;
import foundry.veil.quasar.fx.Trail;
import foundry.veil.quasar.util.MathUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RenderData {

    @Deprecated
    private static final ResourceLocation BLANK = Veil.veilPath("textures/special/blank.png");

    private final Vector3d prevPosition;
    private final Vector3d renderPosition;
    private final Vector3f prevRotation;
    private final Vector3f renderRotation;
    private float prevScale;
    private float renderScale;
    private float red;
    private float green;
    private float blue;
    private float alpha;
    public float renderAge;
    public float agePercent;
    private SpriteData spriteData;
    private TextureAtlasSprite atlasSprite;
    private final List<TrailSettings> trails;
    private final List<Trail> renderTrails;

    public RenderData() {
        this.prevPosition = new Vector3d();
        this.renderPosition = new Vector3d();
        this.prevRotation = new Vector3f();
        this.renderRotation = new Vector3f();
        this.prevScale = 1.0F;
        this.renderScale = 1.0F;
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;
        this.alpha = 1.0F;
        this.renderAge = 0.0F;
        this.spriteData = null;
        this.atlasSprite = null;
        this.trails = new ArrayList<>();
        this.renderTrails = new ArrayList<>();
    }

    @ApiStatus.Internal
    public void tick(Vector3dc position, Vector3fc rotation, float scale) {
        this.prevPosition.set(position);
        this.prevRotation.set(rotation);
        this.prevScale = scale;
    }

    @ApiStatus.Internal
    public void render(Vector3dc position, Vector3fc rotation, float scale, int age, int lifetime, float partialTicks) {
        this.prevPosition.lerp(position, partialTicks, this.renderPosition);
        this.prevRotation.lerp(rotation, partialTicks, this.renderRotation);
        this.renderScale = Mth.lerp(partialTicks, this.prevScale, scale);
        this.renderAge = age + partialTicks;
        this.agePercent = Math.min(this.renderAge / (float) lifetime, 1.0F);
    }

    public Vector3dc getRenderPosition() {
        return this.renderPosition;
    }

    public Vector3fc getRenderRotation() {
        return this.renderRotation;
    }

    public float getRenderScale() {
        return this.renderScale;
    }

    public float getRenderAge() {
        return this.renderAge;
    }

    public float getAgePercent() {
        return this.agePercent;
    }

    public float getRed() {
        return this.red;
    }

    public float getGreen() {
        return this.green;
    }

    public float getBlue() {
        return this.blue;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public @Nullable SpriteData getSpriteData() {
        return this.spriteData;
    }

    public TextureAtlasSprite getAtlasSprite() {
        return this.atlasSprite;
    }

    public ResourceLocation getTexture() {
        if (this.atlasSprite != null) {
            return this.atlasSprite.atlasLocation();
        } else if (this.spriteData != null) {
            return this.spriteData.sprite();
        } else {
            return BLANK;
        }
    }

    public List<TrailSettings> getTrails() {
        return this.trails;
    }

    public void renderTrails(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, int packedLight) {
        for (TrailSettings trail : this.trails) {
            Trail tr = new Trail(MathUtil.colorFromVec4f(trail.getTrailColor()), (ageScale) -> trail.getTrailWidthModifier().modify(ageScale, 1));
            tr.setBillboard(trail.getBillboard());
            tr.setLength(trail.getTrailLength());
            tr.setFrequency(trail.getTrailFrequency());
            tr.setTilingMode(trail.getTilingMode());
            tr.setTexture(trail.getTrailTexture());
            tr.setParentRotation(trail.getParentRotation());
            // TODO change to joml vectors
            tr.pushRotatedPoint(new Vec3(this.prevPosition.x, this.prevPosition.y, this.prevPosition.z), new Vec3(this.prevRotation.x, this.prevRotation.y, this.prevRotation.z));
            this.renderTrails.add(tr);
        }
        this.trails.clear();
        // TODO move to renderer
        this.renderTrails.forEach(trail -> {
            trail.pushRotatedPoint(new Vec3(this.renderPosition.x, this.renderPosition.y, this.renderPosition.z), new Vec3(this.renderRotation.x, this.renderRotation.y, this.renderRotation.z));
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
            trail.render(poseStack, bufferSource.getBuffer(VeilRenderType.quasarTranslucent(trail.getTexture())), packedLight);
            poseStack.popPose();
        });
    }

    public List<Trail> getRenderTrails() {
        return this.renderTrails;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public void setColor(Vector4fc color) {
        this.red = color.x();
        this.green = color.y();
        this.blue = color.z();
        this.alpha = color.w();
    }

    public void addTrails(TrailSettings... trails) {
        this.trails.addAll(List.of(trails));
    }

    public void addTrails(Collection<TrailSettings> trails) {
        this.trails.addAll(trails);
    }

    public void setSpriteData(@Nullable SpriteData spriteData) {
        this.spriteData = spriteData;
    }

    public void setAtlasSprite(@Nullable TextureAtlasSprite atlasSprite) {
        this.atlasSprite = atlasSprite;
    }
}
