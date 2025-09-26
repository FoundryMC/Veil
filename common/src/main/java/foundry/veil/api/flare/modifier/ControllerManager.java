package foundry.veil.api.flare.modifier;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import foundry.veil.api.flare.EffectHost;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ControllerManager {
    private final Table<String, String, Controller> controllers = HashBasedTable.create();
    private final Map<String, Controller> globalControllers = new HashMap<>();

    public void addController(Controller controller) {
        String name = controller.getIdentifier().name();
        String invoker = controller.getIdentifier().host();
        if (name.startsWith("global::")) globalControllers.put(name, controller);
        else controllers.put(name, invoker, controller);
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

    public void updateAllControllers(float partialTick) {
        for (Controller controller : controllers.values()) {
            if (controller == null) continue;
            controller.update(partialTick);
        }
    }
}
