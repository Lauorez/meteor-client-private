package meteordevelopment.meteorclient.systems.modules.own;

import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class TooManyHacks extends Module {
    public TooManyHacks() {
        super(Categories.Own, "Too Many Hacks", "Deactivates hotkeys so you dont activate them by mistake.");
    }
}
