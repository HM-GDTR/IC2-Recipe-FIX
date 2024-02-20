package site.rainbowx.icrecipefix.mixin;

import com.google.gson.JsonObject;
import ic2.core.recipe.v2.RecipeHolder;
import ic2.core.recipe.v2.WeightedMachineRecipeSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.TreeMap;

@Mixin({WeightedMachineRecipeSerializer.class})
public abstract class WeightedMachineRecipeSerializerMixin {
    private static Map<Identifier, String> mp = new TreeMap<>();

    @Shadow
    public abstract RecipeHolder read(Identifier id, JsonObject json);

    @Inject(at = @At(value = "TAIL"), method = "Lic2/core/recipe/v2/WeightedMachineRecipeSerializer;read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lic2/core/recipe/v2/RecipeHolder;")
    public void afterReadJson(Identifier id, JsonObject json, CallbackInfoReturnable<RecipeHolder> ci) {
        RecipeHolder ret = ci.getReturnValue();
        if(!mp.containsKey(ret.getId())) {
            mp.put(ret.getId(), json.toString());
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "Lic2/core/recipe/v2/WeightedMachineRecipeSerializer;read(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lic2/core/recipe/v2/RecipeHolder;", cancellable = true)
    public void beforeReadBuf(Identifier id, PacketByteBuf buf, CallbackInfoReturnable<RecipeHolder> ci) {
        RecipeHolder ret;
        String jsonStr = buf.readString();
        JsonObject obj = JsonHelper.deserialize(jsonStr);
        ci.setReturnValue(read(id, obj));
        ci.cancel();
    }

    @Inject(at = @At(value = "HEAD"), method = "Lic2/core/recipe/v2/WeightedMachineRecipeSerializer;write(Lnet/minecraft/network/PacketByteBuf;Lic2/core/recipe/v2/RecipeHolder;)V", cancellable = true)
    public void beforeWriteBuf(PacketByteBuf buf, RecipeHolder recipe, CallbackInfo ci) {
        String str = mp.get(recipe.getId());
        buf.writeString(str);
        ci.cancel();
    }
}
