package foundry.veil.api.flare.modifier;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import foundry.veil.VeilClient;
import foundry.veil.api.flare.EffectHost;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ControllerManager {
    /**
     * <p>A table to store non-global controllers.</p>
     * <p>Rows - Controllers</p>
     * <p>Columns - Hosts</p>
     */
    private final Table<String, String, Controller> controllers = HashBasedTable.create();
    private final Map<String, GlobalController> globalControllers = new HashMap<>();

    public ControllerManager() {
        VeilClient.clientPlatform().onRegisterGlobalControllers(this::addGlobalController);
    }

    public void addController(Controller controller) {
        String name = controller.getIdentifier().name();
        String host = controller.getIdentifier().host();
        if (name.startsWith("global::") || controller instanceof GlobalController) throw new IllegalArgumentException("Global controllers should be ");
        else controllers.put(name, host, controller);
    }

    private void addGlobalController(GlobalController globalController) {
        String name = globalController.getIdentifier().name();
        globalControllers.put(name, globalController);
    }

    public @Nullable Controller getController(String name, String host) {
        return name.startsWith("global::") ?
                globalControllers.get(name) :
                controllers.get(name, host);
    }

    public Controller getOrCreateController(String name, EffectHost host) {
        Controller controller = getController(name, host.getName());
        if (controller == null) {
            controller = new Controller(name, host);
            controller.initialize();
            addController(controller);
        }
        return controller;
    }

    public void removeHost(String host) {
        controllers.columnKeySet().remove(host);
    }
}
