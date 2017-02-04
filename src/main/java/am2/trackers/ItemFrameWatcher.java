package am2.trackers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import am2.ArsMagica2;
import am2.api.recipes.RecipesEssencePool;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleApproachEntity;
import am2.particles.ParticleColorShift;
import am2.particles.ParticleHoldPosition;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFrameWatcher{

	private final HashMap<EntityItemFrameComparator, Integer> watchedFrames;
	private final ArrayList<EntityItemFrameComparator> queuedAddFrames;
	private final ArrayList<EntityItemFrameComparator> queuedRemoveFrames;

	private static final int processTime = 800;
	
	RecipesEssencePool poolRecipes = new RecipesEssencePool();

	public ItemFrameWatcher(){
		watchedFrames = new HashMap<EntityItemFrameComparator, Integer>();
		queuedAddFrames = new ArrayList<EntityItemFrameComparator>();
		queuedRemoveFrames = new ArrayList<EntityItemFrameComparator>();
	}

	public void checkWatchedFrames(){
		ArrayList<EntityItemFrameComparator> toRemove = new ArrayList<EntityItemFrameComparator>();

		updateQueuedChanges();

		for (EntityItemFrameComparator frameComp : watchedFrames.keySet()){

			Integer time = watchedFrames.get(frameComp);
			if (time == null) time = 0;

			if (frameComp == null || frameComp.frame == null || frameComp.frame.worldObj == null)
				continue;

			if (!frameComp.frame.worldObj.isRemote || time >= processTime)
				toRemove.add(frameComp);

			if (frameIsValid(frameComp.frame)){
				if (!checkFrameRadius(frameComp)){
					toRemove.remove(frameComp);
				}
			}else{
				time++;
				watchedFrames.put(frameComp, time);
			}
		}

		for (EntityItemFrameComparator frame : toRemove){
			stopWatchingFrame(frame.frame);
		}
	}

	private boolean checkFrameRadius(EntityItemFrameComparator frameComp){

		int radius = 3;

		boolean shouldRemove = true;
		Integer time;

		EntityItemFrame frame = frameComp.frame;
		time = watchedFrames.get(frameComp);

		for (int i = -radius; i <= radius; ++i){
			for (int j = -radius; j <= radius; ++j){
				for (int k = -radius; k <= radius; ++k){

					if (frame.worldObj.getBlockState(frame.getPosition().add(i, j, k)).getBlock() == BlockDefs.liquid_essence.getBlock()){
						if (time == null){
							time = 0;
						}
						time++;



						if (time >= processTime){
							if (!frame.worldObj.isRemote){
								EntityItem output = new EntityItem(frame.worldObj, Math.floor(frame.posX) + 0.5, frame.posY, Math.floor(frame.posZ) + 0.5,poolRecipes.matchingRecipe(frame.getDisplayedItem().getItem()));
								frame.setDead();
								frame.worldObj.spawnEntityInWorld(output);
								shouldRemove = true;
								break;
							}
						}else{
							shouldRemove = false;
							if (frame.worldObj.isRemote && (time / 64) % 1 == 0){
								spawnCompendiumProgressParticles(frame, frame.getPosition().add(i, j, k));
							}
						}
					}
				}
			}
		}

		watchedFrames.put(frameComp, time);
		return shouldRemove;
	}

	private boolean frameIsValid(EntityItemFrame frame){
		return frame != null && !frame.isDead && frame.getDisplayedItem() != null && poolRecipes.matchingRecipe(frame.getDisplayedItem().getItem()) != null;
	}

	private void updateQueuedChanges(){
		
		//safe copy to avoid CME
		EntityItemFrameComparator[] toAdd = queuedAddFrames.toArray(new EntityItemFrameComparator[queuedAddFrames.size()]);
		queuedAddFrames.clear();

		for (EntityItemFrameComparator comp : toAdd){
			if (comp.frame != null && (comp.frame.getDisplayedItem() == null || comp.frame.getDisplayedItem().getItem() != poolRecipes.matchingRecipe(comp.frame.getDisplayedItem().getItem()).getItem()))
				watchedFrames.put(comp, 0);
		}

		//safe copy to avoid CME, again with queued removes
		EntityItemFrameComparator[] toRemove = queuedRemoveFrames.toArray(new EntityItemFrameComparator[queuedRemoveFrames.size()]);
		queuedRemoveFrames.clear();

		for (EntityItemFrameComparator comp : toRemove){
			Integer time = watchedFrames.get(comp);
			if (time != null && time >= processTime &&
					comp.frame != null && !comp.frame.isDead && comp.frame.worldObj.isRemote &&
					(comp.frame.getDisplayedItem() != null &&
							(comp.frame.getDisplayedItem().getItem() == poolRecipes.matchingRecipe(comp.frame.getDisplayedItem().getItem()).getItem()))){
				spawnCompendiumCompleteParticles(comp.frame);
			}
			watchedFrames.remove(comp);
		}
	}

	public void startWatchingFrame(EntityItemFrame frame){
		queuedAddFrames.add(new EntityItemFrameComparator(frame));
	}

	public void stopWatchingFrame(EntityItemFrame frame){
		queuedRemoveFrames.add(new EntityItemFrameComparator(frame));
	}

	@SideOnly(Side.CLIENT)
	public void spawnCompendiumProgressParticles(EntityItemFrame frame, BlockPos pos){
		Random rand = new Random();
		AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(frame.worldObj, "symbols", pos.getX() + rand.nextDouble(), pos.getY() + rand.nextDouble(), pos.getZ() + rand.nextDouble());
		if (particle != null){
			particle.setIgnoreMaxAge(true);
			particle.AddParticleController(new ParticleApproachEntity(particle, frame, 0.02f, 0.04f, 1, false).setKillParticleOnFinish(true));
			//particle.AddParticleController(new ParticleArcToEntity(particle, 1, frame, false).SetSpeed(0.02f).setKillParticleOnFinish(true));
			particle.setRandomScale(0.05f, 0.12f);
		}
	}

	@SideOnly(Side.CLIENT)
	public void spawnCompendiumCompleteParticles(EntityItemFrame frame){
		AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(frame.worldObj, "radiant", frame.posX, frame.posY, frame.posZ);
		if (particle != null){
			particle.setIgnoreMaxAge(false);
			particle.setMaxAge(40);
			particle.setParticleScale(0.3f);
			//particle.AddParticleController(new ParticleApproachEntity(particle, frame, 0.02f, 0.04f, 1, false).setKillParticleOnFinish(true));
			particle.AddParticleController(new ParticleHoldPosition(particle, 40, 1, false));
			particle.AddParticleController(new ParticleColorShift(particle, 1, false).SetShiftSpeed(0.2f));
		}
	}

	private class EntityItemFrameComparator{
		private final EntityItemFrame frame;

		public EntityItemFrameComparator(EntityItemFrame frame){
			this.frame = frame;
		}

		@Override
		public boolean equals(Object obj){
			if (frame == null) return false;
			if (obj instanceof EntityItemFrame){
				return ((EntityItemFrame)obj).getEntityId() == frame.getEntityId() && ((EntityItemFrame)obj).worldObj.isRemote == frame.worldObj.isRemote;
			}
			if (obj instanceof EntityItemFrameComparator){
				return ((EntityItemFrameComparator)obj).frame.getEntityId() == frame.getEntityId() && ((EntityItemFrameComparator)obj).frame.worldObj.isRemote == frame.worldObj.isRemote;
			}
			return false;
		}

		@Override
		public int hashCode(){
			if (frame == null || frame.worldObj == null) return 0;
			return frame.getEntityId() + (frame.worldObj.isRemote ? 1 : 2);
		}
	}
}
