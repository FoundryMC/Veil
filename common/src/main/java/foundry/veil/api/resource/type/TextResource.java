package foundry.veil.api.resource.type;

import foundry.veil.Veil;
import foundry.veil.api.resource.VeilResourceAction;
import foundry.veil.api.resource.VeilResourceInfo;
import foundry.veil.api.resource.VeilResourceManager;
import foundry.veil.impl.resource.action.TextEditAction;
import imgui.extension.texteditor.TextEditorLanguage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Internal
public record TextResource(
        VeilResourceInfo resourceInfo,
        Type type,
        @Nullable TextEditorLanguage languageDefinition
) implements VeilTextResource<TextResource> {

    @Override
    public List<VeilResourceAction<TextResource>> getActions() {
        return List.of(new TextEditAction<>());
    }

    @Override
    public boolean canHotReload() {
        return false;
    }

    @Override
    public void hotReload(VeilResourceManager resourceManager) throws IOException {
    }

    @Override
    public int getIconCode() {
        return this.type.getIcon();
    }

    public enum Type {
        TEXT(".txt", 0xED0F),
        JSON(".json", 0xECCD, TextEditorLanguage::Json);

        private final String extension;
        private final int icon;
        private final Supplier<TextEditorLanguage> languageDefinition;

        Type(String extension, int icon, @Nullable Supplier<TextEditorLanguage> languageDefinition) {
            this.extension = extension;
            this.icon = icon;
            this.languageDefinition = languageDefinition;
        }

        Type(String extension, int icon) {
            this(extension, icon, null);
        }

        public String getExtension() {
            return this.extension;
        }

        public int getIcon() {
            return this.icon;
        }

        public @Nullable TextEditorLanguage getLanguageDefinition() {
            return Veil.IMGUIMC && this.languageDefinition != null ? this.languageDefinition.get() : null;
        }
    }
}
