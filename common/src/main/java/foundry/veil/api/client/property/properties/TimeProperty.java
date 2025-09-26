package foundry.veil.api.client.property.properties;

import foundry.veil.api.client.property.ImmutableProperty;
import foundry.veil.api.client.property.Property;
import foundry.veil.api.client.registry.PropertyRegistry;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.api.flare.data.effect.FlareMaterial;
import foundry.veil.api.flare.modifier.PropertyModifier;
import gg.moonflower.molangcompiler.api.MolangExpression;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;

/**
 * <p>Applies time in seconds since client booted up according to this vector: (t/20, t, t*2, t*3)<p/>
 * Added as a default property named '_Time' in all {@link FlareMaterial}.
 *
 * @author GuyApooye
 */
public class TimeProperty extends Property<Vector4f> implements ImmutableProperty {

    private static final Minecraft minecraft = Minecraft.getInstance();
    public static final TimeProperty INSTANCE = new TimeProperty();

    private TimeProperty() {
        super(PropertyRegistry.VEC4.get(), new Vector4f());
    }

    @Override
    public void applyValue(ShaderUniformAccess uniform, int location) {
        float time = minecraft.getFrameTimeNs() * 1e-9f;
        value.set(time / 20.0f, time, 2.0f * time, Mth.sin(time));
        uniform.setVector(value);
    }

    @Override
    public void modify(Vector4f value, PropertyModifier.PropertyModifierMode mode, Optional<List<MolangExpression>> optionalMolang) {
    }

    @Override
    protected Vector4f cloneValue(Vector4f value) {
        return new Vector4f(value);
    }
}
