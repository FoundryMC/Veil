package foundry.veil.quasar.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.Veil;
import foundry.veil.quasar.data.ParticleSettings;
import foundry.veil.quasar.data.QuasarParticleData;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.modules.particle.CollisionParticleModule;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;
import java.util.List;

@Deprecated
public class QuasarVanillaParticle extends Particle {

    public static final Vec3[] PLANE = {
            // plane from -1 to 1 on Y axis and -1 to 1 on X axis
            new Vec3(-1, 1, 0), new Vec3(1, 1, 0), new Vec3(1, -1, 0), new Vec3(-1, -1, 0)
    };

    public static final Vec3[] CUBE = {
            // TOP
            new Vec3(1, 1, -1), new Vec3(1, 1, 1), new Vec3(-1, 1, 1), new Vec3(-1, 1, -1),

            // BOTTOM
            new Vec3(-1, -1, -1), new Vec3(-1, -1, 1), new Vec3(1, -1, 1), new Vec3(1, -1, -1),

            // FRONT
            new Vec3(-1, -1, 1), new Vec3(-1, 1, 1), new Vec3(1, 1, 1), new Vec3(1, -1, 1),

            // BACK
            new Vec3(1, -1, -1), new Vec3(1, 1, -1), new Vec3(-1, 1, -1), new Vec3(-1, -1, -1),

            // LEFT
            new Vec3(-1, -1, -1), new Vec3(-1, 1, -1), new Vec3(-1, 1, 1), new Vec3(-1, -1, 1),

            // RIGHT
            new Vec3(1, -1, 1), new Vec3(1, 1, 1), new Vec3(1, 1, -1), new Vec3(1, -1, -1)};
    private static final ResourceLocation BLANK = Veil.veilPath("textures/special/blank.png");

    public boolean shouldCollide = false;
    protected boolean emissive = true;
    private boolean stoppedByCollision;

    private final QuasarParticle particle;

    public QuasarVanillaParticle(QuasarParticleData data, ParticleSettings particleSettings, ParticleEmitter parentEmitter, ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;

        this.particle = new QuasarParticle(world, data, particleSettings, parentEmitter);
        this.particle.getPosition().set(x, y, z);
        this.particle.init();
        // FIXME
//        this.renderModules = new ArrayList<>();//data.renderModules;
//        this.initModules = new ArrayList<>();//data.initModules;
//        this.updateModules = new ArrayList<>();//data.updateModules;
//        this.collisionModules = new ArrayList<>();//data.collisionModules;
//        this.forces = new ArrayList<>();//data.forces;
//        this.subEmitters = new ArrayList<>();//data.subEmitters;
//        this.trailModules = new ArrayList<>();//data.initModules.stream().filter(m -> m instanceof TrailParticleModule).map(m -> (TrailParticleModule) m).collect(Collectors.toList());
        //    List<ResourceLocation> subEmitters = new ArrayList<>();
        //    List<AbstractParticleForce> forces = new ArrayList<>();
        //    List<InitParticleModule> initModules = new ArrayList<>();
        //    List<RenderParticleModule> renderModules = new ArrayList<>();
        //    List<UpdateParticleModule> updateModules = new ArrayList<>();
        //    List<CollisionParticleModule> collisionModules = new ArrayList<>();

        float scale = this.particle.getScale();
        float width = this.bbWidth / 2.0F;
        float height = this.bbHeight;
        this.setBoundingBox(new AABB(x - (double) width, y, z - (double) width, x + (double) width, y + (double) height, z + (double) width));
        this.setSize(scale * 0.5f, scale * 0.5f);
    }

    @Override
    public void tick() {
        this.particle.tick();
//        this.hasPhysics = true;
        Vector3d velocity = this.particle.getVelocity();
        Vector3d position = this.particle.getPosition();
        Vector3f rotation = this.particle.getRotation();
        if ((this.stoppedByCollision || this.onGround)) {
            for (CollisionParticleModule collisionParticle : this.particle.getModules().getCollisionModules()) {
                collisionParticle.collide(this.particle);
            }
        }
        if (!this.shouldCollide && this.particle.getModules().getCollisionModules().length > 0) {
            this.shouldCollide = true;
        }
        // friction module
//        this.xd *= this.speed;
//        this.yd *= this.speed;
//        this.zd *= this.speed;

        QuasarParticleData data = this.particle.getData();
        if (data.faceVelocity()) {
            Vector3d normalizedMotion = velocity.normalize(new Vector3d());
            rotation.x = (float) Math.atan2(normalizedMotion.y, Math.sqrt(normalizedMotion.x * normalizedMotion.x + normalizedMotion.z * normalizedMotion.z));
            rotation.y = (float) Math.atan2(normalizedMotion.x, normalizedMotion.z);
            if (data.renderStyle() == RenderStyle.BILLBOARD) {
                rotation.y += (float) (Math.PI / 2.0);
            }
        }

        // vanilla particle
//        this.yd -= 0.04 * (double) this.gravity;
        this.move(velocity.x, velocity.y, velocity.z);
//        if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
//            this.xd *= 1.1;
//            this.zd *= 1.1;
//        }

//        this.xd *= this.friction;
//        this.yd *= this.friction;
//        this.zd *= this.friction;
//        if (this.onGround) {
//            this.xd *= 0.699999988079071;
//            this.zd *= 0.699999988079071;
//        }

        if (this.shouldCollide) {
            List<Entity> entities = this.level.getEntities(null, this.getBoundingBox().inflate(this.particle.getScale() * 2f));
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                    this.stoppedByCollision = true;
                }
            }
        }

        if (this.particle.isRemoved()) {
            this.remove();
        }
    }

    @Override
    public void remove() {
        this.particle.onRemove();
        super.remove();
    }

    private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square(100.0D);

    @Override
    public void move(double pX, double pY, double pZ) {
        if (!this.stoppedByCollision) {
            double d0 = pX;
            double d1 = pY;
            double d2 = pZ;
            if (this.shouldCollide && this.hasPhysics && (pX != 0.0D || pY != 0.0D || pZ != 0.0D) && pX * pX + pY * pY + pZ * pZ < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
                Vec3 vec3 = Entity.collideBoundingBox(null, new Vec3(pX, pY, pZ), this.getBoundingBox(), this.level, List.of());
                pX = vec3.x;
                pY = vec3.y;
                pZ = vec3.z;
            }

            if (pX != 0.0D || pY != 0.0D || pZ != 0.0D) {
                this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
                this.setLocationFromBoundingbox();
            }

            if (Math.abs(d1) >= (double) 1.0E-5F && Math.abs(pY) < (double) 1.0E-5F) {
                this.stoppedByCollision = this.shouldCollide;
            }

            this.onGround = d1 != pY && d1 < 0.0D;
            if (d0 != pX) {
                this.particle.getVelocity().x = 0;
                this.stoppedByCollision = true;
            }

            if (d2 != pZ) {
                this.particle.getVelocity().z = 0;
                this.stoppedByCollision = true;
            }
        }
    }

    @Override
    public void setPos(double x, double y, double z) {
        if (this.particle == null) { // Called in constructor
            return;
        }

        this.particle.getPosition().set(x, y, z);
        float width = this.bbWidth / 2.0F;
        float height = this.bbHeight;
        this.setBoundingBox(new AABB(x - (double) width, y, z - (double) width, x + (double) width, y + (double) height, z + (double) width));
    }

    @Override
    protected void setLocationFromBoundingbox() {
        AABB box = this.getBoundingBox();
        this.particle.getPosition().set((box.minX + box.maxX) / 2.0, box.minY, (box.minZ + box.maxZ) / 2.0);
    }

    @Override
    protected int getLightColor(float $$0) {
        BlockPos pos = this.particle.getBlockPosition();
        return this.level.hasChunkAt(pos) ? LevelRenderer.getLightColor(this.level, pos) : 0;
    }

    @Override
    public void render(VertexConsumer builder, Camera camera, float partialTicks) {
        RenderData renderData = this.particle.getRenderData();
        ParticleRenderType renderType = renderData.getRenderType();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        renderType.begin(bufferBuilder, Minecraft.getInstance().getTextureManager());
        builder = bufferBuilder;

        this.particle.render(partialTicks);
        Vec3 projectedView = camera.getPosition();
//        double ageMultiplier = 1; //1 - Math.pow(Mth.clamp(age + partialTicks, 0, lifetime), 3) / Math.pow(lifetime, 3);
//        float lX = (float) (Mth.lerp(partialTicks, this.xo, this.x));
//        float lY = (float) (Mth.lerp(partialTicks, this.yo, this.y));
//        float lZ = (float) (Mth.lerp(partialTicks, this.zo, this.z));
//        float lerpedYaw = Mth.lerp(partialTicks, this.oYaw, this.yaw);
//        float lerpedPitch = Mth.lerp(partialTicks, this.oPitch, this.pitch);
//        float lerpedRoll = Mth.lerp(partialTicks, this.oRoll, this.roll);
//        if (!this.renderData.getTrails().isEmpty()) {
//            if (this.trails.isEmpty()) {
//                this.renderData.getTrails().forEach(trail -> {
//                    Trail tr = new Trail(MathUtil.colorFromVec4f(trail.getTrailColor()), (ageScale) -> trail.getTrailWidthModifier().modify(ageScale, ageMultiplier));
//                    tr.setBillboard(trail.getBillboard());
//                    tr.setLength(trail.getTrailLength());
//                    tr.setFrequency(trail.getTrailFrequency());
//                    tr.setTilingMode(trail.getTilingMode());
//                    tr.setTexture(trail.getTrailTexture());
//                    tr.setParentRotation(trail.getParentRotation());
//                    tr.pushRotatedPoint(new Vec3(this.xo, this.yo, this.zo), new Vec3(lerpedYaw, lerpedPitch, lerpedRoll));
//                    this.trails.add(tr);
//                });
//            }
//            this.trails.forEach(trail -> {
//                trail.pushRotatedPoint(new Vec3(lX, lY, lZ), new Vec3(lerpedYaw, lerpedPitch, lerpedRoll));
//                PoseStack ps = new PoseStack();
//                ps.pushPose();
//                ps.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
//                trail.render(ps, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypeRegistry.translucentNoCull(trail.getTexture())), this.emissive ? LightTexture.FULL_BRIGHT : this.getLightColor(partialTicks));
//                ps.popPose();
//            });
//        }
        int packedLight = this.emissive ? LightTexture.FULL_BRIGHT : this.getLightColor(partialTicks);
        renderData.renderTrails(projectedView, packedLight);

        Vector3dc renderPosition = renderData.getRenderPosition();
        Vector3fc renderOffset = new Vector3f(
                (float) (renderPosition.x() - projectedView.x()),
                (float) (renderPosition.y() - projectedView.y()),
                (float) (renderPosition.z() - projectedView.z()));
        Vector3dc motionDirection = this.particle.getVelocity().normalize(new Vector3d());
        this.particle.getData().renderStyle().render(this, renderData, renderOffset, motionDirection, packedLight, builder, 1, partialTicks);

        renderType.end(tesselator);
    }

    // FIXME PLEASE renderer
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public Level getLevel() {
        return this.level;
    }

    public enum RenderStyle implements RenderFunction {
        CUBE {
            @Override
            public void render(QuasarVanillaParticle particle, RenderData renderData, Vector3fc renderOffset, Vector3dc motionDirection, int light, VertexConsumer builder, double ageModifier, float partialTicks) {
                Vector3fc rotation = renderData.getRenderRotation();
                for (int i = 0; i < 6; i++) {
                    Vec3[] faceVerts = new Vec3[]{
                            QuasarVanillaParticle.CUBE[i * 4],
                            QuasarVanillaParticle.CUBE[i * 4 + 1],
                            QuasarVanillaParticle.CUBE[i * 4 + 2],
                            QuasarVanillaParticle.CUBE[i * 4 + 3]
                    };
                    SpriteData spriteData = renderData.getSpriteData();
                    TextureAtlasSprite sprite = renderData.getAtlasSprite();
                    if (sprite != null) {
                        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
                        builder = sprite.wrap(builder); // This makes chaining not work properly
                    } else if (spriteData != null) {
                        RenderSystem.setShaderTexture(0, spriteData.sprite());
                    } else {
                        RenderSystem.setShaderTexture(0, BLANK);
                    }
                    for (int j = 0; j < 4; j++) {
                        Vector3f vec = faceVerts[j].toVector3f().mul(-1);
                        QuasarParticleData data = particle.particle.getData();
                        if (vec.z < 0 && data.velocityStretchFactor() != 0.0f) {
                            vec.z *= 1 + data.velocityStretchFactor();
                        }
                        vec.rotateX(rotation.x())
                                .rotateY(rotation.y())
                                .rotateZ(rotation.z())
                                .mul((float) (renderData.getRenderScale() * ageModifier))
                                .add(renderOffset);

                        builder.vertex(vec.x, vec.y, vec.z);
                        builder.uv((float) j / 2, j % 2);
                        builder.color(renderData.getRed(), renderData.getGreen(), renderData.getBlue(), renderData.getAlpha());
                        builder.uv2(light);
                        builder.endVertex();
                    }
                }
            }
        },
        // TODO: FIX UVS THEY'RE FUCKED
        BILLBOARD {
            @Override
            public void render(QuasarVanillaParticle particle, RenderData renderData, Vector3fc renderOffset, Vector3dc motionDirection, int light, VertexConsumer builder, double ageModifier, float partialTicks) {
                Vector3fc rotation = renderData.getRenderRotation();
                Vec3[] faceVerts = new Vec3[]{
                        QuasarVanillaParticle.PLANE[0],
                        QuasarVanillaParticle.PLANE[1],
                        QuasarVanillaParticle.PLANE[2],
                        QuasarVanillaParticle.PLANE[3]
                };

                Quaternionf faceCameraRotation = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
                SpriteData spriteData = renderData.getSpriteData();
                TextureAtlasSprite sprite = renderData.getAtlasSprite();
                if (sprite != null) {
                    RenderSystem.setShaderTexture(0, sprite.atlasLocation());
                    builder = sprite.wrap(builder); // This makes chaining not work properly
                } else if (spriteData != null) {
                    RenderSystem.setShaderTexture(0, spriteData.sprite());
                } else {
                    RenderSystem.setShaderTexture(0, BLANK);
                }

                // turn quat into pitch and yaw
                for (int j = 0; j < 4; j++) {
                    Vector3f vec = faceVerts[j].toVector3f().mul(-1.0F);
                    if (particle.particle.getData().velocityStretchFactor() > 0f) {
                        vec.set(vec.x * (1 + particle.particle.getData().velocityStretchFactor()), vec.y, vec.z);
                    }
                    if (particle.particle.getData().faceVelocity()) {
                        vec.rotateX(rotation.x())
                                .rotateY(rotation.y())
                                .rotateZ(rotation.z());
                    }
//                vec = vec.xRot(lerpedPitch).yRot(lerpedYaw).zRot(lerpedRoll);
                    faceCameraRotation.transform(vec).mul((float) (renderData.getRenderScale() * ageModifier)).add(renderOffset);

                    float u, v;
                    if (j == 0) {
                        u = 0;
                        v = 0;
                    } else if (j == 1) {
                        u = 1;
                        v = 0;
                    } else if (j == 2) {
                        u = 1;
                        v = 1;
                    } else {
                        u = 0;
                        v = 1;
                    }
                    if (spriteData != null) {
                        int spritesheetRows = spriteData.frameHeight();
                        int spritesheetColumns = spriteData.frameWidth();
                        int spriteCount = spriteData.frameCount();
                        float animationSpeed = spriteData.frameTime();
                        // get frame index from age + partial ticks, but it should be an integer.
                        int frameIndex = (int) ((particle.age + partialTicks) / animationSpeed);
                        // get the frame index in the spritesheet
                        int frameIndexInSpritesheet = frameIndex % spriteCount;
                        // get the row and column of the frame in the spritesheet
                        int frameRow = frameIndexInSpritesheet / spritesheetColumns;
                        int frameColumn = frameIndexInSpritesheet % spritesheetColumns;
                        // get the u and v coordinates of the frame, using u and v which are this vertex's u and v
                        u *= (1f / spritesheetColumns) + frameColumn * (1f / spritesheetColumns);
                        v *= (1f / spritesheetRows) + frameRow * (1f / spritesheetRows);
                    }
//                    if (particle.sprite != null) {
//                        u1 = u;
//                        v1 = v;
//                    }
                    builder.vertex(vec.x, vec.y, vec.z);
                    builder.uv(u, v);
                    builder.color(renderData.getRed(), renderData.getGreen(), renderData.getBlue(), renderData.getAlpha());
                    builder.uv2(light);
                    builder.endVertex();
                }
            }
        };
    }

    @FunctionalInterface
    interface RenderFunction {

        void render(QuasarVanillaParticle particle, RenderData renderData, Vector3fc renderOffset, Vector3dc motionDirection, int light, VertexConsumer builder, double ageModifier, float partialTicks);
    }
}