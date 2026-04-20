public class Fly extends Module {
    public Fly() {
        super("Fly", "Allows creative flight.", Category.MOVEMENT, GLFW.GLFW_KEY_F);
    }

    @Override
    protected void onEnable() {
        // Subscribe to events, modify player abilities, etc.
    }

    @Override
    protected void onDisable() {
        // Restore normal flight state.
    }
}
