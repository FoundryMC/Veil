package foundry.veil.shader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import foundry.veil.Veil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.FileUtil;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

public class ExtendedShaderInstance extends ShaderInstance {

    private static final String SHADER_PATH = "shaders/core/";
    private static final String SHADER_INCLUDE_PATH = "shaders/include/";
    static final Logger LOGGER = LogUtils.getLogger();
    private static final AbstractUniform DUMMY_UNIFORM = new AbstractUniform();
    private static final boolean ALWAYS_REAPPLY = true;
    private static ShaderInstance lastAppliedShader;
    private static int lastProgramId = -1;
    private final Map<String, Object> samplerMap = Maps.newHashMap();
    private final List<String> samplerNames = Lists.newArrayList();
    private final List<Integer> samplerLocations = Lists.newArrayList();
    private final List<Uniform> uniforms = Lists.newArrayList();
    private final List<Integer> uniformLocations = Lists.newArrayList();
    private final Map<String, Uniform> uniformMap = Maps.newHashMap();
    private final int programId;
    private final String name;
    private boolean dirty;
    private final BlendMode blend;
    private final List<Integer> attributes;
    private final List<String> attributeNames;
    private final Program vertexProgram;
    private final Program fragmentProgram;
    private final VertexFormat vertexFormat;
    @Nullable
    public final Uniform MODEL_VIEW_MATRIX;
    @Nullable
    public final Uniform PROJECTION_MATRIX;
    @Nullable
    public final Uniform INVERSE_VIEW_ROTATION_MATRIX;
    @Nullable
    public final Uniform TEXTURE_MATRIX;
    @Nullable
    public final Uniform SCREEN_SIZE;
    @Nullable
    public final Uniform COLOR_MODULATOR;
    @Nullable
    public final Uniform LIGHT0_DIRECTION;
    @Nullable
    public final Uniform LIGHT1_DIRECTION;
    @Nullable
    public final Uniform FOG_START;
    @Nullable
    public final Uniform FOG_END;
    @Nullable
    public final Uniform FOG_COLOR;
    @Nullable
    public final Uniform FOG_SHAPE;
    @Nullable
    public final Uniform LINE_WIDTH;
    @Nullable
    public final Uniform GAME_TIME;
    @Nullable
    public final Uniform CHUNK_OFFSET;
    // literally just copied from vanilla with 2 minor changes because i didnt want to write 20+ accessors
    public ExtendedShaderInstance(ResourceProvider $$0, String $$1, VertexFormat $$2, String id) throws IOException {
        super($$0, $$1, $$2);
        this.name = $$1;
        this.vertexFormat = $$2;

        ResourceLocation rl = ResourceLocation.tryParse(id + ":shaders/core/" + $$1 + ".json");
        try {
            Reader $$4 = $$0.openAsReader(Objects.requireNonNull(rl));

            try {
                JsonObject $$5 = GsonHelper.parse($$4);
                String $$6 = GsonHelper.getAsString($$5, "vertex");
                String $$7 = GsonHelper.getAsString($$5, "fragment");
                JsonArray $$8 = GsonHelper.getAsJsonArray($$5, "samplers", (JsonArray)null);
                if ($$8 != null) {
                    int $$9 = 0;

                    for(Iterator var11 = $$8.iterator(); var11.hasNext(); ++$$9) {
                        JsonElement $$10 = (JsonElement)var11.next();

                        try {
                            this.parseSamplerNode($$10);
                        } catch (Exception var20) {
                            ChainedJsonException $$12 = ChainedJsonException.forException(var20);
                            $$12.prependJsonKey("samplers[" + $$9 + "]");
                            throw $$12;
                        }
                    }
                }

                JsonArray $$13 = GsonHelper.getAsJsonArray($$5, "attributes", (JsonArray)null);
                if ($$13 != null) {
                    int $$14 = 0;
                    this.attributes = Lists.newArrayListWithCapacity($$13.size());
                    this.attributeNames = Lists.newArrayListWithCapacity($$13.size());

                    for(Iterator var27 = $$13.iterator(); var27.hasNext(); ++$$14) {
                        JsonElement $$15 = (JsonElement)var27.next();

                        try {
                            this.attributeNames.add(GsonHelper.convertToString($$15, "attribute"));
                        } catch (Exception var19) {
                            ChainedJsonException $$17 = ChainedJsonException.forException(var19);
                            $$17.prependJsonKey("attributes[" + $$14 + "]");
                            throw $$17;
                        }
                    }
                } else {
                    this.attributes = null;
                    this.attributeNames = null;
                }

                JsonArray $$18 = GsonHelper.getAsJsonArray($$5, "uniforms", (JsonArray)null);
                int $$23;
                if ($$18 != null) {
                    $$23 = 0;

                    for(Iterator var29 = $$18.iterator(); var29.hasNext(); ++$$23) {
                        JsonElement $$20 = (JsonElement)var29.next();

                        try {
                            this.parseUniformNode($$20);
                        } catch (Exception var18) {
                            ChainedJsonException $$22 = ChainedJsonException.forException(var18);
                            $$22.prependJsonKey("uniforms[" + $$23 + "]");
                            throw $$22;
                        }
                    }
                }

                this.blend = parseBlendNode(GsonHelper.getAsJsonObject($$5, "blend", (JsonObject)null));
                this.vertexProgram = getOrCreate($$0, Program.Type.VERTEX, $$6);
                this.fragmentProgram = getOrCreate($$0, Program.Type.FRAGMENT, $$7);
                this.programId = ProgramManager.createProgram();
                if (this.attributeNames != null) {
                    $$23 = 0;

                    for(UnmodifiableIterator var30 = $$2.getElementAttributeNames().iterator(); var30.hasNext(); ++$$23) {
                        String $$24 = (String)var30.next();
                        Uniform.glBindAttribLocation(this.programId, $$23, $$24);
                        this.attributes.add($$23);
                    }
                }

                ProgramManager.linkShader(this);
                this.updateLocations();
            } catch (Throwable var21) {
                if ($$4 != null) {
                    try {
                        $$4.close();
                    } catch (Throwable var17) {
                        var21.addSuppressed(var17);
                    }
                }

                throw var21;
            }

            if ($$4 != null) {
                $$4.close();
            }
        } catch (Exception var22) {
            ChainedJsonException $$27 = ChainedJsonException.forException(var22);
            $$27.setFilenameAndFlush(rl.getPath());
            throw $$27;
        }

        this.markDirty();
        this.MODEL_VIEW_MATRIX = this.getUniform("ModelViewMat");
        this.PROJECTION_MATRIX = this.getUniform("ProjMat");
        this.INVERSE_VIEW_ROTATION_MATRIX = this.getUniform("IViewRotMat");
        this.TEXTURE_MATRIX = this.getUniform("TextureMat");
        this.SCREEN_SIZE = this.getUniform("ScreenSize");
        this.COLOR_MODULATOR = this.getUniform("ColorModulator");
        this.LIGHT0_DIRECTION = this.getUniform("Light0_Direction");
        this.LIGHT1_DIRECTION = this.getUniform("Light1_Direction");
        this.FOG_START = this.getUniform("FogStart");
        this.FOG_END = this.getUniform("FogEnd");
        this.FOG_COLOR = this.getUniform("FogColor");
        this.FOG_SHAPE = this.getUniform("FogShape");
        this.LINE_WIDTH = this.getUniform("LineWidth");
        this.GAME_TIME = this.getUniform("GameTime");
        this.CHUNK_OFFSET = this.getUniform("ChunkOffset");
    }
    private static Program getOrCreate(final ResourceProvider $$0, Program.Type $$1, String $$2) throws IOException {
        Program $$3 = (Program)$$1.getPrograms().get($$2);
        Program $$8;
        if ($$3 == null) {
            String $$4 = "shaders/core/" + $$2 + $$1.getExtension();
            Resource $$5 = $$0.getResourceOrThrow(new ResourceLocation($$4));
            InputStream $$6 = $$5.open();

            try {
                final String $$7 = FileUtil.getFullResourcePath($$4);
                $$8 = Program.compileShader($$1, $$2, $$6, $$5.sourcePackId(), new GlslPreprocessor() {
                    private final Set<String> importedPaths = Sets.newHashSet();

                    public String applyImport(boolean $$0x, String $$1) {
                        String var10000 = $$0x ? $$7 : "shaders/include/";
                        $$1 = FileUtil.normalizeResourcePath(var10000 + $$1);
                        if (!this.importedPaths.add($$1)) {
                            return null;
                        } else {
                            ResourceLocation $$2 = new ResourceLocation($$1);

                            try {
                                Reader $$3 = $$0.openAsReader($$2);

                                String var5;
                                try {
                                    var5 = IOUtils.toString($$3);
                                } catch (Throwable var8) {
                                    if ($$3 != null) {
                                        try {
                                            $$3.close();
                                        } catch (Throwable var7) {
                                            var8.addSuppressed(var7);
                                        }
                                    }

                                    throw var8;
                                }

                                if ($$3 != null) {
                                    $$3.close();
                                }

                                return var5;
                            } catch (IOException var9) {
                                Veil.LOGGER.error("Could not open GLSL import {}: {}", $$1, var9.getMessage());
                                return "#error " + var9.getMessage();
                            }
                        }
                    }
                });
            } catch (Throwable var11) {
                if ($$6 != null) {
                    try {
                        $$6.close();
                    } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                    }
                }

                throw var11;
            }

            if ($$6 != null) {
                $$6.close();
            }
        } else {
            $$8 = $$3;
        }

        return $$8;
    }

    public static BlendMode parseBlendNode(JsonObject $$0) {
        if ($$0 == null) {
            return new BlendMode();
        } else {
            int $$1 = 32774;
            int $$2 = 1;
            int $$3 = 0;
            int $$4 = 1;
            int $$5 = 0;
            boolean $$6 = true;
            boolean $$7 = false;
            if (GsonHelper.isStringValue($$0, "func")) {
                $$1 = BlendMode.stringToBlendFunc($$0.get("func").getAsString());
                if ($$1 != 32774) {
                    $$6 = false;
                }
            }

            if (GsonHelper.isStringValue($$0, "srcrgb")) {
                $$2 = BlendMode.stringToBlendFactor($$0.get("srcrgb").getAsString());
                if ($$2 != 1) {
                    $$6 = false;
                }
            }

            if (GsonHelper.isStringValue($$0, "dstrgb")) {
                $$3 = BlendMode.stringToBlendFactor($$0.get("dstrgb").getAsString());
                if ($$3 != 0) {
                    $$6 = false;
                }
            }

            if (GsonHelper.isStringValue($$0, "srcalpha")) {
                $$4 = BlendMode.stringToBlendFactor($$0.get("srcalpha").getAsString());
                if ($$4 != 1) {
                    $$6 = false;
                }

                $$7 = true;
            }

            if (GsonHelper.isStringValue($$0, "dstalpha")) {
                $$5 = BlendMode.stringToBlendFactor($$0.get("dstalpha").getAsString());
                if ($$5 != 0) {
                    $$6 = false;
                }

                $$7 = true;
            }

            if ($$6) {
                return new BlendMode();
            } else {
                return $$7 ? new BlendMode($$2, $$3, $$4, $$5, $$1) : new BlendMode($$2, $$3, $$1);
            }
        }
    }

    public void close() {
        Iterator var1 = this.uniforms.iterator();

        while(var1.hasNext()) {
            Uniform $$0 = (Uniform)var1.next();
            $$0.close();
        }

        ProgramManager.releaseProgram(this);
    }

    public void clear() {
        RenderSystem.assertOnRenderThread();
        ProgramManager.glUseProgram(0);
        lastProgramId = -1;
        lastAppliedShader = null;
        int $$0 = GlStateManager._getActiveTexture();

        for(int $$1 = 0; $$1 < this.samplerLocations.size(); ++$$1) {
            if (this.samplerMap.get(this.samplerNames.get($$1)) != null) {
                GlStateManager._activeTexture('蓀' + $$1);
                GlStateManager._bindTexture(0);
            }
        }

        GlStateManager._activeTexture($$0);
    }

    public void apply() {
        RenderSystem.assertOnRenderThread();
        this.dirty = false;
        lastAppliedShader = this;
        this.blend.apply();
        if (this.programId != lastProgramId) {
            ProgramManager.glUseProgram(this.programId);
            lastProgramId = this.programId;
        }

        int $$0 = GlStateManager._getActiveTexture();

        for(int $$1 = 0; $$1 < this.samplerLocations.size(); ++$$1) {
            String $$2 = (String)this.samplerNames.get($$1);
            if (this.samplerMap.get($$2) != null) {
                int $$3 = Uniform.glGetUniformLocation(this.programId, $$2);
                Uniform.uploadInteger($$3, $$1);
                RenderSystem.activeTexture('蓀' + $$1);
                RenderSystem.enableTexture();
                Object $$4 = this.samplerMap.get($$2);
                int $$5 = -1;
                if ($$4 instanceof RenderTarget) {
                    $$5 = ((RenderTarget)$$4).getColorTextureId();
                } else if ($$4 instanceof AbstractTexture) {
                    $$5 = ((AbstractTexture)$$4).getId();
                } else if ($$4 instanceof Integer) {
                    $$5 = (Integer)$$4;
                }

                if ($$5 != -1) {
                    RenderSystem.bindTexture($$5);
                }
            }
        }

        GlStateManager._activeTexture($$0);
        Iterator var7 = this.uniforms.iterator();

        while(var7.hasNext()) {
            Uniform $$6 = (Uniform)var7.next();
            $$6.upload();
        }

    }

    public void markDirty() {
        this.dirty = true;
    }

    @Nullable
    public Uniform getUniform(String $$0) {
        RenderSystem.assertOnRenderThread();
        return (Uniform)this.uniformMap.get($$0);
    }

    public AbstractUniform safeGetUniform(String $$0) {
        RenderSystem.assertOnGameThread();
        Uniform $$1 = this.getUniform($$0);
        return (AbstractUniform)($$1 == null ? DUMMY_UNIFORM : $$1);
    }

    private void updateLocations() {
        RenderSystem.assertOnRenderThread();
        IntList $$0 = new IntArrayList();

        int $$4;
        for($$4 = 0; $$4 < this.samplerNames.size(); ++$$4) {
            String $$2 = (String)this.samplerNames.get($$4);
            int $$3 = Uniform.glGetUniformLocation(this.programId, $$2);
            if ($$3 == -1) {
                LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", this.name, $$2);
                this.samplerMap.remove($$2);
                $$0.add($$4);
            } else {
                this.samplerLocations.add($$3);
            }
        }

        for($$4 = $$0.size() - 1; $$4 >= 0; --$$4) {
            int $$5 = $$0.getInt($$4);
            this.samplerNames.remove($$5);
        }

        Iterator var6 = this.uniforms.iterator();

        while(var6.hasNext()) {
            Uniform $$6 = (Uniform)var6.next();
            String $$7 = $$6.getName();
            int $$8 = Uniform.glGetUniformLocation(this.programId, $$7);
            if ($$8 == -1) {
                LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name, $$7);
            } else {
                this.uniformLocations.add($$8);
                $$6.setLocation($$8);
                this.uniformMap.put($$7, $$6);
            }
        }

    }

    private void parseSamplerNode(JsonElement $$0) {
        JsonObject $$1 = GsonHelper.convertToJsonObject($$0, "sampler");
        String $$2 = GsonHelper.getAsString($$1, "name");
        if (!GsonHelper.isStringValue($$1, "file")) {
            this.samplerMap.put($$2, (Object)null);
            this.samplerNames.add($$2);
        } else {
            this.samplerNames.add($$2);
        }
    }

    public void setSampler(String $$0, Object $$1) {
        this.samplerMap.put($$0, $$1);
        this.markDirty();
    }

    private void parseUniformNode(JsonElement $$0) throws ChainedJsonException {
        JsonObject $$1 = GsonHelper.convertToJsonObject($$0, "uniform");
        String $$2 = GsonHelper.getAsString($$1, "name");
        int $$3 = Uniform.getTypeFromString(GsonHelper.getAsString($$1, "type"));
        int $$4 = GsonHelper.getAsInt($$1, "count");
        float[] $$5 = new float[Math.max($$4, 16)];
        JsonArray $$6 = GsonHelper.getAsJsonArray($$1, "values");
        if ($$6.size() != $$4 && $$6.size() > 1) {
            throw new ChainedJsonException("Invalid amount of values specified (expected " + $$4 + ", found " + $$6.size() + ")");
        } else {
            int $$7 = 0;

            for(Iterator var9 = $$6.iterator(); var9.hasNext(); ++$$7) {
                JsonElement $$8 = (JsonElement)var9.next();

                try {
                    $$5[$$7] = GsonHelper.convertToFloat($$8, "value");
                } catch (Exception var13) {
                    ChainedJsonException $$10 = ChainedJsonException.forException(var13);
                    $$10.prependJsonKey("values[" + $$7 + "]");
                    throw $$10;
                }
            }

            if ($$4 > 1 && $$6.size() == 1) {
                while($$7 < $$4) {
                    $$5[$$7] = $$5[0];
                    ++$$7;
                }
            }

            int $$11 = $$4 > 1 && $$4 <= 4 && $$3 < 8 ? $$4 - 1 : 0;
            Uniform $$12 = new Uniform($$2, $$3 + $$11, $$4, this);
            if ($$3 <= 3) {
                $$12.setSafe((int)$$5[0], (int)$$5[1], (int)$$5[2], (int)$$5[3]);
            } else if ($$3 <= 7) {
                $$12.setSafe($$5[0], $$5[1], $$5[2], $$5[3]);
            } else {
                $$12.set(Arrays.copyOfRange($$5, 0, $$4));
            }

            this.uniforms.add($$12);
        }
    }

    public Program getVertexProgram() {
        return this.vertexProgram;
    }

    public Program getFragmentProgram() {
        return this.fragmentProgram;
    }

    public void attachToProgram() {
        this.fragmentProgram.attachToShader(this);
        this.vertexProgram.attachToShader(this);
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.programId;
    }

}
