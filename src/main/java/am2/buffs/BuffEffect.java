package am2.buffs;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import am2.ArsMagica2;
import am2.proxy.CommonProxy;

public abstract class BuffEffect extends PotionEffect{
	protected boolean InitialApplication;
	protected boolean HasNotified;

	public BuffEffect(Potion buffID, int duration, int amplifier){
		super(buffID, duration, amplifier);
		InitialApplication = true;
		HasNotified = ((duration / 20) > 5) ? false : true;
	}

	public boolean shouldNotify(){
		return true;
	}
	
	/**
	 * Called when the effect is created
	 * @param entityliving
	 */
	public abstract void applyEffect(EntityLivingBase entityliving);

	public abstract void stopEffect(EntityLivingBase entityliving);

	private void effectEnding(EntityLivingBase entityliving){
		stopEffect(entityliving);
	}

	@Override
	public void performEffect(EntityLivingBase entityliving){
	}

	@Override
	public void combine(PotionEffect potioneffect){
		if (!(potioneffect instanceof BuffEffect)){
			return;
		}
		int thisAmplifier = this.getAmplifier();
		if (thisAmplifier >= potioneffect.getAmplifier()){
			super.combine(potioneffect);
			this.HasNotified = false;
		}
	}

	@Override
	public boolean onUpdate(EntityLivingBase entityliving){
		if (InitialApplication){
			InitialApplication = false;
			applyEffect(entityliving);
		}
		else if (getDuration() <= 1){
			effectEnding(entityliving);
		}else if ((getDuration() / 20) < 5 && !HasNotified && shouldNotify() && !entityliving.worldObj.isRemote){
			HasNotified = true;
		}
		performEffect(entityliving);
		if (ArsMagica2.proxy instanceof CommonProxy){
			return super.onUpdate(entityliving);
		}else{
			return true;
		}
	}

	public boolean isReady(int i, int j){
		int k = 25 >> j;
		if (k > 0){
			return i % k == 0;
		}else{
			return true;
		}
	}
	
	protected abstract String spellBuffName();

	@Override
	public String getEffectName(){
		return String.format("Spell: %s", spellBuffName());
	}
}
