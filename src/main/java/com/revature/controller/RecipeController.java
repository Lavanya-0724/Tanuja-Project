package com.revature.controller;

import io.javalin.http.Handler;
import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.revature.service.AuthenticationService;
import com.revature.service.RecipeService;
import com.revature.model.Recipe;
import com.revature.model.Chef;
import com.revature.util.Page;

/**
 * The RecipeController class provides RESTful endpoints for managing recipes.
 * It interacts with the RecipeService to fetch, create, update, and delete
 * recipes.
 * Handlers in this class are fields assigned to lambdas, which define the
 * behavior for each endpoint.
 */

public class RecipeController {

    /** The service used to interact with the recipe data. */
    private RecipeService recipeService;

    /** A service that handles authentication-related operations. */
    private AuthenticationService authService;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * TODO: Constructor that initializes the RecipeController with the parameters.
     * 
     * @param recipeService The service that handles the business logic for managing
     *                      recipes.
     *                      * @param authService the service used to manage
     *                      authentication-related operations
     */
    public RecipeController(RecipeService recipeService, AuthenticationService authService) {
        this.recipeService = recipeService;
        this.authService = authService;
    }

    /**
     * TODO: Handler for fetching all recipes. Supports pagination, sorting, and
     * filtering by recipe name or ingredient.
     * 
     * Responds with a 200 OK status and the list of recipes, or 404 Not Found with
     * a result of "No recipes found".
     */
    public Handler fetchAllRecipes = ctx -> {
        String term = ctx.queryParam("term");
        int page = getParamAsClassOrElse(ctx, "page", Integer.class, 1);
        int pageSize = getParamAsClassOrElse(ctx, "pageSize", Integer.class, 10);
        String sortBy = getParamAsClassOrElse(ctx, "sortBy", String.class, "name");
        String sortDirection = getParamAsClassOrElse(ctx, "sortDirection", String.class, "asc");

        if (term != null && !term.trim().isEmpty()) {
            Page<Recipe> recipes = recipeService.searchRecipes(term, page, pageSize, sortBy, sortDirection);
            if (recipes.getItems().isEmpty()) {
                ctx.status(404).result("No recipes found");
            } else {
                ctx.status(200).json(recipes);
            }
        } else {
            Page<Recipe> recipes = recipeService.searchRecipes("", page, pageSize, sortBy, sortDirection);
            if (recipes.getItems().isEmpty()) {
                ctx.status(404).result("No recipes found");
            } else {
                ctx.status(200).json(recipes);
            }
        }
    };

    /**
     * TODO: Handler for fetching a recipe by its ID.
     * 
     * If successful, responds with a 200 status code and the recipe as the response
     * body.
     * 
     * If unsuccessful, responds with a 404 status code and a result of "Recipe not
     * found".
     */
    public Handler fetchRecipeById = ctx -> {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var recipe = recipeService.findRecipe(id);
        if (recipe.isPresent()) {
            ctx.status(200).json(recipe.get());
        } else {
            ctx.status(404).result("Recipe not found");
        }
    };

    /**
     * TODO: Handler for creating a new recipe. Requires authentication via an
     * authorization token taken from the request header.
     * 
     * If successful, responds with a 201 Created status.
     * If unauthorized, responds with a 401 Unauthorized status.
     */
    public Handler createRecipe = ctx -> {
        String authToken = ctx.header("Authorization");
        if (authToken == null) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        // Remove "Bearer " prefix if present
        if (authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7);
        }

        Chef chef = authService.getChefFromSessionToken(authToken);
        if (chef == null) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        Recipe recipe = objectMapper.readValue(ctx.body(), Recipe.class);
        recipe.setAuthor(chef);
        recipeService.saveRecipe(recipe);
        ctx.status(201);
    };

    /**
     * TODO: Handler for deleting a recipe by its id.
     * 
     * If successful, responds with a 200 status and result of "Recipe deleted
     * successfully."
     * 
     * Otherwise, responds with a 404 status and a result of "Recipe not found."
     */
    public Handler deleteRecipe = ctx -> {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var recipe = recipeService.findRecipe(id);
        if (recipe.isPresent()) {
            recipeService.deleteRecipe(id);
            ctx.status(200).result("Recipe deleted successfully.");
        } else {
            ctx.status(404).result("Recipe not found.");
        }
    };

    /**
     * TODO: Handler for updating a recipe by its ID.
     * 
     * If successful, responds with a 200 status code and the updated recipe as the
     * response body.
     * 
     * If unsuccessfuly, responds with a 404 status code and a result of "Recipe not
     * found."
     */
    public Handler updateRecipe = ctx -> {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var existingRecipe = recipeService.findRecipe(id);
        if (existingRecipe.isPresent()) {
            Recipe recipe = objectMapper.readValue(ctx.body(), Recipe.class);
            recipe.setId(id);
            recipe.setAuthor(existingRecipe.get().getAuthor()); // Preserve the original author
            recipeService.saveRecipe(recipe);
            ctx.status(200).json(recipe);
        } else {
            ctx.status(404).result("Recipe not found.");
        }
    };

    /**
     * A helper method to retrieve a query parameter from the context as a specific
     * class type, or return a default value if the query parameter is not present.
     * 
     * @param <T>          The type of the query parameter to be returned.
     * @param ctx          The context of the request.
     * @param queryParam   The query parameter name.
     * @param clazz        The class type of the query parameter.
     * @param defaultValue The default value to return if the query parameter is not
     *                     found.
     * @return The value of the query parameter converted to the specified class
     *         type, or the default value.
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
     * Configure the routes for recipe operations.
     *
     * @param app the Javalin application
     */
    public void configureRoutes(Javalin app) {
        app.get("/recipes", fetchAllRecipes);
        app.get("/recipes/{id}", fetchRecipeById);
        app.post("/recipes", createRecipe);
        app.put("/recipes/{id}", updateRecipe);
        app.delete("/recipes/{id}", deleteRecipe);
    }
}
