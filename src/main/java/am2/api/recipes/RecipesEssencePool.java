package am2.api.recipes;

import java.util.HashMap;

import am2.defs.ItemDefs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class RecipesEssencePool extends RecipesArsMagica{
	private static final RecipesEssencePool poolRecipesBase = new RecipesEssencePool();
	
	public static final RecipesEssencePool manaCrafting(){
		return poolRecipesBase;
	}
	
	public RecipesEssencePool(){
		RecipeList = new HashMap<Integer, RecipeArsMagica>();
		InitRecipes();
	}
	
	private void InitRecipes(){
		//Arcane Compendium
		AddRecipe(new ItemStack[]{new ItemStack(Items.BOOK)}, new ItemStack(ItemDefs.arcaneCompendium, 1));
		//AddRecipe(new ItemStack[]{new ItemStack(ItemDefs.arcaneCompendium)}, new ItemStack(ItemDefs.arcaneCompendium, 1));
		//Journal
		AddRecipe(new ItemStack[]{new ItemStack(Items.WRITTEN_BOOK)}, new ItemStack(ItemDefs.journal, 1));
		//AddRecipe(new ItemStack[]{new ItemStack(ItemDefs.journal)}, new ItemStack(ItemDefs.journal, 1));
	}
		
}