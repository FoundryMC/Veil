package foundry.veil.api.flare.model;

import foundry.veil.api.flare.data.model.*;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import foundry.veil.Veil;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.resources.model.ModelBakery.MODEL_LISTER;
import static foundry.veil.Veil.LOGGER;

public class ShellBakery {
    public static final ResourceLocation MISSING_SHELL_LOCATION = Veil.veilPath("builtin/missing");
    public static final ModelResourceLocation MISSING_SHELL_VARIANT = new ModelResourceLocation(MISSING_SHELL_LOCATION, "shell");
    private final Map<ResourceLocation, ? extends UnbakedShell> shellResources;
    private final Map<ResourceLocation, UnbakedShell> unbakedShellCache = new HashMap<>();
    private final Map<ResourceLocation, BakedShell> bakedShellCache = new HashMap<>();
    private final Map<ModelResourceLocation, UnbakedShell> topLevelShells = new HashMap<>();
    private final Map<ModelResourceLocation, BakedShell> bakedTopLevelShells = new HashMap<>();
    private final UnbakedShell missingShell;

    public ShellBakery(Map<ResourceLocation, ? extends UnbakedShell> shellResources, List<ResourceLocation> shellsToLoad, ProfilerFiller profiler) {
        this.shellResources = shellResources;
        profiler.push("missing_shell");
        try {
            this.missingShell = loadShell(MISSING_SHELL_LOCATION);
            registerShell(MISSING_SHELL_VARIANT, missingShell);
        } catch (IOException e) {
            LOGGER.error("Error loading missing shell, should never happen :(", e);
            throw new RuntimeException(e);
        }
        profiler.popPush("shells");
        for (ResourceLocation resourceLocation : shellsToLoad) {
            loadAndRegisterShell(resourceLocation);
        }
        for (Map.Entry<ResourceLocation, ? extends UnbakedShell> entry : shellResources.entrySet()) {
            registerShell(shell(entry.getKey()), entry.getValue());
        }
        profiler.pop();
    }

    public void bakeShells() {
        this.topLevelShells.forEach((modelLocation, unbakedShell) -> {
            BakedShell bakedShell = null;

            try {
                bakedShell = new ShellBakerImpl().bakeUncached(unbakedShell);
            } catch (Exception exception) {
                LOGGER.warn("Unable to bake shell: '{}': {}", modelLocation, exception);
            }

            if (bakedShell != null) {
                bakedTopLevelShells.put(modelLocation, bakedShell);
            }
        });
    }

    public UnbakedShell getShell(ResourceLocation shellLocation) {
        if (unbakedShellCache.containsKey(shellLocation)) return unbakedShellCache.get(shellLocation);
        try {
            if (!unbakedShellCache.containsKey(shellLocation)) {
                UnbakedShell unbakedShell = loadShell(shellLocation);
                unbakedShellCache.put(shellLocation, unbakedShell);
            }
        } catch (Exception exception) {
            LOGGER.warn("Unable to load shell: '{}': {}", shellLocation, exception);
            unbakedShellCache.put(shellLocation, missingShell);
        }

        return unbakedShellCache.getOrDefault(shellLocation, missingShell);
    }

    private void loadAndRegisterShell(ResourceLocation shellLocation) {
        ModelResourceLocation modelResourceLocation = shell(shellLocation);
        ResourceLocation resourceLocation = shellLocation.withPath("shell/");
        UnbakedShell unbakedShell = getShell(resourceLocation);
        registerShell(modelResourceLocation, unbakedShell);
    }

    private UnbakedShell loadShell(ResourceLocation location) throws IOException {
        ResourceLocation resourcelocation = MODEL_LISTER.idToFile(location);
        UnbakedShell unbakedShell = shellResources.get(resourcelocation);
        if (unbakedShell == null) {
            return null;
//            throw new RuntimeException(new FileNotFoundException(resourcelocation.toString()));
        } else {
            unbakedShell.setLocation(location);
            return unbakedShell;
        }
    }

    private void registerShell(ModelResourceLocation modelLocation, UnbakedShell unbakedShell) {
        topLevelShells.put(modelLocation, unbakedShell);
    }

    public static ModelResourceLocation shell(ResourceLocation modelLocation) {
        return new ModelResourceLocation(modelLocation, "shell");
    }

    public Map<ModelResourceLocation, BakedShell> getBakedTopLevelShells() {
        return bakedTopLevelShells;
    }

    public BakedShell getBakedShell(ResourceLocation modelLocation) {
        ModelResourceLocation modelResourceLocation = shell(modelLocation);
        BakedShell bakedShell = bakedTopLevelShells.get(modelResourceLocation);
        if (bakedShell == null) bakedShell = new ShellBakerImpl().bake(modelLocation);
        return bakedShell;
    }

    class ShellBakerImpl implements ShellBaker {

        ShellBakerImpl() {
        }

        @Override
        public UnbakedShell getShell(ResourceLocation location) {
            return ShellBakery.this.getShell(location);
        }

        @Nullable
        @Override
        public UnbakedShell getTopLevelShell(ModelResourceLocation location) {
            return topLevelShells.get(location);
        }

        @Override
        public BakedShell bake(ResourceLocation location) {
            BakedShell bakedShell = bakedShellCache.get(location);
            if (bakedShell == null) {
                UnbakedShell unbakedShell = getShell(location);
                bakedShell = bakeUncached(unbakedShell);
                bakedShellCache.put(location, bakedShell);
            }
            return bakedShell;
        }

        @Nullable
        @Override
        public BakedShell bakeUncached(UnbakedShell unbakedShell) {
            return unbakedShell.bake();
        }
    }

    public static class FaceBakery {
        public static BakedShell bake(UnbakedShell unbakedShell) {
            SimpleBakedShell.Builder builder = new SimpleBakedShell.Builder();
            for (ShellElement element : unbakedShell.getElements()) {
                for (Direction direction : element.faces().keySet()) {
                    builder.addFace(bakeFace(element, element.faces().get(direction), direction));
                }
            }

            return builder.build();
        }

        public static FlareBakedQuad bakeFace(ShellElement element, ShellElementFace face, Direction direction) {
            return bakeQuad(element.from(), element.to(), face, direction, element.rotation());
        }

        public static FlareBakedQuad bakeQuad(Vector3f from, Vector3f to, ShellElementFace face, Direction facing, ShellElementRotation rotation) {
            Vector3f normal = facing.step();
            float[] vertexData = makeVertices(face.uv(), normal, facing, setupShape(from, to), rotation);

            return new FlareBakedQuad(vertexData, normal);

        }

        private static float[] makeVertices(ShellFaceUV uvs, Vector3f normal, Direction direction, float[] posDiv16, @Nullable ShellElementRotation rotation) {
            float[] vertexData = new float[20];
            Matrix4f rotationMatrix = null;
            if (rotation != null) {
                Vector3f axis = getAxis(rotation.axis());
                rotationMatrix = new Matrix4f().rotation(rotation.angle() * Mth.DEG_TO_RAD, axis);
                rotateNormalBy(normal, rotationMatrix);
            }
            for(int i = 0; i < 4; i++) {
                bakeVertex(vertexData, i, direction, uvs, posDiv16, rotation, rotationMatrix);
            }

            return vertexData;
        }

        private static @NotNull Vector3f getAxis(Direction.Axis axis) {
            return switch (axis) {
                case X -> new Vector3f(1.0F, 0.0F, 0.0F);
                case Y -> new Vector3f(0.0F, 1.0F, 0.0F);
                case Z -> new Vector3f(0.0F, 0.0F, 1.0F);
            };
        }

        private static void bakeVertex(float[] vertexData, int vertexIndex, Direction direction, ShellFaceUV shellFaceUV, float[] posDiv16, @Nullable ShellElementRotation rotation, @Nullable Matrix4f rotationMatrix) {
            FaceInfo.VertexInfo vertexInfo = FaceInfo.fromFacing(direction).getVertexInfo(vertexIndex);
            Vector3f actualPos = new Vector3f(posDiv16[vertexInfo.xFace], posDiv16[vertexInfo.yFace], posDiv16[vertexInfo.zFace]);
            applyVertexTransform(actualPos, rotation, rotationMatrix);
            fillVertex(vertexData, vertexIndex, actualPos, shellFaceUV);
        }

        private static float[] setupShape(Vector3f min, Vector3f max) {
            float[] vertexPosition = new float[Direction.values().length];
            //center the center
            vertexPosition[FaceInfo.Constants.MIN_X] = min.x() / 16.0F - 0.5f;
            vertexPosition[FaceInfo.Constants.MIN_Y] = min.y() / 16.0F;
            vertexPosition[FaceInfo.Constants.MIN_Z] = min.z() / 16.0F - 0.5f;
            vertexPosition[FaceInfo.Constants.MAX_X] = max.x() / 16.0F - 0.5f;
            vertexPosition[FaceInfo.Constants.MAX_Y] = max.y() / 16.0F;
            vertexPosition[FaceInfo.Constants.MAX_Z] = max.z() / 16.0F - 0.5f;
            return vertexPosition;
        }

        private static void fillVertex(float[] vertexData, int vertexIndex, Vector3f pos, ShellFaceUV shellFaceUV) {
            int i = vertexIndex * 5;
            vertexData[i] = pos.x();
            vertexData[i + 1] = pos.y();
            vertexData[i + 2] = pos.z();
            vertexData[i + 3] = shellFaceUV.getU(vertexIndex) / 16.0F;
            vertexData[i + 4] = shellFaceUV.getV(vertexIndex) / 16.0F;
        }

        private static void applyVertexTransform(Vector3f pos, @Nullable ShellElementRotation rotation, @Nullable Matrix4f rotationMatrix) {
            if (rotation == null || rotationMatrix == null) return;

            Vector3f scale = switch (rotation.axis()) {
                case X -> new Vector3f(0.0F, 1.0F, 1.0F);
                case Y -> new Vector3f(1.0F, 0.0F, 1.0F);
                case Z -> new Vector3f(1.0F, 1.0F, 0.0F);
            };


            transformVertexBy(pos, new Vector3f(rotation.origin()).div(16.0F).sub(0.5f, 0.0f, 0.5f), rotationMatrix, scale);
        }

        private static void transformVertexBy(Vector3f pos, Vector3f origin, Matrix4f transform, Vector3f scale) {
            Vector3f offset = transform.transformPosition(new Vector3f(pos.x() - origin.x(), pos.y() - origin.y(), pos.z() - origin.z()));
//            Vector3f offset = new Vector3f(pos.x() - origin.x(), pos.y() - origin.y(), pos.z() - origin.z());
//            offset.mul(scale, new Vector3f());
            pos.set(offset.x() + origin.x(), offset.y() + origin.y(), offset.z() + origin.z());
        }

        private static void rotateNormalBy(Vector3f normal, Matrix4f transform) {
            transform.transformDirection(normal);
        }
    }
}
