package foundry.veil.api.client.property.properties;

import com.mojang.blaze3d.shaders.Uniform;
import foundry.veil.api.client.property.ImmutableProperty;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.flare.data.effect.FlareMaterial;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;

/**
 * <p>Applies time in seconds since client booted up according to this vector: (t/20, t, t*2, t*3)</p>
 * Added as a default property named <code>_Time</code> in all {@link FlareMaterial}.
 *
 * @author GuyApooye
 */
@ImmutableProperty
public class TimeProperty extends Property<Vector4f> {

    public static final TimeProperty INSTANCE = new TimeProperty();

    private TimeProperty() {
        super(PropertyRegistry.VEC4.get(), new Vector4f());
    }

    @Override
    public void applyValue(String name, ShaderInstance shader) {
        Uniform uniform = shader.getUniform(name);
        if (uniform != null) {
            float time = System.nanoTime() * 1e-9f;
            uniform.set(this.value.set(time / 20.0f, time, 2.0f * time, Mth.sin(time % Mth.TWO_PI)));
        }
    }

    @Override
    public void modify(Vector4f value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
    }

    @Override
    protected Vector4f cloneValue(Vector4f value) {
        return new Vector4f(value);
    }
}
