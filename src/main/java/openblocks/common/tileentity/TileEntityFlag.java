package openblocks.common.tileentity;

import java.util.Set;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import openmods.api.IActivateAwareTile;
import openmods.api.IPlaceAwareTile;
import openmods.colors.ColorMeta;
import openmods.model.eval.EvalModelState;
import openmods.sync.ISyncListener;
import openmods.sync.ISyncableObject;
import openmods.sync.SyncMap;
import openmods.sync.SyncableEnum;
import openmods.sync.SyncableFloat;
import openmods.tileentity.SyncedTileEntity;

public class TileEntityFlag extends SyncedTileEntity implements IPlaceAwareTile, IActivateAwareTile {

	private SyncableFloat angle;
	private SyncableEnum<ColorMeta> colorIndex;

	private EvalModelState clipsState = EvalModelState.EMPTY;

	public TileEntityFlag() {}

	@Override
	protected void createSyncedFields() {
		angle = new SyncableFloat();
		colorIndex = SyncableEnum.create(ColorMeta.GREEN);
	}

	@Override
	protected void onSyncMapCreate(SyncMap syncMap) {
		syncMap.addUpdateListener(new ISyncListener() {
			@Override
			public void onSync(Set<ISyncableObject> changes) {
				if (changes.contains(angle)) {
					setStateAngle(angle.get());
					markBlockForRenderUpdate(getPos());
				}
			}
		});
	}

	public ColorMeta getColor() {
		return colorIndex.get();
	}

	public float getAngle() {
		return angle.get();
	}

	@Override
	public boolean onBlockActivated(EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player != null && player.isSneaking()) { return true; }
		if (!worldObj.isRemote) {
			if (getOrientation().down() == EnumFacing.DOWN) {
				angle.set(angle.get() + 10f);
				sync();
				return false;
			}
		}
		return true;
	}

	@Override
	public void onBlockPlacedBy(IBlockState state, EntityLivingBase placer, ItemStack stack) {
		final EnumFacing rotation = getOrientation().up();
		if (rotation == EnumFacing.UP) {
			final float playerAngle = placer.rotationYawHead;
			final int angle = MathHelper.floor_float(playerAngle / 10) * 10;
			this.angle.set(angle);
			setStateAngle(angle);
		}

		colorIndex.set(ColorMeta.fromBlockMeta(stack.getItemDamage() & 0xF));
	}

	public EvalModelState getRenderState() {
		return clipsState;
	}

	private void setStateAngle(float angle) {
		final float arg = 1 - (MathHelper.wrapDegrees(angle) + 180.0f) / 360.0f; // TODO move to blockstate once model is POWERFULL
		clipsState = clipsState.withArg("rotation", arg);
	}
}
