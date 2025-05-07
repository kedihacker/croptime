package org.agriad.untitled;

import io.wispforest.owo.config.annotation.*;


@Modmenu(modId = "croptime")
@Config(name = "croptimeconfig", wrapperName = "MyConfig")
public class ModConfigmodel {
    public int anIntOption = 16;
    public growthenum growthmodel = growthenum.SIMULATED;

    public enum growthenum {
        SIMULATED, FIXEDTIME;
    }

}
