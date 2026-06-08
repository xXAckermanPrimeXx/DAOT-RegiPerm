package com.zenyssevernox.regiperm.mixin;

import com.zenyssevernox.regiperm.data.RegiPermClaim;
import com.zenyssevernox.regiperm.data.RegiPermState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;

import java.util.List;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow
    @Final
    private World world;

    @ModifyVariable(
            method = "collectBlocksAndDamageEntities",
            at = @At("STORE"),
            require = 0
    )
    private List<?> regiperm$protectClaimedBlocks(List<?> affectedBlocks) {

        System.out.println("REGIPERM EXPLOSION MIXIN FIRED");

        if (!(this.world instanceof ServerWorld serverWorld)) {
            return affectedBlocks;
        }

        RegiPermState state = RegiPermState.getServerState(serverWorld.getServer());

        affectedBlocks.removeIf(object -> {
            if (!(object instanceof net.minecraft.util.math.BlockPos pos)) {
                return false;
            }

            RegiPermClaim claim = state.getClaimAt(new ChunkPos(pos));
            return claim != null;
        });

        return affectedBlocks;
    }
}