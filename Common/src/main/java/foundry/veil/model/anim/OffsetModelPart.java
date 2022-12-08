package foundry.veil.model.anim;

import net.minecraft.client.model.geom.ModelPart;

public interface OffsetModelPart {
    float getOffsetX();
    float getOffsetY();
    float getOffsetZ();
    void setOffset(float x, float y, float z);
    boolean isChild(ModelPart part);
}
