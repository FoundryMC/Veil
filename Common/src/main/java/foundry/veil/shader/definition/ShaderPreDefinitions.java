package foundry.veil.shader.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * <p>Manages pre-defined variables and data in java that can be applied to shaders.</p>
 * <p>Regular definitions are added with {@link #define(String)}, {@link #define(String, String)},
 * and {@link #set(String, String)}. These schedule a shader recompilation every time they are set
 * so shaders can remain up-to-date. </p>
 */
public class ShaderPreDefinitions {

    private final Consumer<String> definitionCallback;
    private final Map<String, String> definitions;
    private final Map<String, String> staticDefinitions;

    /**
     * Creates a new set of predefinitions.
     *
     * @param definitionCallback The callback for when definitions change or <code>null</code> to ignore changes
     */
    public ShaderPreDefinitions(@Nullable Consumer<String> definitionCallback) {
        this.definitionCallback = definitionCallback;
        this.definitions = new HashMap<>();
        this.staticDefinitions = new HashMap<>();
    }

    private @NotNull String getDefinition(@NotNull String name, @Nullable String definition) {
        Objects.requireNonNull(name, "name");
        name = name.toUpperCase(Locale.ROOT);
        if (definition == null) {
            return "#define " + name;
        }
        return "#define " + name + " " + definition;
    }

    /**
     * Sets the value of a definition pair. If the value has changed, all shaders depending on it will recompile.
     *
     * @param name The name of the definition to set
     */
    public void define(@NotNull String name) {
        this.define(name, null);
    }

    /**
     * Sets the value of a definition pair. If the value has changed, all shaders depending on it will recompile.
     *
     * @param name       The name of the definition to set
     * @param definition The value to associate with it or <code>null</code> to only add <code>#define name</code>
     */
    public void define(@NotNull String name, @Nullable String definition) {
        this.set(name, this.getDefinition(name, definition));
    }

    /**
     * Sets the value of a definition pair. If the value has changed, all shaders depending on it will recompile.
     *
     * @param name       The name of the definition to set
     * @param definition The value to associate with it
     */
    public void set(@NotNull String name, @NotNull String definition) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(definition, "definition");
        String previous = this.definitions.put(name, definition);
        if (this.definitionCallback != null && !Objects.equals(previous, definition)) {
            this.definitionCallback.accept(name);
        }
    }

    /**
     * Sets a definition added to all shaders. These should be treated as static final variables.
     *
     * @param name The name of the definition to set
     */
    public void defineStatic(@NotNull String name) {
        this.defineStatic(name, null);
    }

    /**
     * Sets a definition added to all shaders. These should be treated as static final variables.
     *
     * @param name       The name of the definition to set
     * @param definition The value to associate with it or <code>null</code> to only add <code>#define name</code>
     */
    public void defineStatic(@NotNull String name, @Nullable String definition) {
        this.setStatic(name, this.getDefinition(name, definition));
    }

    /**
     * Sets a definition added to all shaders. These should be treated as static final variables.
     *
     * @param name       The name of the definition to set
     * @param definition The value to associate with it
     */
    public void setStatic(@NotNull String name, @NotNull String definition) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(definition, "definition");
        this.staticDefinitions.put(name, definition);
    }

    /**
     * Removes the definition with the specified name.
     *
     * @param name The name of the definition to remove
     */
    public void remove(@NotNull String name) {
        if (this.definitionCallback != null && this.definitions.remove(name) != null) {
            this.definitionCallback.accept(name);
        }
    }

    /**
     * Adds definitions that never change during runtime. This is for constants, like a debug flag.
     *
     * @param definitionConsumer The consumer for definition lines
     */
    public void addStaticDefinitions(@NotNull Consumer<String> definitionConsumer) {
        this.staticDefinitions.values().forEach(definitionConsumer);
    }

    /**
     * Retrieves a definition by name.
     *
     * @param name The name of the definition
     * @return The definition with that name or <code>null</code> if it doesn't exist
     */
    public @Nullable String getDefinition(@NotNull String name) {
        return this.definitions.get(name);
    }
}
