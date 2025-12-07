package foundry.veil.api.flare.modifier;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import foundry.veil.VeilClient;
import foundry.veil.api.flare.EffectHost;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @since 2.5.0
 */
public class ControllerManager {

    /**
     * <p>A table to store non-global controllers.</p>
     * <p>Rows - Controllers</p>
     * <p>Columns - Hosts</p>
     */
    private final Table<String, String, HostBoundController> controllers = Tables.newCustomTable(new WeakHashMap<>(), WeakHashMap::new);
    private final Map<String, GlobalController> globalControllers = new HashMap<>();

    public ControllerManager() {
        VeilClient.clientPlatform().onRegisterGlobalControllers(this::addGlobalController);
    }

    public void addController(Controller controller) {
        String name = controller.getName();
        if (name.startsWith("global::") || controller instanceof GlobalController) {
            throw new IllegalArgumentException("Global controllers should be ");
        } else {
            HostBoundController hostBound = ((HostBoundController) controller);
            String host = hostBound.getHost();
            this.controllers.put(name, host, hostBound);
        }
    }

    private void addGlobalController(GlobalController globalController) {
        String name = globalController.getName();
        this.globalControllers.put(name, globalController);
    }

    public @Nullable Controller getController(String name, String host) {
        return name.startsWith("global::") ?
                this.globalControllers.get(name) :
                this.controllers.get(name, host);
    }

    public Controller getOrCreateController(String name, EffectHost host) {
        Controller controller = this.getController(name, host.getName());
        if (controller == null) {
            controller = new HostBoundController(name, host);
            controller.initialize();
            this.addController(controller);
        }
        return controller;
    }

    public void removeHost(String host) {
        this.controllers.columnKeySet().remove(host);
    }
}
