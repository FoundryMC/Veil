package foundry.veil.shader.texture;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import foundry.veil.framebuffer.AdvancedFbo;
import foundry.veil.pipeline.VeilRenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * Source for post textures. This allows resource location textures as well as other special types.
 *
 * @author Ocelot
 */
public interface ShaderTextureSource {

    Codec<Type> TYPE_CODEC = Codec.STRING.flatXmap(name -> Optional.ofNullable(Type.byName(name))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown post texture source: " + name)),
            object -> DataResult.success(object.name().toLowerCase(Locale.ROOT))
    );
    Codec<ShaderTextureSource> CODEC = Codec.either(ResourceLocation.CODEC,
                    TYPE_CODEC.<ShaderTextureSource>dispatch(ShaderTextureSource::getType, Type::getCodec))
            .xmap(either -> either.map(LocationSource::new, right -> right),
                    source -> source instanceof LocationSource l ? Either.left(l.location()) : Either.right(source));

    Context GLOBAL_CONTEXT = new Context() {
        @Override
        public @Nullable AdvancedFbo getFramebuffer(ResourceLocation name) {
            return VeilRenderSystem.getRenderer().getFramebufferManager().getFramebuffer(name);
        }

        @Override
        public AbstractTexture getTexture(ResourceLocation name) {
            return Minecraft.getInstance().getTextureManager().getTexture(name);
        }
    };

    /**
     * Retrieves the id of this texture based on context.
     *
     * @param context The context to use
     * @return The id of the texture to bind
     */
    int getId(Context context);

    /**
     * @return The type of shader texture this is
     */
    Type getType();

    /**
     * Types of post textures that can be used.
     *
     * @author Ocelot
     */
    enum Type {
        LOCATION(LocationSource.CODEC),
        FRAMEBUFFER(FramebufferSource.CODEC);

        private final Codec<? extends ShaderTextureSource> codec;

        Type(Codec<? extends ShaderTextureSource> codec) {
            this.codec = codec;
        }

        /**
         * @return The codec for this specific type
         */
        public Codec<? extends ShaderTextureSource> getCodec() {
            return this.codec;
        }

        /**
         * Retrieves a type by name.
         *
         * @param name The name of the type to retrieve
         * @return The type by that name
         */
        public static @Nullable Type byName(String name) {
            for (Type type : Type.values()) {
                if (type.name().toLowerCase(Locale.ROOT).equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * Context for applying shader textures.
     *
     * @author Ocelot
     */
    interface Context {

        /**
         * Retrieves a framebuffer by id.
         *
         * @param name The name of the framebuffer to retrieve
         * @return The framebuffer with that id or <code>null</code> if it was not found
         */
        @Nullable AdvancedFbo getFramebuffer(ResourceLocation name);

        /**
         * Retrieves a texture by id.
         *
         * @param name The name of the texture to retrieve
         * @return The texture with that id or the missing texture if it was not found
         */
        AbstractTexture getTexture(ResourceLocation name);
    }
}
