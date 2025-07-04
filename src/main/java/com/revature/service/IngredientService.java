package com.revature.service;

import java.util.List;
import java.util.Optional;

import com.revature.dao.IngredientDAO;
import com.revature.model.Ingredient;
import com.revature.util.Page;
import com.revature.util.PageOptions;

/**
 * The IngredientService class provides services related to Ingredient
 * objects, including CRUD operations and search functionalities. It serves
 * as a mediator between the data access layer (IngredientDao) and the
 * application logic, ensuring that all operations on Ingredient objects
 * are performed consistently and efficiently.
 */

public class IngredientService {

    /**
     * The data access object used for performing operations on Ingredient entities.
     */
    private IngredientDAO ingredientDAO;

    /**
     * Constructs an IngredientService with the specified IngredientDao.
     *
     * TODO: Finish the implementation so that this class's instance variables are
     * initialized accordingly.
     * 
     * @param ingredientDao the IngredientDao to be used by this service for data
     *                      access
     */

    public IngredientService(IngredientDAO ingredientDAO) {
        this.ingredientDAO = ingredientDAO;
    }

    /**
     * TODO: Finds an Ingredient by its unique identifier.
     *
     * @param id the unique identifier of the Ingredient
     * @return an Optional containing the Ingredient if found, or an empty Optional
     *         if not found
     */
    public Optional<Ingredient> findIngredient(int id) {
        Ingredient ingredient = ingredientDAO.getIngredientById(id);
        return Optional.ofNullable(ingredient);
    }

    /**
     * TODO: Searches for Ingredients based on a search term with pagination and
     * sorting options.
     *
     * @param term          the search term for filtering Ingredients by attributes
     * @param page          the page number to retrieve
     * @param pageSize      the number of results per page
     * @param sortBy        the field to sort the results by
     * @param sortDirection the direction of sorting (e.g., "asc" or "desc")
     * @return a Page object containing the list of Ingredients matching the
     *         criteria
     */
    public Page<Ingredient> searchIngredients(String term, int page, int pageSize, String sortBy,
            String sortDirection) {
        PageOptions pageOptions = new PageOptions(page, pageSize, sortBy, sortDirection);
        if (term == null || term.trim().isEmpty()) {
            return ingredientDAO.getAllIngredients(pageOptions);
        } else {
            return ingredientDAO.searchIngredients(term, pageOptions);
        }
    }

    /**
     * TODO: Searches for Ingredients based on a search term.
     * If the term is null, retrieves all Ingredients.
     *
     * @param term the search term used to find ingredients
     * @return a list of Ingredient objects that match the search term
     */
    public List<Ingredient> searchIngredients(String term) {
        if (term == null || term.trim().isEmpty()) {
            return ingredientDAO.getAllIngredients();
        } else {
            return ingredientDAO.searchIngredients(term);
        }
    }

    /**
     * TODO: Deletes an Ingredient by its unique identifier, if it exists.
     *
     * @param id the unique identifier of the ingredient to be deleted
     */

    public void deleteIngredient(int id) {
        Ingredient ingredient = ingredientDAO.getIngredientById(id);
        if (ingredient != null) {
            ingredientDAO.deleteIngredient(ingredient);
        }
    }

    /**
     * TODO: Saves an Ingredient entity. If the Ingredient's ID is zero, a new
     * Ingredient is created and the `ingredient` parameter's ID is updated.
     * 
     * Otherwise, updates the existing Ingredient.
     *
     * @param ingredient the Ingredient entity to be saved or updated
     */
    public void saveIngredient(Ingredient ingredient) {
        if (ingredient.getId() == 0) {
            // Create new ingredient
            int newId = ingredientDAO.createIngredient(ingredient);
            ingredient.setId(newId);
        } else {
            // Update existing ingredient
            ingredientDAO.updateIngredient(ingredient);
        }
    }
}
