package foundry.veil.postprocessing;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.Veil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class InstantiatedPostProcessor<I extends DynamicEffectInstance> extends PostProcessor {
    private final List<DynamicEffectInstance> instances = new ArrayList<>(getMaxInstances());

    private final DataBuffer dataBuffer = new DataBuffer();

    /**
     * THIS VALUE SHOULD NOT CHANGE!!!
     * @return max fx instance count
     */
    protected abstract int getMaxInstances();

    /**
     * THIS VALUE SHOULD NOT CHANGE!!!
     * @return the size of data (how many floats) it takes for passing one fx instance to the shader
     */
    protected abstract int getDataSizePerInstance();

    public DynamicEffectInstance getFxInstance(int index) {
        return instances.get(index);
    }

    public int getFxInstanceCount() {
        return instances.size();
    }

    public DataBuffer getDataBuffer() {
        return dataBuffer;
    }

    public List<DynamicEffectInstance> getFxInstances() {
        return instances;
    }

    @Override
    public void init() {
        super.init();
        dataBuffer.generate((long) getMaxInstances() * getDataSizePerInstance());
    }

    /**
     * Add an fx instance
     * @return the instance added or null if the amount of instances has reached max
     */
    @Nullable
    public I addFxInstance(I instance) {
        if (instances.size() >= getMaxInstances()) {
            Veil.LOGGER.warn("Failed to add fx instance to " + this + ": reached max instance count of " + getMaxInstances());
            return null;
        }
        instances.add(instance);
        setActive(true);
        Veil.LOGGER.warn("Added fx instance to " + this + ": " + instance);
        Minecraft.getInstance().player.sendSystemMessage(Component.literal("Added fx instance " + instance.getName()));
        return instance;
    }

    @Override
    public void beforeProcess(PoseStack viewModelStack) {
        for (int i=instances.size()-1; i>=0; i--) {
            DynamicEffectInstance instance = instances.get(i);
            instance.update(MC.getDeltaFrameTime());
            if (instance.isRemoved()) {
                instances.remove(i);
            }
        }

        if (instances.isEmpty()) {
            setActive(false);
            return;
        }

        float[] data = new float[instances.size() * getDataSizePerInstance()];
        for (int ins = 0; ins < instances.size(); ins++) {
            DynamicEffectInstance instance = instances.get(ins);
            int offset = ins * getDataSizePerInstance();
            instance.writeDataToBuffer((index, d) -> {
                if (index >= getDataSizePerInstance() || index < 0)
                    throw new IndexOutOfBoundsException(index);
                data[offset + index] = d;
            });
        }

        dataBuffer.upload(data);
    }

    protected void setDataBufferUniform(EffectInstance effectInstance, String bufferName, String countName) {
        dataBuffer.apply(effectInstance, bufferName);
        effectInstance.safeGetUniform(countName).set(instances.size());
    }
}
