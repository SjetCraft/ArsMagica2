package am2;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.common.base.Predicate;

import am2.api.power.IObeliskFuelHelper;
import net.minecraft.item.ItemStack;

public class ObeliskFuelHelper implements IObeliskFuelHelper{
	private HashMap<Predicate<ItemStack>, Integer> validFuels;

	public static final ObeliskFuelHelper instance = new ObeliskFuelHelper();

	private ObeliskFuelHelper(){
		validFuels = new HashMap<Predicate<ItemStack>, Integer>();
	}

	@Override
	public void registerFuelType(Predicate<ItemStack> stack, int burnTime){
		validFuels.put(stack, burnTime);
	}

	@Override
	public int getFuelBurnTime(ItemStack stack){
		if (stack.isEmpty())
			return 0;

		for (Entry<Predicate<ItemStack>, Integer> possibleFuel : validFuels.entrySet()){
			if (possibleFuel.getKey().apply(stack))
				return possibleFuel.getValue();
		}
		return 0;
	}
}
