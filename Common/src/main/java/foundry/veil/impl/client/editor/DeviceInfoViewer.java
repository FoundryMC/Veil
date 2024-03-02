package foundry.veil.impl.client.editor;

import foundry.veil.api.client.editor.SingleWindowEditor;
import foundry.veil.api.opencl.VeilOpenCL;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.GL_MAX_COMBINED_UNIFORM_BLOCKS;
import static org.lwjgl.opengl.GL31C.GL_MAX_UNIFORM_BUFFER_BINDINGS;
import static org.lwjgl.opengl.GL40C.GL_MAX_TRANSFORM_FEEDBACK_BUFFERS;
import static org.lwjgl.opengl.GL42C.*;
import static org.lwjgl.opengl.GL43C.*;

@ApiStatus.Internal
public class DeviceInfoViewer extends SingleWindowEditor {

    private void renderOpenGL() {
        ImGui.text("Vendor: " + glGetString(GL_VENDOR));
        ImGui.text("Renderer: " + glGetString(GL_RENDERER));
        ImGui.text("Version: " + glGetString(GL_VERSION));
        ImGui.separator();

        GLCapabilities caps = GL.getCapabilities();
        text("Max Uniform Buffer Bindings:", "" + glGetInteger(GL_MAX_UNIFORM_BUFFER_BINDINGS), "The limit on the number of uniform buffer binding points. This is the limit for glBindBufferRange when using GL_UNIFORM_BUFFER.");
        text("Max Combined Uniform Blocks:", "" + glGetInteger(GL_MAX_COMBINED_UNIFORM_BLOCKS), "The maximum number of uniform blocks that all of the active programs can use. If two (or more) shader stages use the same block, they count separately towards this limit.");
        text("Max Combined Texture Image Units:", "" + glGetInteger(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS), "The total number of texture units that can be used from all active programs. This is the limit on glActiveTexture(GL_TEXTURE0 + i) and glBindSampler.");
        text("Max Transform Feedback Separate Attributes:", "" + glGetInteger(GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS), "When doing separate mode Transform Feedback, this is the maximum number of varying variables that can be captured.");
        text("Max Transform Feedback Separate Components:", "" + glGetInteger(GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS), "When doing separate mode Transform Feedback, this is the maximum number of components for a single varying variable (note that varyings can be arrays or structs) that can be captured.");
        text("Max Transform Feedback Interleaved Components:", "" + glGetInteger(GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS), "When doing interleaved Transform Feedback, this is the total number of components that can be captured within a single buffer.");

        text("Max Transform Feedback Buffers:", caps.OpenGL40 || caps.GL_ARB_transform_feedback3 ? "" + glGetInteger(GL_MAX_TRANSFORM_FEEDBACK_BUFFERS) : null, "The maximum number of buffers that can be written to in transform feedback operations.");

        boolean atomicCounters = caps.OpenGL42 || caps.GL_ARB_shader_atomic_counters;
        text("Max Atomic Counter Buffer Bindings:", atomicCounters ? "" + glGetInteger(GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS) : null, "The total number of atomic counter buffer binding points. This is the limit for glBindBufferRange when using GL_ATOMIC_COUNTER_BUFFER.");
        text("Max Combined Atomic Counter Buffers:", atomicCounters ? "" + glGetInteger(GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS) : null, "The maximum number of atomic counter buffers variables across all active programs.");
        text("Max Combined Atomic Counters:", atomicCounters ? "" + glGetInteger(GL_MAX_COMBINED_ATOMIC_COUNTERS) : null, "The maximum number of atomic counter variables across all active programs.");
        text("Max Shader Storage Buffer Bindings:", atomicCounters ? "" + glGetInteger(GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS) : null, "The total number of shader storage buffer binding points. This is the limit for glBindBufferRange when using GL_SHADER_STORAGE_BUFFER.");

        boolean shaderStorageBuffers = caps.OpenGL43 || caps.GL_ARB_shader_storage_buffer_object;
        text("Max Combined Shader Storage Blocks:", shaderStorageBuffers ? "" + glGetInteger(GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS) : null, "The maximum number of shader storage blocks across all active programs. As with UBOs, blocks that are the same between stages are counted for each stage.");
        text("Max Image Units:", caps.OpenGL42 || caps.GL_ARB_shader_image_load_store ? "" + glGetInteger(GL_MAX_IMAGE_UNITS) : null, "The total number of image units that can be used for image variables from all active programs. This is the limit on glBindImageTexture.");
        text("Max Combined Shader Output Resources:", shaderStorageBuffers ? "" + glGetInteger(GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES) : null, "The total number of shader storage blocks, image variables, and fragment shader outputs across all active programs cannot exceed this number. This is the \"amount of stuff\" that a sequence of shaders can write to (barring Transform Feedback).");

        text("Max Framebuffer Color Attachments:", "" + glGetInteger(GL_MAX_COLOR_ATTACHMENTS), null);
    }

    private static void tooltip(String text) {
        ImGui.textDisabled("(?)");
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.pushTextWrapPos(ImGui.getFontSize() * 35.0f);
            ImGui.textUnformatted(text);
            ImGui.popTextWrapPos();
            ImGui.endTooltip();
        }
    }

    private static void text(String text, @Nullable String value, @Nullable String tooltip) {
        if (value != null) {
            ImGui.text(text + " " + value);
        } else {
            ImGui.textDisabled(text + " Unsupported");
        }
        if (tooltip != null) {
            ImGui.sameLine();
            tooltip(tooltip);
        }
    }

    private void renderOpenAL() {
        ImGui.text("Vendor: " + alGetString(AL_VENDOR));
        ImGui.text("Renderer: " + alGetString(AL_RENDERER));
        ImGui.text("Version: " + alGetString(AL_VERSION));
    }

    private void renderOpenCL() {
        VeilOpenCL cl = VeilOpenCL.get();
        VeilOpenCL.PlatformInfo[] platforms = cl.getPlatforms();

        ImGui.text("Platforms");
        for (int i = 0; i < platforms.length; i++) {
            VeilOpenCL.PlatformInfo platform = platforms[i];
            if (!ImGui.collapsingHeader(platform.name() + " (0x%X)".formatted(platform.id()), i == 0 ? ImGuiTreeNodeFlags.DefaultOpen : 0)) {
                continue;
            }

            ImGui.text("Profile: " + platform.profile());
            ImGui.text("CL Version: " + platform.version());
            ImGui.text("Vendor: " + platform.vendor());

            ImGui.separator();

            VeilOpenCL.DeviceInfo[] devices = platform.devices();
            ImGui.text("Devices");
            for (int j = 0; j < devices.length; j++) {
                VeilOpenCL.DeviceInfo device = devices[i];
                if (!ImGui.collapsingHeader(device.name() + " (0x%X)".formatted(device.id()), i == 0 ? ImGuiTreeNodeFlags.DefaultOpen : 0)) {
                    continue;
                }

                ImGui.indent();
                List<String> types = new ArrayList<>(1);
                if (device.isDefault()) {
                    types.add("Default");
                }
                if (device.isCpu()) {
                    types.add("CPU");
                }
                if (device.isGpu()) {
                    types.add("GPU");
                }
                if (device.isAccelerator()) {
                    types.add("Accelerator");
                }
                ImGui.text("Type: " + types.stream().collect(Collectors.joining(", ")));
                ImGui.text("Vendor ID: 0x%X".formatted(device.vendorId()));
                ImGui.text("Max Compute Units: " + device.maxComputeUnits());
                ImGui.text("Max Work Item Dimensions: " + device.maxWorkItemDimensions());
                ImGui.text("Max Work Group Size: " + device.maxWorkGroupSize());
                ImGui.text("Max Clock Frequency: " + device.maxClockFrequency() + " MHz");
                ImGui.text("Address Size: " + device.addressBits() + " bits");
                ImGui.text("Available: " + device.available());
                ImGui.text("Compiler Available: " + device.compilerAvailable());
                ImGui.text("Requires Manual CL/GL Sync: " + device.requireManualInteropSync());
                ImGui.text("Vendor: " + device.vendor());
                ImGui.text("Version: " + device.version());
                ImGui.text("Driver Version: " + device.driverVersion());
                ImGui.text("Profile: " + device.profile());
                String cVersion = device.openclCVersion();
                if (cVersion != null) {
                    ImGui.text("OpenCL C Version: " + cVersion);
                }
                ImGui.unindent();
            }
        }
    }

    @Override
    public String getDisplayName() {
        return "Device Info";
    }

    @Override
    protected void renderComponents() {
        if (ImGui.beginTabBar("##info")) {
            if (ImGui.beginTabItem("OpenCL")) {
                this.renderOpenCL();
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("OpenGL")) {
                this.renderOpenGL();
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("OpenAL")) {
                this.renderOpenAL();
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }
    }

    @Override
    public void render() {
        ImGui.setNextWindowSizeConstraints(400, 460, Float.MAX_VALUE, Float.MAX_VALUE);
        super.render();
    }
}
