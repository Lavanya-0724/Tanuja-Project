package com.revature.test;

import static org.mockito.Mockito.*;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.junit.jupiter.api.Test;

import com.revature.controller.RecipeController;
import com.revature.model.Recipe;
import com.revature.service.AuthenticationService;
import com.revature.service.RecipeService;
import com.revature.util.Page;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class RecipeControllerTest {

    @Test
    public void testGetRecipesWithRecipeName() throws Exception {
        RecipeService recipeService = mock(RecipeService.class);
        AuthenticationService authService = mock(AuthenticationService.class);
        List<Recipe> mockResults = Collections.singletonList(new Recipe("Grilled Cheese", "Grill bread and cheese"));
        Page<Recipe> mockPage = new Page<>(1, 10, 1, 1, mockResults);
        when(recipeService.searchRecipes("Cheese", 1, 10, "name", "asc")).thenReturn(mockPage);

        Context ctx = mock(Context.class);
        when(ctx.queryParam("term")).thenReturn("Cheese");
        when(ctx.status(200)).thenReturn(ctx);
        when(ctx.json(any())).thenReturn(ctx);

        Handler getRecipes = new RecipeController(recipeService, authService).fetchAllRecipes;
        getRecipes.handle(ctx);

        verify(ctx).status(200);
        verify(ctx).json(mockPage);
    }

    @Test
    public void testGetRecipesWithNoParams() throws Exception {
        RecipeService recipeService = mock(RecipeService.class);
        AuthenticationService authService = mock(AuthenticationService.class);
        List<Recipe> allRecipes = Arrays.asList(new Recipe("Apple Pie"), new Recipe("Grilled Cheese"),
                new Recipe("Steak"));
        Page<Recipe> mockPage = new Page<>(1, 10, 1, 3, allRecipes);
        when(recipeService.searchRecipes("", 1, 10, "name", "asc")).thenReturn(mockPage);

        Context ctx = mock(Context.class);
        when(ctx.queryParam("term")).thenReturn(null);
        when(ctx.status(200)).thenReturn(ctx);
        when(ctx.json(any())).thenReturn(ctx);

        Handler getRecipesHandler = new RecipeController(recipeService, authService).fetchAllRecipes;
        getRecipesHandler.handle(ctx);

        verify(ctx).status(200); // Set the response status code
        verify(ctx).json(mockPage);
    }

    @Test
    public void testGetRecipesWithNoResults() throws Exception {
        RecipeService recipeService = mock(RecipeService.class);
        AuthenticationService authService = mock(AuthenticationService.class);
        Page<Recipe> emptyPage = new Page<>(1, 10, 0, 0, Collections.emptyList());
        when(recipeService.searchRecipes("Nonexistent Recipe", 1, 10, "name", "asc")).thenReturn(emptyPage);

        Context ctx = mock(Context.class);
        when(ctx.queryParam("term")).thenReturn("Nonexistent Recipe");
        when(ctx.status(404)).thenReturn(ctx);
        when(ctx.result(anyString())).thenReturn(ctx);

        Handler getRecipes = new RecipeController(recipeService, authService).fetchAllRecipes;
        getRecipes.handle(ctx);

        verify(ctx).status(404);
        verify(ctx).result("No recipes found");
    }
}