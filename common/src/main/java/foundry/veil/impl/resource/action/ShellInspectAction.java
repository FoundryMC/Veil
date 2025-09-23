package foundry.veil.impl.resource.action;

import foundry.veil.api.resource.VeilEditorEnvironment;
import foundry.veil.api.resource.VeilResourceAction;
import net.minecraft.network.chat.Component;
import foundry.veil.api.client.registry.VeilResourceEditorRegistry;
import foundry.veil.api.resource.type.ShellResource;

import java.util.OptionalInt;

public record ShellInspectAction<T extends ShellResource>() implements VeilResourceAction<T> {

    private static final Component NAME = Component.translatable("resource.veil.action.shell_edit");
    private static final Component DESC = Component.translatable("resource.veil.action.shell_edit.desc");

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
        environment.open(resource, VeilResourceEditorRegistry.SHELL.get());
    }
}
