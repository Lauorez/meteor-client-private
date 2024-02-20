package meteordevelopment.meteorclient.systems.modules.Own;

import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class TooManyHacks extends Module {
    public TooManyHacks() {
        super(Categories.Own, "TooManyHacks", "Deactivates hotkeys so you dont activate them by mistake.");
    }
}
