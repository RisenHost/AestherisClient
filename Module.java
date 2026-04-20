import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all client modules. 
 * Supports full manual configuration for Velaris/Prestige style setups.
 */
public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private int keybind; // LWJGL keycode, -1 = unbound
    private boolean enabled;
    
    // This holds your manual configurations (Sliders, Toggles, etc.)
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category, int keybind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keybind = keybind;
        this.enabled = false;
    }

    // ---------- Setting Management (For Manual Config) ----------
    
    public void addSetting(Setting<?> setting) {
        this.settings.add(setting);
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    // ---------- State Management ----------
    
    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    // ---------- Lifecycle Hooks ----------
    
    /** Called when the module is enabled. Override to add behavior. */
    protected void onEnable() {}

    /** Called when the module is disabled. Override to add cleanup behavior. */
    protected void onDisable() {}

    // ---------- Getters & Setters ----------
    
    public String getName() { return name; }
    
    public String getDescription() { return description; }
    
    public Category getCategory() { return category; }
    
    public int getKeybind() { return keybind; }
    
    public void setKeybind(int keybind) { this.keybind = keybind; }
    
    public boolean isEnabled() { return enabled; }
}
