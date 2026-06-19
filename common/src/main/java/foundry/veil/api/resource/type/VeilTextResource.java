package foundry.veil.api.resource.type;

import foundry.veil.api.resource.VeilResource;
import imgui.extension.texteditor.TextEditorLanguage;
import org.jetbrains.annotations.Nullable;

public interface VeilTextResource<T extends VeilTextResource<?>> extends VeilResource<T> {

    /**
     * @return The text language definition for this resource or <code>null</code> to disable syntax highlighting
     * @since 4.3.0
     */
    @Nullable
    default TextEditorLanguage languageDefinition() {
        return null;
    }
}
