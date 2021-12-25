package UAW.world.blocks.power;

import UAW.graphics.UAWFxD;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.*;
import mindustry.entities.effect.MultiEffect;
import mindustry.graphics.Drawf;
import mindustry.world.blocks.power.ImpactReactor;

/**
 * Modified version of ImpactReactor
 * <p>
 * Power generation block which can use items, liquids or both as input sources for power production.
 * Can have rotators, using DrawRotator is not preferred due to on how weird it rotates.
 * </p>
 */
public class WarmUpGenerator extends ImpactReactor {
	public TextureRegion liquidRegion, rotatorRegion, heatRegion, topRegion;
	public Effect smokeEffect = new MultiEffect(Fx.melting, Fx.burning, Fx.fireSmoke);
	public float rotationSpeed = 15f;

	public WarmUpGenerator(String name) {
		super(name);
		warmupSpeed = 0.005f;
		hasItems = false;
		explosionRadius = size * 4;
		explosionDamage = size * 125;
		explodeEffect = UAWFxD.dynamicExplosion(explosionRadius);
	}

	@Override
	public void load() {
		bottomRegion = Core.atlas.find(name);
		liquidRegion = Core.atlas.find(name + "-liquid");
		rotatorRegion = Core.atlas.find(name + "-rotator");
		topRegion = Core.atlas.find(name + "-top");
		heatRegion = Core.atlas.find(name + "-heat");
	}

	@Override
	public TextureRegion[] icons() {
		if (rotatorRegion.found()) {
			return new TextureRegion[]{bottomRegion, rotatorRegion, topRegion};
		} else return new TextureRegion[]{bottomRegion, topRegion};
	}

	public class WarmUpGeneratorBuild extends ImpactReactorBuild {
		float intensity;

		@Override
		public void updateTile() {
			super.updateTile();
			intensity += warmup * edelta();
			if (warmup >= 0.001) {
				if (Mathf.chance(intensity / 5)) {
					smokeEffect.at(x, y);
				}
			}
		}

		@Override
		public void draw() {
			Draw.rect(bottomRegion, x, y);
			if (liquidRegion.found()) {
				Drawf.liquid(liquidRegion, x, y, liquids.total() / liquidCapacity, liquids.current().color);
			}
			if (rotatorRegion.found()) {
				Drawf.spinSprite(rotatorRegion, x, y, rotationSpeed * intensity);
			}
			Draw.rect(topRegion, x, y);
			Draw.color(Color.valueOf("ff9b59"));
			Draw.alpha((0.3f + Mathf.absin(Time.time, 3f, 0.9f)) * warmup);
			Draw.rect(heatRegion, x, y);

			Draw.reset();
		}

		@Override
		public void onDestroyed() {
			super.onDestroyed();
			Fires.create(tile);
		}
	}
}