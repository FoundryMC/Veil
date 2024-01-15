package foundry.veil.ext;

import net.minecraft.client.model.geom.ModelPart;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Internal
public interface ModelPartExtension {

    Supplier<ModelPart> veil$getParent();

    void veil$setParent(Supplier<ModelPart> parent);

    float veil$getOffsetX();

    float veil$getOffsetY();

    float veil$getOffsetZ();

    void veil$setOffset(float x, float y, float z);

    boolean veil$isChild(ModelPart part);
}
