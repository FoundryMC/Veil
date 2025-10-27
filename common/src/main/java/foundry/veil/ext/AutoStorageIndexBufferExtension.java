package foundry.veil.ext;

public interface AutoStorageIndexBufferExtension {

    void veil$ensureStorage(int neededIndexCount);

    int veil$getBuffer();
}
