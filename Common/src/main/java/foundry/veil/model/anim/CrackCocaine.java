package foundry.veil.model.anim;

import net.minecraft.client.model.geom.ModelPart;

import java.util.function.Supplier;

public interface CrackCocaine {
    Supplier<ModelPart> getParent();

    void setParent(Supplier<ModelPart> parent);
}
