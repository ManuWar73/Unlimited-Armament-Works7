package UAW.world.blocks.gas;

import UAW.content.UAWGas;
import arc.Core;
import arc.func.Func;
import gas.GasStack;
import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.ui.Bar;
import mindustry.world.consumers.*;
import mindustry.world.meta.Stat;

import static UAW.Vars.tick;

public class LiquidBoiler extends GasCrafter {
	public float waterAmount = 30f;
	public float steamMultiplier = 1.5f;

	public LiquidBoiler(String name) {
		super(name);
		warmupSpeed = 0.01f;
		craftTime = 2 * tick;
	}

	@Override
	public void init() {
		super.init();
		hasItems = true;
		hasLiquids = true;
		hasGasses = true;
		outputsGas = true;
		gasCapacity = waterAmount * steamMultiplier * 1.5f;
		liquidCapacity = waterAmount * 1.5f;
		consumes.liquid(Liquids.water, waterAmount / craftTime);
		outputGas = new GasStack(UAWGas.steam, waterAmount * steamMultiplier);
	}

	@Override
	public void setStats() {
		super.setStats();
		stats.remove(Stat.productionTime);
	}

	@Override
	public void setBars() {
		super.setBars();
		Func<Building, Liquid> current;

		bars.add("heat", (LiquidBoilerBuild entity) ->
			new Bar(() ->
				Core.bundle.format("bar.heat", (int) (entity.warmup)),
				() -> Pal.lightOrange,
				entity::warmupProgress
			));

		if (consumes.has(ConsumeType.liquid) && consumes.get(ConsumeType.liquid) instanceof ConsumeLiquid) {
			Liquid liquid = consumes.<ConsumeLiquid>get(ConsumeType.liquid).liquid;
			current = entity -> liquid;
		} else current = entity -> entity.liquids == null ? Liquids.water : entity.liquids.current();

		bars.add("liquid", entity -> new Bar(() -> entity.liquids.get(current.get(entity)) <= 0.001f ? Core.bundle.get("bar.liquid") : current.get(entity).localizedName,
			() -> current.get(entity).barColor(), () -> entity == null || entity.liquids == null ? 0f : entity.liquids.get(current.get(entity)) / liquidCapacity));
	}

	public class LiquidBoilerBuild extends GasCrafterBuild {

		public float warmupProgress() {
			return warmup;
		}

		@Override
		public void updateTile() {
			super.updateTile();
			if (progress >= 1f) {
				consume();

				if (outputItems != null) {
					for (ItemStack output : outputItems) {
						for (int i = 0; i < output.amount; i++) {
							offload(output.item);
						}
					}
				}

				if (outputLiquid != null) {
					handleLiquid(this, outputLiquid.liquid, outputLiquid.amount);
				}

				if (outputGas != null && warmupProgress() >= 0.85f) {
					handleGas(this, outputGas.gas, outputGas.amount);
				}

				craftEffect.at(x, y);
				progress %= 1f;
			}

			if (outputItems != null && timer(timerDump, dumpTime / timeScale)) {
				for (ItemStack output : outputItems) {
					dump(output.item);
				}
			}

			if (outputLiquid != null && outputLiquid.liquid != null && hasLiquids) {
				dumpLiquid(outputLiquid.liquid);
			}

			if (outputGas != null && outputGas.gas != null && hasGasses) {
				dumpGas(outputGas.gas);
			}
		}
	}
}
