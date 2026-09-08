package foundry.veil.api.client.render.ext;

import foundry.veil.Veil;
import foundry.veil.api.compat.ImmersivePortalsCompat;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.opengl.KHRDebug.*;

/**
 * Provides access to debug functionality for all platforms.
 *
 * @author Ocelot
 */
public enum VeilDebug {
    DISABLED {
        @Override
        public void debugMessageInsert(int type, int id, int severity, CharSequence message) {
        }

        @Override
        public void objectLabel(int identifier, int name, CharSequence label) {
        }

        @Override
        public void pushDebugGroup(CharSequence message) {
        }

        @Override
        public void popDebugGroup() {
        }
    },
    ENABLED {
        @Override
        public void debugMessageInsert(int type, int id, int severity, CharSequence message) {
            glDebugMessageInsert(GL_DEBUG_SOURCE_APPLICATION, type, id, severity, message);
        }

        @Override
        public void objectLabel(int identifier, int name, @Nullable CharSequence label) {
            if (label != null) {
                if (!ImmersivePortalsCompat.isLoaded()) {
                    glObjectLabel(identifier, name, label);
                }
            } else {
                nglObjectLabel(identifier, name, 0, 0L);
            }
        }

        @Override
        public void pushDebugGroup(CharSequence message) {
            glPushDebugGroup(GL_DEBUG_SOURCE_APPLICATION, 10000 + MESSAGE_ID.incrementAndGet() * 100, message);
        }

        @Override
        public void popDebugGroup() {
            glPopDebugGroup();
        }
    };

    @ApiStatus.Internal
    public static final AtomicInteger MESSAGE_ID = new AtomicInteger();

    private static VeilDebug debug;

    /**
     * This function can be called by applications and third-party libraries to generate their own messages, such as ones containing timestamp information or signals about specific render system events.
     *
     * @param type     the type of the debug message insert
     * @param id       the user-supplied identifier of the message to insert
     * @param severity the severity of the debug messages to insert
     * @param message  a character array containing the message to insert
     */
    public abstract void debugMessageInsert(int type, int id, int severity, CharSequence message);

    /**
     * Labels a named object identified within a namespace.
     *
     * @param identifier the namespace from which the name of the object is allocated
     * @param name       the name of the object to label
     * @param label      a string containing the label to assign to the object
     */
    public abstract void objectLabel(int identifier, int name, @Nullable CharSequence label);

    /**
     * Pushes a debug group described by the string {@code message} into the command stream. The value of {@code id} specifies the ID of messages generated.
     * The parameter {@code length} contains the number of characters in {@code message}. If {@code length} is negative, it is implied that {@code message}
     * contains a null terminated string. The message has the specified {@code source} and {@code id}, {@code type} {@link KHRDebug#GL_DEBUG_TYPE_PUSH_GROUP DEBUG_TYPE_PUSH_GROUP}, and
     * {@code severity} {@link KHRDebug#GL_DEBUG_SEVERITY_NOTIFICATION DEBUG_SEVERITY_NOTIFICATION}. The GL will put a new debug group on top of the debug group stack which inherits the control of the
     * volume of debug output of the debug group previously residing on the top of the debug group stack. Because debug groups are strictly hierarchical, any
     * additional control of the debug output volume will only apply within the active debug group and the debug groups pushed on top of the active debug group.
     *
     * @param message a string containing the message to be sent to the debug output stream
     */
    public abstract void pushDebugGroup(CharSequence message);

    /**
     * Pops the active debug group. When a debug group is popped, the GL will also generate a debug output message describing its cause based on the
     * {@code message} string, the source {@code source}, and an ID {@code id} submitted to the associated {@link KHRDebug#glPushDebugGroup PushDebugGroup} command. {@link KHRDebug#GL_DEBUG_TYPE_PUSH_GROUP DEBUG_TYPE_PUSH_GROUP}
     * and {@link KHRDebug#GL_DEBUG_TYPE_POP_GROUP DEBUG_TYPE_POP_GROUP} share a single namespace for message {@code id}. {@code severity} has the value {@link KHRDebug#GL_DEBUG_SEVERITY_NOTIFICATION DEBUG_SEVERITY_NOTIFICATION}. The {@code type}
     * has the value {@link KHRDebug#GL_DEBUG_TYPE_POP_GROUP DEBUG_TYPE_POP_GROUP}. Popping a debug group restores the debug output volume control of the parent debug group.
     *
     * <p>Attempting to pop the default debug group off the stack generates a {@link GL11#GL_STACK_UNDERFLOW STACK_UNDERFLOW} error; pushing a debug group onto a stack containing
     * {@link KHRDebug#GL_MAX_DEBUG_GROUP_STACK_DEPTH MAX_DEBUG_GROUP_STACK_DEPTH} minus one elements will generate a {@link GL11#GL_STACK_OVERFLOW STACK_OVERFLOW} error.</p>
     *
     * @see <a href="https://docs.gl/gl4/glPopDebugGroup">Reference Page</a>
     */
    public abstract void popDebugGroup();

    /**
     * @return The best implementation of GL debug for this platform
     */
    public static VeilDebug get() {
        if (debug == null) {
            GLCapabilities caps = GL.getCapabilities();
            if (caps.OpenGL43 || caps.GL_KHR_debug) {
                debug = ENABLED;
                Veil.LOGGER.info("GL Debug supported");
            } else {
                debug = DISABLED;
                Veil.LOGGER.info("GL Debug unsupported");
            }
        }
        return debug;
    }
}
