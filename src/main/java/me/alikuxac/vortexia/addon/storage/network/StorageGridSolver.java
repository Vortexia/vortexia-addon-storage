// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import me.alikuxac.vortexia.api.grid.Grid;
import me.alikuxac.vortexia.api.grid.GridSolver;

public class StorageGridSolver implements GridSolver {

    private final StorageAddon addon;

    public StorageGridSolver(StorageAddon addon) {
        this.addon = addon;
    }

    @Override
    public void solve(Grid coreGrid) {
        StorageGrid storageGrid = addon.getNetworkManager().getOrCreateGrid(coreGrid);
        storageGrid.tick();
    }
}
