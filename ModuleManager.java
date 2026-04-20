package com.aetheris.client.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages all registered modules, providing access and lifecycle control.
 */
public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    /**
     * Registers a module with the manager.
     * @param module The module instance to register.
     * @throws IllegalStateException if a module with the same name is already registered.
     */
    public void register(Module module) {
        if (getModule(module.getName()).isPresent()) {
            throw new IllegalStateException("Module with name '" + module.getName() + "' is already registered.");
        }
        modules.add(module);
    }

    /**
     * Initializes and registers all client modules.
     * Called during client startup.
     */
    public void init() {
        // Example registration:
        // register(new ExampleCombatModule());
        // register(new ExampleMovementModule());
        // ...
    }

    /**
     * @return An unmodifiable view of all registered modules.
     */
    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    /**
     * Retrieves a module by its exact name (case-insensitive).
     * @param name The name of the module.
     * @return An Optional containing the module if found, empty otherwise.
     */
    public Optional<Module> getModule(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Retrieves a module by its class type.
     * @param clazz The class of the desired module.
     * @param <T> The module type.
     * @return An Optional containing the module if found, empty otherwise.
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> Optional<T> getModule(Class<T> clazz) {
        return modules.stream()
                .filter(clazz::isInstance)
                .map(m -> (T) m)
                .findFirst();
    }

    /**
     * Returns all modules belonging to a specific category.
     * @param category The category to filter by.
     * @return A list of modules in the category (modifiable for internal use, but consider immutability if exposed).
     */
    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .toList();
    }

    /**
     * Disables all currently enabled modules. Useful for client shutdown.
     */
    public void disableAll() {
        modules.forEach(m -> m.setEnabled(false));
    }
}
