package foundry.veil.quasar.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.quasar.client.particle.data.QuasarParticleData;
import foundry.veil.quasar.client.particle.data.SpriteData;
import foundry.veil.quasar.data.ParticleSettings;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.modules.particle.render.RenderData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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

    private final ResourceLocation dataId;
    private RenderStyle renderStyle = RenderStyle.BILLBOARD;
    public TextureAtlasSprite sprite = null;

    public boolean shouldCollide = false;
    protected boolean emissive = true;
    private boolean stoppedByCollision;

    //    protected List<TrailParticleModule> trailModules = new ArrayList<>();
    private final QuasarParticle particle;
    //    List<ResourceLocation> subEmitters = new ArrayList<>();
//    List<AbstractParticleForce> forces = new ArrayList<>();
//    List<InitParticleModule> initModules = new ArrayList<>();
//    List<RenderParticleModule> renderModules = new ArrayList<>();
//    List<UpdateParticleModule> updateModules = new ArrayList<>();
//    List<CollisionParticleModule> collisionModules = new ArrayList<>();
    private final ParticleEmitter parentEmitter;

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
        this.dataId = data.getRegistryId();
        this.renderStyle = data.renderStyle();
        this.parentEmitter = parentEmitter;

        this.setScale(this.particle.getScale());
    }

    public void setScale(float scale) {
        this.particle.setScale(scale);
        this.setSize(scale * 0.5f, scale * 0.5f);
    }

    public ResourceLocation getDataId() {
        return this.dataId;
    }

    public double getXDelta() {
        return this.particle.getVelocity().x();
    }

    public double getYDelta() {
        return this.particle.getVelocity().y();
    }

    public double getZDelta() {
        return this.particle.getVelocity().z();
    }

    public void setXDelta(double x) {
        this.particle.getVelocity().x = x;
    }

    public void setYDelta(double y) {
        this.particle.getVelocity().y = y;
    }

    public void setZDelta(double z) {
        this.particle.getVelocity().z = z;
    }

    public Vector3d getDeltaMovement() {
        return this.particle.getVelocity();
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public boolean stoppedByCollision() {
        return this.stoppedByCollision;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    @Override
    public void tick() {
        this.particle.tick();
//        this.hasPhysics = true;
        Vector3d motion = this.particle.getVelocity();
        Vector3d position = this.particle.getPosition();
        Vector3f rotation = this.particle.getRotation();
        this.x = position.x;
        if ((this.stoppedByCollision || this.onGround)) {
            this.particle.getModules().collide(this.particle);
        }
        if (!this.shouldCollide && this.particle.getModules().getCollisionModules().length > 0) {
            this.shouldCollide = true;
        }
        // friction module
//        this.xd *= this.speed;
//        this.yd *= this.speed;
//        this.zd *= this.speed;

        if (this.particle.getData().faceVelocity()) {
            Vector3d normalizedMotion = motion.normalize(new Vector3d());
            rotation.x = (float) Math.atan2(normalizedMotion.y, Math.sqrt(normalizedMotion.x * normalizedMotion.x + normalizedMotion.z * normalizedMotion.z));
            rotation.y = (float) Math.atan2(normalizedMotion.x, normalizedMotion.z);
            if (this.renderStyle == RenderStyle.BILLBOARD) {
                rotation.y += (float) (Math.PI / 2.0);
            }
        }

        // vanilla particle
        this.yd -= 0.04 * (double) this.gravity;
        this.move(this.xd, this.yd, this.zd);
        if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
            this.xd *= 1.1;
            this.zd *= 1.1;
        }

        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        if (this.onGround) {
            this.xd *= 0.699999988079071;
            this.zd *= 0.699999988079071;
        }

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
                this.xd = 0.0D;
                this.stoppedByCollision = true;
            }

            if (d2 != pZ) {
                this.zd = 0.0D;
                this.stoppedByCollision = true;
            }
        }
    }

    @Override
    public void render(VertexConsumer builder, Camera camera, float partialTicks) {
        RenderData renderData = this.particle.getRenderData();
        ParticleRenderType renderType = renderData.getRenderType();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        renderType.begin(bufferBuilder, Minecraft.getInstance().getTextureManager());
        builder = bufferBuilder;

        renderData.setRed(this.rCol);
        renderData.setGreen(this.gCol);
        renderData.setBlue(this.bCol);
        renderData.setAlpha(this.alpha);

        this.particle.render(partialTicks);
        this.rCol = renderData.getRed();
        this.gCol = renderData.getGreen();
        this.bCol = renderData.getBlue();
        this.alpha = renderData.getAlpha();
        Vector3fc rotation = renderData.getRenderRotation();
        this.roll = rotation.z();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 projectedView = camera.getPosition();
        double ageMultiplier = 1; //1 - Math.pow(Mth.clamp(age + partialTicks, 0, lifetime), 3) / Math.pow(lifetime, 3);
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
        Vector3f renderOffset = new Vector3f(
                (float) (renderPosition.x() - projectedView.x()),
                (float) (renderPosition.y() - projectedView.y()),
                (float) (renderPosition.z() - projectedView.z()));
        Vector3dc motionDirection = new Vector3d(this.xd, this.yd, this.zd).normalize();
        this.renderStyle.render(this, renderData, renderOffset, motionDirection, packedLight, builder, ageMultiplier, partialTicks);

        renderType.end(tesselator);
    }

    // FIXME PLEASE renderer
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    // FIXME temporary

//    public void setPitch(float pitch) {
//        this.xRot = pitch;
//        // this.rotation.x = pitch;
//    }
//
//    public void setYaw(float yaw) {
//        this.yRot = yaw;
//        // this.rotation.y = yaw;
//    }
//
//    public void setRoll(float roll) {
//        this.roll = roll;
//        // this.rotation.z = roll;
//    }
//
//    public void setRotation(float pitch, float yaw, float roll) {
//        this.xRot = pitch;
//        this.yRot = yaw;
//        this.roll = roll;
//        //  this.rotation.set(pitch, yaw, roll);
//    }
//
//    public void setRotation(Vector3fc rotation) {
//        this.xRot = rotation.x();
//        this.yRot = rotation.y();
//        this.roll = rotation.z();
//        // this.rotation.set(rotation);
//    }
//
//    public void setRotation(Vector3dc rotation) {
//        this.xRot = (float) rotation.x();
//        this.yRot = (float) rotation.y();
//        this.roll = (float) rotation.z();
//        // this.rotation.set(rotation);
//    }
//
//    public void vectorToRotation(Vector3dc vector) {
//        this.setRotation((float) Math.asin(vector.y()), (float) Math.atan2(vector.x(), vector.z()), 0);
//        // this.rotation.set((float) Math.asin(vector.y()), (float) Math.atan2(vector.x(), vector.z()), 0);
//    }

    // END

//    public Vec3 getPos() {
//        return new Vec3(this.x, this.y, this.z);
//    }
//
//    public void addForce(Vec3 force) {
//        this.xd += force.x;
//        this.yd += force.y;
//        this.zd += force.z;
//    }
//
//    public void addForce(double x, double y, double z) {
//        this.xd += x;
//        this.yd += y;
//        this.zd += z;
//    }
//
//    public void modifyForce(double modifier) {
//        this.xd *= modifier;
//        this.yd *= modifier;
//        this.zd *= modifier;
//    }
//
//    public void modifyForce(Vec3 modifier) {
//        this.xd *= modifier.x;
//        this.yd *= modifier.y;
//        this.zd *= modifier.z;
//    }

    public int getAge() {
        return this.particle.getAge();
    }

//    public void addRotation(Vec3 rot) {
//        this.xRot += (float) rot.x;
//        this.yRot += (float) rot.y;
//        this.roll += (float) rot.z;
//    }

//    public void overrideRotation(Vec3 rot) {
//        this.yaw = (float) rot.x;
//        this.pitch = (float) rot.y;
//        this.roll = (float) rot.z;
//    }

//    public void setColor(Vector4fc color) {
//        this.rCol = color.x();
//        this.gCol = color.y();
//        this.bCol = color.z();
//        this.alpha = color.w();
//    }

    public Level getLevel() {
        return this.level;
    }

//    public ParticleContext getContext() {
//        return new ParticleContext(this.position, this.previousMotion, this);
//    }

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
                    if (particle.sprite != null) {
                        RenderSystem.setShaderTexture(0, particle.sprite.atlasLocation());
                    } else if (spriteData != null) {
                        RenderSystem.setShaderTexture(0, spriteData.sprite());
                    }
                    for (int j = 0; j < 4; j++) {
                        Vector3f vec = faceVerts[j].toVector3f().mul(-1);
                        if (vec.z < 0 && particle.particle.getData().velocityStretchFactor() != 0.0f) {
                            vec.set(vec.x, vec.y, vec.z * (1 + particle.particle.getData().velocityStretchFactor()));
                        }
                        vec = vec.rotateX(rotation.x())
                                .rotateY(rotation.y())
                                .rotateZ(rotation.z())
                                .mul((float) (renderData.getRenderScale() * ageModifier))
                                .add(renderOffset);

                        builder.vertex(vec.x, vec.y, vec.z)
                                .uv((float) j / 2, j % 2)
                                .color(particle.rCol, particle.gCol, particle.bCol, particle.alpha)
                                .uv2(light)
                                .endVertex();
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
                if (particle.sprite != null) {
                    RenderSystem.setShaderTexture(0, particle.sprite.atlasLocation());
                } else if (spriteData != null) {
                    RenderSystem.setShaderTexture(0, spriteData.sprite());
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
                    TextureAtlasSprite atlasSprite = renderData.getAtlasSprite();
                    if (atlasSprite != null) {
                        atlasSprite.wrap(builder).uv(u, v);
                    } else {
                        builder.uv(u, v);
                    }
                    builder.color(particle.rCol, particle.gCol, particle.bCol, particle.alpha);
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