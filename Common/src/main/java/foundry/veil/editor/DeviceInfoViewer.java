package foundry.veil.editor;

import foundry.veil.opencl.VeilOpenCL;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.opengl.GL11C.*;

/**
 * Displays information about the current device and platform.
 *
 * @author Ocelot
 */
public class DeviceInfoViewer extends SingleWindowEditor {

    private void renderOpenGL() {
        ImGui.text("Vendor: " + glGetString(GL_VENDOR));
        ImGui.text("Renderer: " + glGetString(GL_RENDERER));
        ImGui.text("Version: " + glGetString(GL_VERSION));
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

                String type = device.isCpu() ? "CPU" : device.isGpu() ? "GPU" : device.isAccelerator() ? "Accelerator" : "Custom";
                ImGui.text("Type: " + type);
                ImGui.text("Vendor ID: 0x%X".formatted(device.vendorId()));
                ImGui.text("Max Compute Units: " + device.maxComputeUnits());
                ImGui.text("Max Work Item Dimensions: " + device.maxWorkItemDimensions());
                ImGui.text("Max Work Group Size: " + device.maxWorkGroupSize());
                ImGui.text("Max Clock Frequency: " + device.maxClockFrequency() + " MHz");
                ImGui.text("Address Size: " + device.addressBits() + " bits");
                ImGui.text("Available: " + device.available());
                ImGui.text("Compiler Available: " + device.compilerAvailable());
                ImGui.separator();
                ImGui.text("Vendor: " + device.vendor());
                ImGui.text("Version: " + device.version());
                ImGui.text("Driver Version: " + device.driverVersion());
                ImGui.text("Profile: " + device.profile());
                String cVersion = device.openclCVersion();
                if (cVersion != null) {
                    ImGui.text("OpenCL C Version: " + cVersion);
                }
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
