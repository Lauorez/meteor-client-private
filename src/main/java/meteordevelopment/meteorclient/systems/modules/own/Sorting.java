//mit bot alle kisten in feststellbaren bereich ablaufen und checken ob item in inventar = item in chest -> item in chest legen
package meteordevelopment.meteorclient.systems.modules.own;

import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class Sorting extends Module {
    public Sorting() {
        super(Categories.Own, "Sorting", "Scans nearby chests and tries to Stack inventory Items into these chests..");
    }
}
