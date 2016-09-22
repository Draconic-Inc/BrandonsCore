package com.brandon3055.brandonscore.api;

import java.util.LinkedList;

/**
 * Created by brandon3055 on 26/4/2016.
 * This is just a helper interface to make multiblocks a little easier.
 * This never really worked that well so it will probably get removed or replaced at some point.
 */
@Deprecated
public interface IMultiBlock {

    /**
     * @return true if the multiblock structure is valid.
     */
    boolean isStructureValid();

    /**
     * Tells the master to check if the structure is still valid and take appropriate action if the structure is no longer valid.
     *
     * @return true if the structure is still valid.
     */
    boolean validateStructure();

    /**
     * @return true if this tile is the structure controller.
     */
    boolean isController();

    /**
     * satellite structures are other separate multiblock structures that are linked to this structure
     *
     * @return true if this tile is the structure controller and the structure has satellites.
     */
    boolean hasSatelliteStructures();

    /**
     * @return the controller for the structure or null if the controller can not be found
     */
    IMultiBlock getController();

    /**
     * @return a list containing the controller for each of the satellite structures.
     */
    LinkedList<IMultiBlock> getSatelliteControllers();

}
