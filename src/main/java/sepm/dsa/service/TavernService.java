package sepm.dsa.service;

import sepm.dsa.model.Location;
import sepm.dsa.model.Region;
import sepm.dsa.model.Tavern;

import java.util.List;

public interface TavernService {

    /**
     * Calculates the price for a night's stay in the given tavern.
     *
     * @param tavern The chosen tavern; must not be null
     * @return The costs of a hypothetical stay.
     */
    int getPriceForStay(Tavern tavern);

	/**
	 * Get all taverns for a specified location or empty List if nothing found
	 *
	 * @param location must not be null
	 * @return the taverns for the location or empty list
	 */
	List<Tavern> getAllForLocation(Location location);

    /**
     * Get all taverns
     *
     * @return the taverns for the location or empty list
     */
    List<Tavern> getAll();


    /**
     * Update a Tavern
     *
     * @param t Tavern (must not be null)
     * @return The updated tavern model.
     */
    Tavern update(Tavern t);

	void remove(Tavern tavern);

	void add(Tavern tavern);

}
