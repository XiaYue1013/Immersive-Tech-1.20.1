package immersivetech.port;

import java.util.Objects;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public final class BoilerRecipe implements Recipe<Container> {
    public static final Serializer SERIALIZER = new Serializer();
    public static final RecipeType<BoilerRecipe> TYPE = new RecipeType<>() {
        @Override public String toString() { return ImmersiveTech.MOD_ID + ":boiler"; }
    };
    private final ResourceLocation id;
    private final Fluid input;
    private final int inputAmount;
    private final int outputAmount;
    private final int time;

    public BoilerRecipe(ResourceLocation id, Fluid input, int inputAmount, int outputAmount, int time) {
        this.id = id; this.input = input; this.inputAmount = inputAmount;
        this.outputAmount = outputAmount; this.time = time;
    }
    public Fluid input() { return input; }
    public int inputAmount() { return inputAmount; }
    public int outputAmount() { return outputAmount; }
    public int time() { return time; }
    @Override public boolean matches(Container c, Level l) { return false; }
    @Override public ItemStack assemble(Container c, RegistryAccess a) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess a) { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return SERIALIZER; }
    @Override public RecipeType<?> getType() { return TYPE; }

    public static final class Serializer implements RecipeSerializer<BoilerRecipe> {
        @Override public BoilerRecipe fromJson(ResourceLocation id, JsonObject json) {
            Fluid input = Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(
                    new ResourceLocation(json.get("input_fluid").getAsString())),
                    "Unknown boiler input fluid");
            return new BoilerRecipe(id, input, json.get("input_amount").getAsInt(),
                    json.get("output_amount").getAsInt(), json.get("time").getAsInt());
        }
        @Override public BoilerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf b) {
            Fluid input = ForgeRegistries.FLUIDS.getValue(b.readResourceLocation());
            if (input == null) throw new IllegalArgumentException("Unknown boiler fluid");
            return new BoilerRecipe(id, input, b.readVarInt(), b.readVarInt(), b.readVarInt());
        }
        @Override public void toNetwork(FriendlyByteBuf b, BoilerRecipe r) {
            b.writeResourceLocation(ForgeRegistries.FLUIDS.getKey(r.input));
            b.writeVarInt(r.inputAmount); b.writeVarInt(r.outputAmount); b.writeVarInt(r.time);
        }
    }
}
