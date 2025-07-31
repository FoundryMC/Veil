package foundry.veil.api.resource.type;

import foundry.veil.api.client.imgui.VeilLanguageDefinitions;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public interface VeilShaderResource<T extends VeilShaderResource<?>> extends VeilTextResource<T> {

    @Override
    default int getIconCode() {
        return 0xECD1; // Code file icon
    }

    @Override
    default @Nullable TextEditorLanguageDefinition languageDefinition() {
        return VeilLanguageDefinitions.glsl();
    }
}
