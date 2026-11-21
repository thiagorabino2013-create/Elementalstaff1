package dev.elementalstaff;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ElementalStaffItem extends Item {
    private static final int COOLDOWN_TICKS = 100;

    public ElementalStaffItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            if (user.isSneaking()) {
                createTsunami(serverWorld, user);
            } else {
                createMeteor(serverWorld, user);
            }
            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        }

        return ActionResult.SUCCESS;
    }

    private void createMeteor(ServerWorld world, PlayerEntity player) {
        HitResult hit = player.raycast(48.0, 0.0F, false);
        Vec3d target = hit.getPos();
        Vec3d start = target.add(0.0, 28.0, 0.0);

        // Falling fire/smoke trail.
        for (int i = 0; i < 32; i++) {
            double t = i / 32.0;
            double x = start.x + (target.x - start.x) * t;
            double y = start.y + (target.y - start.y) * t;
            double z = start.z + (target.z - start.z) * t;
            world.spawnParticles(ParticleTypes.FLAME, x, y, z, 9, 0.55, 0.55, 0.55, 0.025);
            world.spawnParticles(ParticleTypes.LAVA, x, y, z, 3, 0.35, 0.35, 0.35, 0.01);
            world.spawnParticles(ParticleTypes.SMOKE, x, y, z, 5, 0.45, 0.45, 0.45, 0.02);
        }

        // Large in-game explosion: damages entities and destroys terrain.
        world.createExplosion(player, target.x, target.y, target.z,
                16.0F, false, World.ExplosionSourceType.TNT);

        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                target.x, target.y, target.z, 1, 0.0, 0.0, 0.0, 0.0);
        world.spawnParticles(ParticleTypes.CLOUD,
                target.x, target.y, target.z, 100, 3.0, 1.5, 3.0, 0.18);
    }

    private void createTsunami(ServerWorld world, PlayerEntity player) {
        Vec3d forward = player.getRotationVec(1.0F).normalize();
        Vec3d right = new Vec3d(-forward.z, 0.0, forward.x).normalize();
        Vec3d origin = player.getPos().add(forward.multiply(4.0));

        // A broad, advancing series of destructive impact waves.
        // Each burst damages entities and breaks terrain in the game world.
        for (int wave = 0; wave < 6; wave++) {
            Vec3d center = origin.add(forward.multiply(wave * 5.0));
            double y = player.getY() + 2.0;

            for (int column = -5; column <= 5; column++) {
                Vec3d point = center.add(right.multiply(column * 1.7));
                world.spawnParticles(ParticleTypes.SPLASH,
                        point.x, y, point.z, 24, 0.65, 2.2, 0.65, 0.16);
                world.spawnParticles(ParticleTypes.BUBBLE,
                        point.x, y, point.z, 10, 0.55, 1.8, 0.55, 0.08);
                world.spawnParticles(ParticleTypes.CLOUD,
                        point.x, y, point.z, 12, 0.8, 1.4, 0.8, 0.12);
            }

            // Smaller overlapping blasts create the tsunami's destructive front.
            world.createExplosion(player, center.x, y, center.z,
                    7.0F, false, World.ExplosionSourceType.TNT);
        }
    }
}
