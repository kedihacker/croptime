package com.croptime;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.TreeMap;

public interface IwheatData extends Component , ServerTickingComponent {
    TreeMap<Long, List<BlockPos>> gettree();
    void settree(TreeMap<Long, List<BlockPos>> tree);
}
