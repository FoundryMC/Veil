package foundry.veil.postprocessing;

import net.minecraft.client.renderer.EffectInstance;

import static org.lwjgl.opengl.GL31.*;

public class DataBuffer {
    private int tbo = 0;
    private int tex = 0;

    /**
     * Generate or regenerate TBO and texture
     * @param size the size of the buffer (how many float numbers it can store)
     */
    public void generate(long size) {
        destroy();

        tbo = glGenBuffers();
        glBindBuffer(GL_TEXTURE_BUFFER, tbo);
        glBufferData(GL_TEXTURE_BUFFER, size * 4, GL_STATIC_DRAW);

        tex = glGenTextures();
        glBindTexture(GL_TEXTURE_BUFFER, tex);
        glTexBuffer(GL_TEXTURE_BUFFER, GL_R32F, tbo);

        glBindBuffer(GL_TEXTURE_BUFFER, 0);
        glBindTexture(GL_TEXTURE_BUFFER, 0);
    }

    public void destroy() {
        if (tbo != 0)
            glDeleteBuffers(tbo);
        if (tex != 0)
            glDeleteTextures(tex);
        tbo = 0;
        tex = 0;
    }

    public void upload(float[] data) {
        glBindBuffer(GL_TEXTURE_BUFFER, tbo);
        glBufferSubData(GL_TEXTURE_BUFFER, 0, data);
        glBindBuffer(GL_TEXTURE_BUFFER, 0);
    }

    public void apply(EffectInstance effect, String uniform) {
        glBindBuffer(GL_TEXTURE_BUFFER, tbo);
        int unit = effect.samplerMap.size();
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_BUFFER, tex);

        effect.safeGetUniform(uniform).set(unit);
        glActiveTexture(GL_TEXTURE0);
    }
}
