//so wie die flugbahn auf das target wird rot angezeigt, die wie du aimst wird blau angezeigt und wenn die
// beiden sich überschneiden wirds kp lila angezeigt
package meteordevelopment.meteorclient.systems.modules.own;

import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class AimAssist extends Module {
    public AimAssist() {
        super(Categories.Own, "AimAssist", "Shows you the best angle to shoot with arrows / trajectories.");
    }
}
