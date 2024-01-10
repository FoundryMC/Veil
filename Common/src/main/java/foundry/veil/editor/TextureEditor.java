package foundry.veil.editor;

import com.mojang.blaze3d.systems.RenderSystem;
import imgui.ImGui;
import imgui.flag.ImGuiDir;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glIsTexture;

public class TextureEditor extends SingleWindowEditor {

    private final IntSet texturesSet;
    private final Map<Integer, OpenTexture> openTextures;
    private final ImBoolean flip;
    private int[] textures;
    private int selectedTexture;

    public TextureEditor() {
        this.texturesSet = new IntArraySet();
        this.openTextures = new HashMap<>();
        this.flip = new ImBoolean();
        this.textures = new int[0];
        this.selectedTexture = 0;
    }

    private void scanTextures() {
        this.texturesSet.clear();
        for (int i = 0; i < 10000; i++) {
            if (!glIsTexture(i)) {
                continue;
            }

            this.texturesSet.add(i);
        }

        if (this.textures.length != this.texturesSet.size()) {
            if (!this.texturesSet.contains(this.selectedTexture)) {
                this.selectedTexture = 0;
            }
            this.textures = this.texturesSet.toIntArray();
            this.openTextures.keySet().removeIf(a -> !this.texturesSet.contains(a.intValue()));
        }
    }

    @Override
    public String getDisplayName() {
        return "Textures";
    }

    @Override
    protected void renderComponents() {
        this.scanTextures();

        int selectedId = this.selectedTexture < 0 || this.selectedTexture >= this.textures.length ? 0 : this.textures[this.selectedTexture];
        int[] value = {this.selectedTexture};

        ImGui.beginDisabled(this.textures.length == 0);
        if (ImGui.sliderInt("##textures", value, 0, this.textures.length - 1, selectedId == 0 ? "No Texture" : Integer.toString(selectedId))) {
            this.selectedTexture = value[0];
        }
        ImGui.endDisabled();
        ImGui.sameLine();

        ImGui.pushButtonRepeat(true);
        ImGui.beginDisabled(this.selectedTexture <= 0);
        if (ImGui.arrowButton("##left", ImGuiDir.Left)) {
            this.selectedTexture--;
        }
        ImGui.endDisabled();
        ImGui.beginDisabled(this.selectedTexture >= this.textures.length - 1);
        ImGui.sameLine(0.0f, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.arrowButton("##right", ImGuiDir.Right)) {
            this.selectedTexture++;
        }
        ImGui.endDisabled();
        ImGui.popButtonRepeat();

        ImGui.beginDisabled(this.openTextures.containsKey(selectedId) && this.openTextures.get(selectedId).visible.get());
        ImGui.sameLine(0.0f, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.button("Pop Out")) {
            this.openTextures.put(selectedId, new OpenTexture(this.flip.get()));
        }
        ImGui.endDisabled();

        ImGui.sameLine(0.0f, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.checkbox("Flip Y", this.flip);

        if (selectedId != 0) {
            addImage(selectedId, this.flip.get());
        }
    }

    @Override
    public void render() {
        super.render();

        Iterator<Map.Entry<Integer, OpenTexture>> iterator = this.openTextures.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, OpenTexture> entry = iterator.next();
            int id = entry.getKey();

            OpenTexture texture = entry.getValue();
            if (!texture.visible.get()) {
                continue;
            }

            ImBoolean open = texture.open;
            if (!open.get()) {
                open.set(true);
                ImGui.setNextWindowSize(800, 600);
            }


            if (ImGui.begin("Texture " + id, open)) {
                ImBoolean flip = texture.flip;
                ImGui.checkbox("Flip Y", flip);
                addImage(id, flip.get());
            }
            ImGui.end();

            if (!open.get()) {
                iterator.remove();
            }
        }
    }

    @Override
    public void renderMenuBar() {
        for (Map.Entry<Integer, OpenTexture> entry : this.openTextures.entrySet()) {
            ImGui.menuItem("Texture " + entry.getKey(), null, entry.getValue().visible);
        }
    }

    @Override
    public boolean isMenuBarEnabled() {
        return !this.openTextures.isEmpty();
    }

    private static void addImage(int selectedId, boolean flip) {
        RenderSystem.bindTexture(selectedId);
        int width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
        float size = ImGui.getContentRegionAvailX();
        ImGui.image(selectedId, size, size * (float) height / (float) width, 0, flip ? 1 : 0, 1, flip ? 0 : 1, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onHide() {
        super.onHide();
        this.texturesSet.clear();
        this.textures = new int[0];
        this.selectedTexture = 0;
    }

    private record OpenTexture(ImBoolean open, ImBoolean visible, ImBoolean flip) {

        private OpenTexture(boolean flip) {
            this(new ImBoolean(), new ImBoolean(true), new ImBoolean(flip));
        }
    }
}
