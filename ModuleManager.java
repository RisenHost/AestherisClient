import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages all registered modules, providing access and lifecycle control.
 */
public final class ModuleManager {
    
    // This is the "Singleton" fix so other files can find your hacks!
    private static final ModuleManager INSTANCE = new ModuleManager();

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    private final List<Module> modules = new ArrayList<>();

    public void register(Module module) {
        if (getModule(module.getName()).isPresent()) {
            throw new IllegalStateException("Module with name '" + module.getName() + "' is already registered.");
        }
        modules.add(module);
    }

    public void init() {
        // We will add your Combat hacks here in the next step!
        register(new Fly());
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public Optional<Module> getModule(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> Optional<T> getModule(Class<T> clazz) {
        return modules.stream()
                .filter(clazz::isInstance)
                .map(m -> (T) m)
                .findFirst();
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .toList();
    }

    public void disableAll() {
        modules.forEach(m -> m.setEnabled(false));
    }
}
