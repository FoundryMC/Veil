package foundry.veil.quasar.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.deferred.light.PointLight;
import foundry.veil.quasar.client.particle.data.QuasarParticleData;
import foundry.veil.quasar.client.particle.data.QuasarParticleRenderData;
import foundry.veil.quasar.client.particle.data.QuasarParticleRenderType;
import foundry.veil.quasar.client.particle.data.SpriteData;
import foundry.veil.quasar.emitters.ParticleContext;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.modules.particle.init.InitParticleModule;
import foundry.veil.quasar.emitters.modules.particle.init.LightModule;
import foundry.veil.quasar.emitters.modules.particle.render.RenderData;
import foundry.veil.quasar.emitters.modules.particle.render.RenderParticleModule;
import foundry.veil.quasar.emitters.modules.particle.render.TrailParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.collsion.CollisionParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.forces.AbstractParticleForce;
import foundry.veil.quasar.fx.Trail;
import foundry.veil.quasar.util.ColorGradient;
import foundry.veil.quasar.util.MathUtil;
import foundry.veil.api.client.render.shader.RenderTypeRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4fc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static foundry.veil.quasar.emitters.modules.particle.init.LightModule.toLightColor;

public class QuasarParticle extends Particle {
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

    private static final ParticleRenderType RENDER_TYPE_EMISSIVE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();

            //
            RenderSystem.setShader(() -> RenderTypeRegistry.QUASAR_PARTICLE_ADDITIVE_MULTIPLY);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            // opaque
//			RenderSystem.depthMask(true);
//			RenderSystem.disableBlend();
//			RenderSystem.enableLighting();

            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tessellator) {
            tessellator.end();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.enableCull();
        }
    };

    private ResourceLocation dataId;
    private RenderStyle renderStyle = RenderStyle.BILLBOARD;
    private static final ParticleRenderType RENDER_TYPE_FLAT = new QuasarParticleRenderType();

    protected float scale;
    public boolean shouldCollide = false;
    protected boolean emissive = true;
    protected Vec3 previousMotion = Vec3.ZERO;
    protected Vec3 previousPosition = Vec3.ZERO;
    protected Vec3 initialPosition = Vec3.ZERO;
    protected Vec3 position = Vec3.ZERO;
    protected double xRot = 0;
    protected double oxRot = 0;
    protected double yRot = 0;
    protected double oyRot = 0;
    protected double zRot = 0;
    protected double ozRot = 0;
    protected boolean faceVelocity = false;
    protected float velocityStretchFactor = 0.0f;
    public PointLight light;
    public ColorGradient lightGradient;

    protected List<TrailParticleModule> trailModules = new ArrayList<>();
    List<ResourceLocation> subEmitters = new ArrayList<>();
    List<AbstractParticleForce> forces = new ArrayList<>();
    List<InitParticleModule> initModules = new ArrayList<>();
    List<RenderParticleModule> renderModules = new ArrayList<>();
    List<UpdateParticleModule> updateModules = new ArrayList<>();
    List<CollisionParticleModule> collisionModules = new ArrayList<>();
    SpriteData spriteData = SpriteData.BLANK;
    ParticleRenderType renderType = RENDER_TYPE_FLAT;
    float speed;
    ParticleEmitter parentEmitter;

    public QuasarParticle(QuasarParticleData data, ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;
        this.initialPosition = new Vec3(x, y, z);
        this.faceVelocity = data.faceVelocity;
        this.velocityStretchFactor = data.velocityStretchFactor;
        this.setScale(0.2F);
        this.previousMotion = new Vec3(motionX, motionY, motionZ);
        this.renderModules = data.renderModules;
        this.initModules = data.initModules;
        this.updateModules = data.updateModules;
        this.collisionModules = data.collisionModules;
        this.forces = data.forces;
        this.subEmitters = data.subEmitters;
        this.trailModules = data.initModules.stream().filter(m -> m instanceof TrailParticleModule).map(m -> (TrailParticleModule) m).collect(Collectors.toList());
        this.scale = data.particleSettings.getParticleSize();
        this.lifetime = data.particleSettings.getParticleLifetime() + 1;
        this.dataId = data.registryId;
        this.renderStyle = data.renderStyle;
        this.spriteData = data.spriteData;
        this.initModules.forEach(m -> m.run(this));
        this.oPitch = this.pitch;
        this.oYaw = this.yaw;
        this.oRoll = this.roll;
        this.renderType = data.renderType;
        this.speed = data.particleSettings.getParticleSpeed();
        this.parentEmitter = data.parentEmitter;
        this.parentEmitter.particleCount++;
    }

    public QuasarParticle() {
        super(null, 0, 0, 0);
    }

    public List<ResourceLocation> getSubEmitters() {
        return this.subEmitters;
    }

    public void setScale(float scale) {
        this.scale = scale;
        this.setSize(scale * 0.5f, scale * 0.5f);
    }

    public ResourceLocation getDataId() {
        return this.dataId;
    }

    public float getScale() {
        return this.scale;
    }

    public double getXDelta() {
        return (float) this.xd;
    }

    public double getYDelta() {
        return (float) this.yd;
    }

    public double getZDelta() {
        return (float) this.zd;
    }

    public void setXDelta(double x) {
        this.xd = x;
    }

    public void setYDelta(double y) {
        this.yd = y;
    }

    public void setZDelta(double z) {
        this.zd = z;
    }

    public void setDeltaMovement(Vec3 delta) {
        this.xd = delta.x;
        this.yd = delta.y;
        this.zd = delta.z;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public Vec3 getDeltaMovement() {
        return new Vec3(this.xd, this.yd, this.zd);
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
//        MinecraftForge.EVENT_BUS.post(new QuasarParticleTickEvent(this));
        this.hasPhysics = true;
        Vec3 motion = new Vec3(this.xd, this.yd, this.zd);
        this.position = new Vec3(this.x, this.y, this.z);
        if ((this.stoppedByCollision || this.onGround)) {
            this.collisionModules.forEach(m -> {
                m.run(this);
            });
        }
        if (!this.shouldCollide && !this.collisionModules.isEmpty()) {
            this.shouldCollide = true;
        }
        this.updateModules.forEach(m -> {
            m.run(this);
        });
        this.forces.forEach(force -> {
            force.applyForce(this);
        });
        this.xd *= this.speed;
        this.yd *= this.speed;
        this.zd *= this.speed;

        if (this.previousPosition.x == this.position.x) {
            this.stoppedByCollision = true;
        }
        if (this.previousPosition.z == this.position.z) {
            this.stoppedByCollision = true;
        }
        this.previousMotion = motion;
        this.previousPosition = this.position;
        this.oYaw = this.yaw;
        this.oPitch = this.pitch;
        this.oRoll = this.roll;
        if (this.faceVelocity) {
            motion = motion.normalize();
            this.pitch = (float) Math.atan2(motion.y, Math.sqrt(motion.x * motion.x + motion.z * motion.z));
            this.yaw = (float) Math.atan2(motion.x, motion.z);
            if (this.renderStyle == RenderStyle.BILLBOARD) {
                this.yaw += Math.PI / 2;
            }
        }
        super.tick();
        List<Entity> entities = this.level.getEntities(null, this.getBoundingBox().inflate(this.scale * 2f));
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && this.shouldCollide) {
                if (livingEntity.isAlive()) {
                    this.stoppedByCollision = true;
                }
            }
        }

        // parent tick
        // end parent tick
        if(this.age == this.lifetime -1){
            this.remove();
        }
    }

    @Override
    public void remove() {
        this.parentEmitter.particleCount--;
        super.remove();
        if(VeilRenderSystem.renderer().getDeferredRenderer().isEnabled()){
            if(this.light != null) {
                VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer().removeLight(this.light);
            }
        }
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
            }

            if (d2 != pZ) {
                this.zd = 0.0D;
            }

        }
    }

    private void updateLight(float partialTick){
        if(this.light == null) return;
        Vec3 lerpedPos = new Vec3(Mth.lerp(partialTick, this.xo, this.x), Mth.lerp(partialTick, this.yo, this.y), Mth.lerp(partialTick, this.zo, this.z));
        this.light.setPosition(lerpedPos.x, lerpedPos.y, lerpedPos.z);
        LightModule lightModule = (LightModule) this.initModules.stream().filter(m -> m instanceof LightModule).findFirst().orElse(null);
        if(lightModule == null) return;
        this.light.setColor(toLightColor(this.lightGradient.getColor((float) age / lifetime)).mul(lightModule.getBrightness()));
    }
    private static final ResourceLocation STONE_TEXTURE = new ResourceLocation("minecraft", "textures/block/dirt.png");

    private RenderData renderData = null;
    private float pitch = 0;
    private float oPitch = 0;
    private float yaw = 0;
    private float oYaw = 0;
    private final List<Trail> trails = new ArrayList<>();

    @Override
    public void render(VertexConsumer builder, Camera camera, float partialTicks) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RENDER_TYPE_FLAT.begin(bufferBuilder, Minecraft.getInstance().getTextureManager());
        builder = bufferBuilder;

        if (this.renderData == null) {
            this.renderData = new RenderData(this.scale, this.pitch, this.yaw, this.roll, this.rCol, this.gCol, this.bCol, this.alpha);
        } else {
            this.renderData.setScale(this.scale);
            this.renderData.setPitch(this.pitch);
            this.renderData.setYaw(this.yaw);
            this.renderData.setRoll(this.roll);
            this.renderData.setR(this.rCol);
            this.renderData.setG(this.gCol);
            this.renderData.setB(this.bCol);
            this.renderData.setA(this.alpha);
            this.renderData.getTrails().clear();
        }
        this.renderModules.forEach(m -> m.apply(this, partialTicks, this.renderData));
        this.rCol = this.renderData.getR();
        this.gCol = this.renderData.getG();
        this.bCol = this.renderData.getB();
        this.alpha = this.renderData.getA();
        this.yaw = this.renderData.getYaw();
        this.pitch = this.renderData.getPitch();
        this.roll = this.renderData.getRoll();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 projectedView = camera.getPosition();
        double ageMultiplier = 1; //1 - Math.pow(Mth.clamp(age + partialTicks, 0, lifetime), 3) / Math.pow(lifetime, 3);
        float lX = (float) (Mth.lerp(partialTicks, this.xo, this.x));
        float lY = (float) (Mth.lerp(partialTicks, this.yo, this.y));
        float lZ = (float) (Mth.lerp(partialTicks, this.zo, this.z));
        float lerpedYaw = Mth.lerp(partialTicks, this.oYaw, this.yaw);
        float lerpedPitch = Mth.lerp(partialTicks, this.oPitch, this.pitch);
        float lerpedRoll = Mth.lerp(partialTicks, this.oRoll, this.roll);
        if (!this.renderData.getTrails().isEmpty()) {
            if(this.trails.isEmpty()) {
                this.renderData.getTrails().forEach(trail -> {
                    Trail tr = new Trail(MathUtil.colorFromVec4f(trail.getTrailColor()), (ageScale) -> trail.getTrailWidthModifier().modify(ageScale, ageMultiplier));
                    tr.setBillboard(trail.getBillboard());
                    tr.setLength(trail.getTrailLength());
                    tr.setFrequency(trail.getTrailFrequency());
                    tr.setTilingMode(trail.getTilingMode());
                    tr.setTexture(trail.getTrailTexture());
                    tr.setParentRotation(trail.getParentRotation());
                    tr.pushRotatedPoint(new Vec3(this.xo, this.yo, this.zo), new Vec3(lerpedYaw, lerpedPitch, lerpedRoll));
                    this.trails.add(tr);
                });
            }
            this.trails.forEach(trail -> {
                trail.pushRotatedPoint(new Vec3(lX, lY, lZ), new Vec3(lerpedYaw, lerpedPitch, lerpedRoll));
                PoseStack ps = new PoseStack();
                ps.pushPose();
                ps.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
                trail.render(ps, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypeRegistry.translucentNoCull(trail.getTexture())), this.emissive ? LightTexture.FULL_BRIGHT : this.getLightColor(partialTicks));
                ps.popPose();
            });
        }
        float lerpedX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - projectedView.x());
        float lerpedY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - projectedView.y());
        float lerpedZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - projectedView.z());

        int light = LightTexture.FULL_BRIGHT;//getLightColor(partialTicks);
        this.updateLight(partialTicks);
        Vec3 motionDirection = new Vec3(this.xd, this.yd, this.zd).normalize();
        this.renderStyle.render(this, new QuasarParticleRenderData(motionDirection, new Vec3(lerpedX, lerpedY, lerpedZ), light, builder, ageMultiplier, partialTicks));

        RENDER_TYPE_FLAT.end(tesselator);
    }

    // FIXME PLEASE renderer
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public Vec3 getPos() {
        return new Vec3(this.x, this.y, this.z);
    }

    public void addForce(Vec3 force) {
        this.xd += force.x;
        this.yd += force.y;
        this.zd += force.z;
    }

    public void addForce(double x, double y, double z) {
        this.xd += x;
        this.yd += y;
        this.zd += z;
    }

    public void modifyForce(double modifier) {
        this.xd *= modifier;
        this.yd *= modifier;
        this.zd *= modifier;
    }

    public void modifyForce(Vec3 modifier) {
        this.xd *= modifier.x;
        this.yd *= modifier.y;
        this.zd *= modifier.z;
    }

    public int getAge() {
        return this.age;
    }

    public void addRotation(Vec3 rot) {
        this.yaw += (float) rot.x;
        this.pitch += (float) rot.y;
        this.roll += (float) rot.z;

    }

    public void overrideRotation(Vec3 rot) {
        this.yaw = (float) rot.x;
        this.pitch = (float) rot.y;
        this.roll = (float) rot.z;
    }

    public void setColor(Vector4fc color) {
        this.rCol = color.x();
        this.gCol = color.y();
        this.bCol = color.z();
        this.alpha = color.w();
    }

    public Level getLevel() {
        return this.level;
    }

    public List<AbstractParticleForce> getForces() {
        return this.forces;
    }

    public ParticleContext getContext() {
        return new ParticleContext(this.position, this.previousMotion, this);
    }

    public static class Factory implements ParticleProvider<QuasarParticleData> {

        @Override
        public Particle createParticle(@NotNull QuasarParticleData data, ClientLevel world, double x, double y, double z, double motionX,
                                       double motionY, double motionZ) {
            QuasarParticle particle = new QuasarParticle(data, world, x, y, z, motionX, motionY, motionZ);
            particle.shouldCollide = data.shouldCollide;
            particle.faceVelocity = data.faceVelocity;
            particle.velocityStretchFactor = data.velocityStretchFactor;
            return particle;
        }
    }

    public enum RenderStyle {
        CUBE((particle, data) -> {
            float lerpedYaw = Mth.lerp(data.partialTicks, particle.oYaw, particle.yaw);
            float lerpedPitch = Mth.lerp(data.partialTicks, particle.oPitch, particle.pitch);
            float lerpedRoll = Mth.lerp(data.partialTicks, particle.oRoll, particle.roll);
            for (int i = 0; i < 6; i++) {
                Vec3[] faceVerts = new Vec3[]{
                        QuasarParticle.CUBE[i * 4],
                        QuasarParticle.CUBE[i * 4 + 1],
                        QuasarParticle.CUBE[i * 4 + 2],
                        QuasarParticle.CUBE[i * 4 + 3]
                };
                RenderSystem.setShaderTexture(0, particle.spriteData.sprite);
                for (int j = 0; j < 4; j++) {
                    Vec3 vec = faceVerts[j].scale(-1);
                    if (vec.z < 0 && particle.velocityStretchFactor != 0.0f) {
                        vec = new Vec3(vec.x, vec.y, vec.z * (1 + particle.velocityStretchFactor));
                    }
                    vec = vec
                            .xRot(lerpedPitch)
                            .yRot(lerpedYaw)
                            .zRot(lerpedRoll)
                            .scale(particle.scale * data.ageModifier)
                            .add(data.lerpedPos);

                    data.builder.vertex(vec.x, vec.y, vec.z)
                            .uv((float) j / 2, j % 2)
                            .color(particle.rCol, particle.gCol, particle.bCol, particle.alpha)
                            .uv2(data.light)
                            .endVertex();
                }
            }
        }),
        BILLBOARD((particle, data) -> {
            float lerpedYaw = Mth.lerp(data.partialTicks, particle.oYaw, particle.yaw);
            float lerpedPitch = Mth.lerp(data.partialTicks, particle.oPitch, particle.pitch);
            float lerpedRoll = Mth.lerp(data.partialTicks, particle.oRoll, particle.roll);
            Vec3[] faceVerts = new Vec3[]{
                    QuasarParticle.PLANE[0],
                    QuasarParticle.PLANE[1],
                    QuasarParticle.PLANE[2],
                    QuasarParticle.PLANE[3]
            };

            Quaternionf faceCameraRotation = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
            RenderSystem.setShaderTexture(0, particle.spriteData.sprite);
            // turn quat into pitch and yaw
            for (int j = 0; j < 4; j++) {
                Vector3f vec = faceVerts[j].scale(-1).toVector3f();
                if (particle.velocityStretchFactor > 0f) {
                    vec = new Vec3(vec.x * (1 + particle.velocityStretchFactor), vec.y, vec.z).toVector3f();
                }
                if (particle.faceVelocity) {
                    vec = vec
                            .rotateX(lerpedRoll)
                            .rotateY(lerpedPitch)
                            .rotateZ(lerpedYaw);
                }
//                vec = vec.xRot(lerpedPitch).yRot(lerpedYaw).zRot(lerpedRoll);
                vec = faceCameraRotation.transform(vec).mul((float) (particle.scale * data.ageModifier)).add(data.lerpedPos.toVector3f());

                float u = 0;
                float v = 0;
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
                int spritesheetRows = particle.spriteData.getFrameHeight();
                int spritesheetColumns = particle.spriteData.getFrameWidth();
                int spriteCount = particle.spriteData.getFrameCount();
                float animationSpeed = particle.spriteData.getFrameTime();
                // get frame index from age + partial ticks, but it should be an integer.
                int frameIndex = (int) ((particle.age + data.partialTicks) / animationSpeed);
                // get the frame index in the spritesheet
                int frameIndexInSpritesheet = frameIndex % spriteCount;
                // get the row and column of the frame in the spritesheet
                int frameRow = frameIndexInSpritesheet / spritesheetColumns;
                int frameColumn = frameIndexInSpritesheet % spritesheetColumns;
                // get the u and v coordinates of the frame, using u and v which are this vertex's u and v
                float u1 = u * (1f / spritesheetColumns) + frameColumn * (1f / spritesheetColumns);
                float v1 = v * (1f / spritesheetRows) + frameRow * (1f / spritesheetRows);
                data.builder.vertex(vec.x, vec.y, vec.z)
                        .uv(u1, v1)
                        .color(particle.rCol, particle.gCol, particle.bCol, particle.alpha)
                        .uv2(data.light)
                        .endVertex();
            }
        });

        private final BiConsumer<QuasarParticle, QuasarParticleRenderData> renderFunction;

        RenderStyle(BiConsumer<QuasarParticle, QuasarParticleRenderData> renderFunction) {
            this.renderFunction = renderFunction;
        }

        public void render(QuasarParticle particle, QuasarParticleRenderData data) {
            this.renderFunction.accept(particle, data);
        }
    }
}