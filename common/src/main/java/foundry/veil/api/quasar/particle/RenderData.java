package foundry.veil.api.quasar.particle;

import foundry.veil.Veil;
import foundry.veil.api.client.color.Colorc;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.quasar.data.QuasarParticleData;
import foundry.veil.api.quasar.fx.Trail;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public final class RenderData {

    @Deprecated
    public static final ResourceLocation BLANK = Veil.veilPath("textures/special/blank.png");

    private final QuasarParticle particle;
    private final Vector3d prevPosition;
    private final Vector3d renderPosition;
    private final Vector3f prevRotation;
    private final Vector3f renderRotation;
    private float prevRadius;
    private float renderRadius;
    private int packedLight;
    private int fixedPackedLight;
    private float red;
    private float green;
    private float blue;
    private float alpha;
    public float renderAge;
    public float agePercent;

    private boolean additive;
    private SpriteData spriteData;
    private TextureAtlasSprite atlasSprite;
    private RenderType renderType;
    private boolean renderTypeDirty;

    private final List<Trail> trails;

    public RenderData(QuasarParticle particle, QuasarParticleData data) {
        this.particle = particle;
        this.prevPosition = new Vector3d();
        this.renderPosition = new Vector3d();
        this.prevRotation = new Vector3f();
        this.renderRotation = new Vector3f();
        this.prevRadius = 1.0F;
        this.renderRadius = 1.0F;
        this.packedLight = LightTexture.FULL_BRIGHT;
        this.fixedPackedLight = -1;
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;
        this.alpha = 1.0F;
        this.renderAge = 0.0F;
        this.agePercent = 0.0F;

        this.additive = data.additive();
        this.spriteData = data.spriteData();
        this.atlasSprite = null;
        this.markDirty();

        this.trails = new ArrayList<>();
    }

    @ApiStatus.Internal
    public void tick(QuasarParticle particle, int packedLight) {
        this.prevPosition.set(particle.getPosition());
        this.prevRotation.set(particle.getRotation());
        this.prevRadius = particle.getRadius();
        this.packedLight = packedLight;
        this.tickTrails();
    }

    @ApiStatus.Internal
    public void render(QuasarParticle particle, float partialTicks) {
        this.prevPosition.lerp(particle.getPosition(), partialTicks, this.renderPosition);
        this.prevRotation.lerp(particle.getRotation(), partialTicks, this.renderRotation);
        this.renderRadius = Mth.lerp(partialTicks, this.prevRadius, particle.getRadius());
        this.renderAge = particle.getAge() + partialTicks;
        this.agePercent = Math.min(this.renderAge / (float) particle.getLifetime(), 1.0F);
    }

    /**
     * Forces the render type to be updated on the next render call.
     *
     * @since 1.3.0
     */
    public void markDirty() {
        this.renderTypeDirty = true;
    }

    public Vector3dc getRenderPosition() {
        return this.renderPosition;
    }

    public Vector3fc getRenderRotation() {
        return this.renderRotation;
    }

    public float getRenderRadius() {
        return this.renderRadius;
    }

    public float getRenderAge() {
        return this.renderAge;
    }

    public float getAgePercent() {
        return this.agePercent;
    }

    public int getPackedLight() {
        return this.packedLight;
    }

    public int getFixedPackedLight() {
        return this.fixedPackedLight;
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

    /**
     * @since 1.3.0
     */
    public boolean isAdditive() {
        return this.additive;
    }

    /**
     * @return The render type to use for this particle
     */
    public RenderType getRenderType() {
        if (this.renderTypeDirty) {
            this.renderType = this.particle.getData().renderStyle().getRenderType(this.particle, this);
        }
        return this.renderType;
    }

    public List<Trail> getTrails() {
        return this.trails;
    }

    private void tickTrails() {
        if (this.trails.isEmpty()) {
            return;
        }

        for (Trail trail : this.trails) {
            trail.pushRotatedPoint(new Vec3(this.particle.getPosition().x, this.particle.getPosition().y, this.particle.getPosition().z), new Vec3(this.particle.getRotation()));
        }
    }

    // TODO move to renderer
    @ApiStatus.Internal
    public void renderTrails(MatrixStack matrixStack, MultiBufferSource bufferSource, Vec3 cameraPos, int packedLight, float partialTicks) {
        if (this.trails.isEmpty()) {
            return;
        }

        matrixStack.matrixPush();
        matrixStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
        for (Trail trail : this.trails) {
            trail.render(matrixStack, bufferSource.getBuffer(VeilRenderType.quasarTrail(trail.getTexture(), trail.isAdditive())), packedLight, partialTicks, new Vec3(this.particle.getPosition().x, this.particle.getPosition().y, this.particle.getPosition().z), new Vec3(this.particle.getRotation()));
        }
        matrixStack.matrixPop();
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

    public void setColor(Colorc color) {
        this.red = color.red();
        this.green = color.green();
        this.blue = color.blue();
        this.alpha = color.alpha();
    }

    public void setFixedPackedLight(int fixedPackedLight) {
        this.fixedPackedLight = fixedPackedLight;
    }

    /**
     * @since 1.3.0
     */
    public void setAdditive(boolean additive) {
        this.additive = additive;
        this.markDirty();
    }

    public void setSpriteData(@Nullable SpriteData spriteData) {
        this.spriteData = spriteData;
        this.markDirty();
    }

    public void setAtlasSprite(@Nullable TextureAtlasSprite atlasSprite) {
        this.atlasSprite = atlasSprite;
        this.markDirty();
    }
}
