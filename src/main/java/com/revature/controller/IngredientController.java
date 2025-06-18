package com.revature.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.revature.service.IngredientService;
import com.revature.model.Ingredient;
import com.revature.util.Page;

/**
 * The IngredientController class handles operations related to ingredients. It
 * allows for creating, retrieving, updating, and deleting individual
 * ingredients, as well as retrieving a list of all ingredients.
 * 
 * The class interacts with the IngredientService to perform these operations.
 */

public class IngredientController {

    /**
     * A service that manages ingredient-related operations.
     */

    private IngredientService ingredientService;
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructs an IngredientController with the specified IngredientService.
     *
     * TODO: Finish the implementation so that this class's instance variables are
     * initialized accordingly.
     * 
     * @param ingredientService the service used to manage ingredient-related
     *                          operations
     */

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    /**
     * TODO: Retrieves a single ingredient by its ID.
     * 
     * If the ingredient exists, responds with a 200 OK status and the ingredient
     * data. If not found, responds with a 404 Not Found status.
     *
     * @param ctx the Javalin context containing the request path parameter for the
     *            ingredient ID
     */
    public void getIngredient(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var ingredient = ingredientService.findIngredient(id);
        if (ingredient.isPresent()) {
            ctx.status(200).json(ingredient.get());
        } else {
            ctx.status(404).result("Ingredient not found");
        }
    }

    /**
     * TODO: Deletes an ingredient by its ID.
     * 
     * Responds with a 204 No Content status.
     *
     * @param ctx the Javalin context containing the request path parameter for the
     *            ingredient id
     */
    public void deleteIngredient(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ingredientService.deleteIngredient(id);
        ctx.status(204);
    }

    /**
     * TODO: Updates an existing ingredient by its ID.
     * 
     * If the ingredient exists, updates it and responds with a 204 No Content
     * status. If not found, responds with a 404 Not Found status.
     *
     * @param ctx the Javalin context containing the request path parameter and
     *            updated ingredient data in the request body
     */
    public void updateIngredient(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var existingIngredient = ingredientService.findIngredient(id);
        if (existingIngredient.isPresent()) {
            try {
                Ingredient ingredient = objectMapper.readValue(ctx.body(), Ingredient.class);
                ingredient.setId(id);
                ingredientService.saveIngredient(ingredient);
                ctx.status(204);
            } catch (Exception e) {
                ctx.status(400).result("Invalid ingredient data");
            }
        } else {
            ctx.status(404).result("Ingredient not found");
        }
    }

    /**
     * TODO: Creates a new ingredient.
     * 
     * Saves the ingredient and responds with a 201 Created status.
     *
     * @param ctx the Javalin context containing the ingredient data in the request
     *            body
     */
    public void createIngredient(Context ctx) {
        try {
            Ingredient ingredient = objectMapper.readValue(ctx.body(), Ingredient.class);
            ingredientService.saveIngredient(ingredient);
            ctx.status(201);
        } catch (Exception e) {
            ctx.status(400).result("Invalid ingredient data");
        }
    }

    /**
     * TODO: Retrieves a paginated list of ingredients, or all ingredients if no
     * pagination parameters are provided.
     * 
     * If pagination parameters are included, returns ingredients based on page,
     * page size, sorting, and filter term.
     *
     * @param ctx the Javalin context containing query parameters for pagination,
     *            sorting, and filtering
     */
    public void getIngredients(Context ctx) {
        String term = ctx.queryParam("term");
        String pageParam = ctx.queryParam("page");
        String pageSizeParam = ctx.queryParam("pageSize");
        String sortBy = getParamAsClassOrElse(ctx, "sortBy", String.class, "name");
        String sortDirection = getParamAsClassOrElse(ctx, "sortDirection", String.class, "asc");

        // If no pagination parameters are provided, return simple array
        if (pageParam == null && pageSizeParam == null) {
            if (term != null && !term.trim().isEmpty()) {
                var ingredients = ingredientService.searchIngredients(term);
                ctx.status(200).json(ingredients);
            } else {
                var ingredients = ingredientService.searchIngredients("");
                ctx.status(200).json(ingredients);
            }
        } else {
            // Pagination parameters provided, return paginated result
            int page = getParamAsClassOrElse(ctx, "page", Integer.class, 1);
            int pageSize = getParamAsClassOrElse(ctx, "pageSize", Integer.class, 10);

            if (term != null && !term.trim().isEmpty()) {
                Page<Ingredient> ingredients = ingredientService.searchIngredients(term, page, pageSize, sortBy,
                        sortDirection);
                ctx.status(200).json(ingredients);
            } else {
                Page<Ingredient> ingredients = ingredientService.searchIngredients("", page, pageSize, sortBy,
                        sortDirection);
                ctx.status(200).json(ingredients);
            }
        }
    }

    /**
     * A helper method to retrieve a query parameter from the context as a specific
     * class type, or return a default value if the query parameter is not present.
     *
     * @param <T>          the type of the query parameter
     * @param ctx          the Javalin context containing query parameters
     * @param queryParam   the name of the query parameter to retrieve
     * @param clazz        the class type of the parameter
     * @param defaultValue the default value to return if the parameter is absent
     * @return the query parameter value as the specified type, or the default value
     *         if absent
     */
    private <T> T getParamAsClassOrElse(Context ctx, String queryParam, Class<T> clazz, T defaultValue) {
        String paramValue = ctx.queryParam(queryParam);
        if (paramValue != null) {
            if (clazz == Integer.class) {
                return clazz.cast(Integer.valueOf(paramValue));
            } else if (clazz == Boolean.class) {
                return clazz.cast(Boolean.valueOf(paramValue));
            } else {
                return clazz.cast(paramValue);
            }
        }
        return defaultValue;
    }

    /**
     * Configure the routes for ingredient operations.
     *
     * @param app the Javalin application
     */
    public void configureRoutes(Javalin app) {
        app.get("/ingredients", this::getIngredients);
        app.get("/ingredients/{id}", this::getIngredient);
        app.post("/ingredients", this::createIngredient);
        app.put("/ingredients/{id}", this::updateIngredient);
        app.delete("/ingredients/{id}", this::deleteIngredient);
    }
}
