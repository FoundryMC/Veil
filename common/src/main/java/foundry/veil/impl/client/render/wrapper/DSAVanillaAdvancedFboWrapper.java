package foundry.veil.impl.client.render.wrapper;

import com.mojang.blaze3d.pipeline.RenderTarget;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import static org.lwjgl.opengl.ARBClearTexture.glClearTexImage;
import static org.lwjgl.opengl.ARBDirectStateAccess.*;
import static org.lwjgl.opengl.GL30C.*;

/**
 * Direct-state implementation of {@link VanillaAdvancedFboWrapper}.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public class DSAVanillaAdvancedFboWrapper extends VanillaAdvancedFboWrapper {

    public DSAVanillaAdvancedFboWrapper(Supplier<RenderTarget> renderTargetSupplier) {
        super(renderTargetSupplier);
    }

    @Override
    public void clear(float red, float green, float blue, float alpha, float depth, int clearMask, int... clearBuffers) {
        if (clearMask == 0) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            boolean clearTex = VeilRenderSystem.clearTextureSupported();

            RenderTarget renderTarget = this.toRenderTarget();
            if ((clearMask & GL_COLOR_BUFFER_BIT) != 0) {
                if (clearTex) {
                    glClearTexImage(renderTarget.getColorTextureId(), 0, GL_RGBA, GL_FLOAT, stack.floats(red, green, blue, alpha));
                } else {
                    glClearNamedFramebufferfv(renderTarget.getColorTextureId(), GL_COLOR, 0, stack.floats(red, green, blue, alpha));
                }
            }

            if (renderTarget.useDepth) {
                boolean hasStencilAttachment = this.hasStencilAttachment();
                boolean hasDepth = (clearMask & GL_DEPTH_BUFFER_BIT) != 0;
                boolean hasStencil = hasStencilAttachment && (clearMask & GL_STENCIL_BUFFER_BIT) != 0;
                if (!hasDepth && !hasStencil) {
                    return;
                }

                if (hasStencilAttachment) {
                    if (hasDepth && hasStencil) {
                        if (clearTex) {
                            glClearTexImage(renderTarget.getDepthTextureId(), 0, GL_DEPTH_STENCIL, GL_FLOAT_32_UNSIGNED_INT_24_8_REV, (ByteBuffer) null);
                        } else {
                            glClearNamedFramebufferfi(this.getId(), GL_DEPTH_STENCIL, 0, depth, glGetInteger(GL_STENCIL_CLEAR_VALUE));
                        }
                    } else {
                        // Can't clear the texture if only clearing depth or stencil
                        if (hasDepth) {
                            glClearNamedFramebufferfv(this.getId(), GL_DEPTH, 0, stack.floats(depth));
                        }
                        if (hasStencil) {
                            glClearNamedFramebufferiv(this.getId(), GL_STENCIL, 0, stack.ints(glGetInteger(GL_STENCIL_CLEAR_VALUE)));
                        }
                    }
                } else {
                    if (clearTex) {
                        glClearTexImage(renderTarget.getDepthTextureId(), 0, GL_DEPTH_COMPONENT, GL_FLOAT, stack.floats(depth));
                    } else {
                        glClearNamedFramebufferfv(this.getId(), GL_DEPTH, 0, stack.floats(depth));
                    }
                }
            }
        }

        if (Minecraft.ON_OSX) {
            glGetError();
        }
    }

    @Override
    public void resolveToFbo(int id, int width, int height, int mask, int filtering) {
        glBlitNamedFramebuffer(this.getId(), id, 0, 0, this.getWidth(), this.getHeight(), 0, 0, width, height, mask, filtering);
    }

    @Override
    public void resolveToAdvancedFbo(AdvancedFbo target, int mask, int filtering) {
        glBlitNamedFramebuffer(this.getId(), target.getId(), 0, 0, this.getWidth(), this.getHeight(), 0, 0, target.getWidth(), target.getHeight(), mask, filtering);
    }

    @Override
    public void resolveToRenderTarget(RenderTarget target, int mask, int filtering) {
        glBlitNamedFramebuffer(this.getId(), target.frameBufferId, 0, 0, this.getWidth(), this.getHeight(), 0, 0, target.width, target.height, mask, filtering);
    }
}
