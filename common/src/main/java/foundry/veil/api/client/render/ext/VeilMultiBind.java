package foundry.veil.api.client.render.ext;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.Veil;
import org.lwjgl.opengl.ARBMultiBind;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.ARBMultiBind.glBindSamplers;
import static org.lwjgl.opengl.ARBMultiBind.glBindTextures;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.*;
import static org.lwjgl.opengl.GL32C.*;
import static org.lwjgl.opengl.GL33C.glBindSampler;
import static org.lwjgl.opengl.GL45C.GL_TEXTURE_TARGET;
import static org.lwjgl.opengl.GL45C.glGetTextureParameteri;

/**
 * Provides access to {@link ARBMultiBind} functionality for all platforms.
 *
 * @author Ocelot
 */
public enum VeilMultiBind {
    LEGACY {
        @Override
        public void bindTextures(int first, IntBuffer textures) {
            int activeTexture = GlStateManager._getActiveTexture();
            for (int i = 0; i < textures.limit(); i++) {
                RenderSystem.activeTexture(GL_TEXTURE0 + first + i);
                int texture = textures.get(i);
                int target = getTarget(texture);
                if (target == GL_TEXTURE_2D && first + i < 12) {
                    RenderSystem.bindTexture(texture);
                } else {
                    glBindTexture(target, texture);
                }
            }
            RenderSystem.activeTexture(activeTexture);
        }

        @Override
        public void bindTextures(int first, int... textures) {
            int activeTexture = GlStateManager._getActiveTexture();
            for (int i = 0; i < textures.length; i++) {
                RenderSystem.activeTexture(GL_TEXTURE0 + first + i);
                int texture = textures[i];
                int target = getTarget(texture);
                if (target == GL_TEXTURE_2D && first + i < 12) {
                    RenderSystem.bindTexture(texture);
                } else {
                    glBindTexture(target, texture);
                }
            }
            RenderSystem.activeTexture(activeTexture);
        }

        @Override
        public void bindSamplers(int first, IntBuffer samplers) {
            for (int i = 0; i < samplers.limit(); i++) {
                glBindSampler(first + i, samplers.get(i));
            }
        }

        @Override
        public void bindSamplers(int first, int... samplers) {
            for (int i = 0; i < samplers.length; i++) {
                glBindSampler(first + i, samplers[i]);
            }
        }
    },
    SUPPORTED {
        @Override
        public void bindTextures(int first, IntBuffer textures) {
            int invalidCount = Math.min(12 - first, textures.limit());
            for (int i = first; i < invalidCount; i++) {
                int texture = textures.get(i - first);
                int target = getTarget(texture);
                if (target == GL_TEXTURE_2D) {
                    GlStateManager.TEXTURES[i].binding = texture;
                }
            }

            glBindTextures(first, textures);
        }

        @Override
        public void bindTextures(int first, int... textures) {
            int invalidCount = Math.min(12 - first, textures.length);
            for (int i = first; i < invalidCount; i++) {
                int texture = textures[i - first];
                int target = getTarget(texture);
                if (target == GL_TEXTURE_2D) {
                    GlStateManager.TEXTURES[i].binding = texture;
                }
            }

            glBindTextures(first, textures);
        }

        @Override
        public void bindSamplers(int first, IntBuffer samplers) {
            glBindSamplers(first, samplers);
        }

        @Override
        public void bindSamplers(int first, int... samplers) {
            glBindSamplers(first, samplers);
        }
    };

    private static final int[] CHECK_BINDINGS = {
            // These 3 are the most likely, so check them first
            GL_TEXTURE_BINDING_2D,
            GL_TEXTURE_BINDING_2D_ARRAY,
            GL_TEXTURE_BINDING_CUBE_MAP,

            GL_TEXTURE_BINDING_1D,
            GL_TEXTURE_BINDING_3D,
            GL_TEXTURE_BINDING_RECTANGLE,
            GL_TEXTURE_BINDING_BUFFER,
            GL_TEXTURE_BINDING_1D_ARRAY,
            GL_TEXTURE_BINDING_2D_MULTISAMPLE,
            GL_TEXTURE_BINDING_2D_MULTISAMPLE_ARRAY,
    };
    private static final int[] CHECK_TARGETS = {
            // These 3 are the most likely, so check them first
            GL_TEXTURE_2D,
            GL_TEXTURE_2D_ARRAY,
            GL_TEXTURE_CUBE_MAP,

            GL_TEXTURE_1D,
            GL_TEXTURE_3D,
            GL_TEXTURE_RECTANGLE,
            GL_TEXTURE_BUFFER,
            GL_TEXTURE_1D_ARRAY,
            GL_TEXTURE_2D_MULTISAMPLE,
            GL_TEXTURE_2D_MULTISAMPLE_ARRAY,
    };

    private static int getTarget(int texture) {
        GLCapabilities caps = GL.getCapabilities();
        if (caps.glGetTextureParameteriv != 0L) { // Last ditch effort if the platform has the method anyways
            return glGetTextureParameteri(texture, GL_TEXTURE_TARGET);
        }
        // Nothing else I can do, so do the dirty hack to figure out the target

        // Clear errors
        while (glGetError() != GL_NO_ERROR) {
        }

        for (int i = 0; i < CHECK_TARGETS.length; i++) {
            int target = CHECK_TARGETS[i];
            int old = glGetInteger(CHECK_BINDINGS[i]);
            glBindTexture(target, texture);
            if (glGetError() == GL_NO_ERROR) {
                glBindTexture(target, old);
                return target;
            }
            glBindTexture(target, old);
        }

        // Should never happen
        return GL_TEXTURE_2D;
    }


    private static VeilMultiBind multiBind;

    /**
     * Binds the specified texture ids to sequential texture units and invalidates the GLStateManager.
     *
     * @param first    The first unit to bind to
     * @param textures The textures to bind
     */
    public abstract void bindTextures(int first, IntBuffer textures);

    /**
     * Binds the specified texture ids to sequential texture units and invalidates the GLStateManager.
     *
     * @param first    The first unit to bind to
     * @param textures The textures to bind
     */
    public abstract void bindTextures(int first, int... textures);

    /**
     * Binds the specified sampler ids to sequential texture units and invalidates the GLStateManager.
     *
     * @param first    The first unit to bind to
     * @param samplers The samplers to bind
     */
    public abstract void bindSamplers(int first, IntBuffer samplers);

    /**
     * Binds the specified sampler ids to sequential texture units and invalidates the GLStateManager.
     *
     * @param first    The first unit to bind to
     * @param samplers The samplers to bind
     */
    public abstract void bindSamplers(int first, int... samplers);

    /**
     * @return The best implementation of multi-bind for this platform
     */
    public static VeilMultiBind get() {
        if (multiBind == null) {
            GLCapabilities caps = GL.getCapabilities();
            if (caps.OpenGL44 || caps.GL_ARB_multi_bind) {
                multiBind = SUPPORTED;
                Veil.LOGGER.info("Multi-Bind supported, using core");
            } else {
                multiBind = LEGACY;
                Veil.LOGGER.info("Multi-Bind unsupported, using legacy");
            }
        }
        return multiBind;
    }
}
