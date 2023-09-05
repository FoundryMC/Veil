package foundry.veil.postprocessing;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import foundry.veil.Veil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PreProcessor extends GlslPreprocessor {
    @Override
    public String applyImport(boolean pUseFullPath, String pDirectory) {
        Veil.LOGGER.debug("Loading moj_import in EffectProgram: " + pDirectory);

        ResourceLocation modLoc = new ResourceLocation(pDirectory);
        ResourceLocation shaderLoc = new ResourceLocation(modLoc.getNamespace(), "shaders/include/" + modLoc.getPath() + ".glsl");

        try {
            Resource shaderResource = Minecraft.getInstance().getResourceManager().getResource(shaderLoc).get();
            InputStream shaderStream = shaderResource.open();

            String s2;
            try {
                s2 = IOUtils.toString(shaderStream, StandardCharsets.UTF_8);
            } catch (Throwable e) {
                if (shaderStream != null) {
                    try {
                        shaderStream.close();
                    } catch (Throwable er) {
                        e.addSuppressed(er);
                    }
                }

                throw e;
            }

            if (shaderStream != null) {
                shaderStream.close();
            }

            return s2;
        } catch (IOException e) {
            Veil.LOGGER.error("Could not open GLSL import {}: {}", pDirectory, e.getMessage());
            return "#error " + e.getMessage();
        }
    }
}
