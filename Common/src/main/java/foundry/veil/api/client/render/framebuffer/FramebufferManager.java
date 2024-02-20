package foundry.veil.api.client.render.framebuffer;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import foundry.veil.Veil;
import foundry.veil.api.CodecReloadListener;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;

import java.util.*;

import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

/**
 * <p>Manages all framebuffers and custom definitions specified in files.
 * All framebuffers except for the main one can be customized from the
 * <code>modid:pinwheel/framebuffers</code> folder in the assets.</p>
 *
 * @author Ocelot
 */
public class FramebufferManager extends CodecReloadListener<FramebufferDefinition> implements NativeResource {

    private static final ResourceLocation MAIN = new ResourceLocation("main");

    public static final Codec<ResourceLocation> FRAMEBUFFER_CODEC = Codec.STRING.comapFlatMap(name -> {
        try {
            if (!name.contains(":")) {
                return DataResult.success(new ResourceLocation("temp", name));
            }

            return DataResult.success(new ResourceLocation(name));
        } catch (ResourceLocationException e) {
            return DataResult.error(() -> "Not a valid resource location: " + name + ". " + e.getMessage());
        }
    }, location -> "temp".equals(location.getNamespace()) ? location.getPath() : location.toString()).stable();

    private final Map<ResourceLocation, FramebufferDefinition> framebufferDefinitions;
    private final Map<ResourceLocation, AdvancedFbo> framebuffers;
    private final Map<ResourceLocation, AdvancedFbo> framebuffersView;
    private final Set<ResourceLocation> screenFramebuffers;

    /**
     * Creates a new instance of the framebuffer manager.
     */
    public FramebufferManager() {
        super(FramebufferDefinition.CODEC, FileToIdConverter.json("pinwheel/framebuffers"));
        this.framebufferDefinitions = new HashMap<>();
        this.framebuffers = new HashMap<>();
        this.framebuffersView = Collections.unmodifiableMap(this.framebuffers);
        this.screenFramebuffers = new HashSet<>();
    }

    private void init() {
        this.free();

        Window window = Minecraft.getInstance().getWindow();
        MolangRuntime runtime = MolangRuntime.runtime()
                .setQuery("screen_width", window.getWidth())
                .setQuery("screen_height", window.getHeight())
                .create();

        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.framebufferDefinitions.forEach((name, definition) -> {
            this.initFramebuffer(name, definition, runtime);
            if (!definition.width().isConstant() || !definition.height().isConstant()) {
                this.screenFramebuffers.add(name);
            }
        });
        AdvancedFbo.unbind();
        this.framebuffers.put(MAIN, AdvancedFbo.getMainFramebuffer());
    }

    private void initFramebuffer(ResourceLocation name, FramebufferDefinition definition, MolangEnvironment runtime) {
        try {
            AdvancedFbo fbo = definition.createBuilder(runtime).build(true);
            fbo.bindDraw(false);
            fbo.clear();
            this.framebuffers.put(name, fbo);
        } catch (Exception e) {
            Veil.LOGGER.error("Failed to initialize framebuffer: {}", name, e);
        }
    }

    @ApiStatus.Internal
    public void resizeFramebuffers(int width, int height) {
        MolangRuntime runtime = MolangRuntime.runtime()
                .setQuery("screen_width", width)
                .setQuery("screen_height", height)
                .create();

        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        for (ResourceLocation name : this.screenFramebuffers) {
            AdvancedFbo fbo = this.framebuffers.remove(name);
            if (fbo != null) {
                fbo.free();
            }

            FramebufferDefinition definition = this.framebufferDefinitions.get(name);
            if (definition != null) {
                this.initFramebuffer(name, definition, runtime);
            }
        }
        AdvancedFbo.unbind();
    }

    @ApiStatus.Internal
    public void clear() {
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.framebuffers.forEach((name, fbo) -> {
            if (MAIN.equals(name)) {
                return;
            }

            fbo.bindDraw(false);
            fbo.clear();
        });

        // Manual unbind to restore the default mc state
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /**
     * Retrieves a framebuffer by the specified name.
     *
     * @param name The name of the framebuffer to retrieve.
     * @return The framebuffer by that name
     */
    public @Nullable AdvancedFbo getFramebuffer(ResourceLocation name) {
        return this.framebuffers.get(name);
    }

    /**
     * @return All custom framebuffers loaded
     */
    public Map<ResourceLocation, AdvancedFbo> getFramebuffers() {
        return this.framebuffersView;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, FramebufferDefinition> data, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        this.framebufferDefinitions.clear();
        this.framebufferDefinitions.putAll(data);
        this.init();
        Veil.LOGGER.info("Loaded {} framebuffers", this.framebufferDefinitions.size());
    }

    @Override
    public void free() {
        this.framebuffers.remove(MAIN);
        this.framebuffers.values().forEach(AdvancedFbo::free);
        this.framebuffers.clear();
        this.screenFramebuffers.clear();
    }
}
