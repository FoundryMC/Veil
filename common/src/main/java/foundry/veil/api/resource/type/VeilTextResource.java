package foundry.veil.api.resource.type;

import foundry.veil.api.resource.VeilResource;
import imgui.extension.texteditor.TextEditorLanguage;
import org.jetbrains.annotations.Nullable;

public interface VeilTextResource<T extends VeilTextResource<?>> extends VeilResource<T> {

    // TODO re-implement
    @Nullable
    default TextEditorLanguage languageDefinition() {
        return null;
    }
}
