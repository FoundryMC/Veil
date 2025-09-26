package foundry.veil.platform;

import foundry.veil.api.event.*;
import org.jetbrains.annotations.ApiStatus;

/**
 * Manages client platform-specific features.
 */
@ApiStatus.Internal
public interface VeilClientPlatform extends
        VeilPostProcessingEvent.Pre,
        VeilPostProcessingEvent.Post,
        VeilAddShaderPreProcessorsEvent,
        VeilShaderCompileEvent,
        VeilDynamicBuffersChangedEvent,
        VeilRegisterGlobalControllersEvent
{
}
