package foundry.veil.impl.resource.action;

import foundry.veil.api.client.registry.VeilResourceEditorRegistry;
import foundry.veil.api.resource.VeilEditorEnvironment;
import foundry.veil.api.resource.VeilResourceAction;
import foundry.veil.api.resource.type.FlareResource;
import net.minecraft.network.chat.Component;

import java.util.OptionalInt;

public record TemplateInspectAction<T extends FlareResource>() implements VeilResourceAction<T> {

    private static final Component NAME = Component.translatable("resource.veil.action.effect_edit");
    private static final Component DESC = Component.translatable("resource.veil.action.effect_edit.desc");

    @Override
    public Component getName() {
        return NAME;
    }

    @Override
    public Component getDescription() {
        return DESC;
    }

    @Override
    public OptionalInt getIcon() {
        return OptionalInt.of(0xECDB); // Edit file line icon
    }

    @Override
    public void perform(VeilEditorEnvironment environment, T resource) {
        environment.open(resource, VeilResourceEditorRegistry.EFFECT.get());
    }
}
