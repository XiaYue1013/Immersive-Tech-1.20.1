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

public final class SolarTowerRecipe implements Recipe<Container> {
    public static final Serializer SERIALIZER = new Serializer();
    public static final RecipeType<SolarTowerRecipe> TYPE = new RecipeType<>() {
        @Override public String toString() { return ImmersiveTech.MOD_ID + ":solar_tower"; }
    };

    private final ResourceLocation id;
    private final Fluid input;
    private final int inputAmount;
    private final int outputAmount;
    private final int time;

    public SolarTowerRecipe(ResourceLocation id, Fluid input, int inputAmount, int outputAmount, int time) {
        this.id = id;
        this.input = input;
        this.inputAmount = inputAmount;
        this.outputAmount = outputAmount;
        this.time = time;
    }
    public Fluid input() { return input; }
    public int inputAmount() { return inputAmount; }
    public int outputAmount() { return outputAmount; }
    public int time() { return time; }
    @Override public boolean matches(Container container, Level level) { return false; }
    @Override public ItemStack assemble(Container container, RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return SERIALIZER; }
    @Override public RecipeType<?> getType() { return TYPE; }

    public static final class Serializer implements RecipeSerializer<SolarTowerRecipe> {
        @Override public SolarTowerRecipe fromJson(ResourceLocation id, JsonObject json) {
            ResourceLocation inputId = new ResourceLocation(json.get("input_fluid").getAsString());
            Fluid input = Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(inputId),
                    "Unknown solar tower input fluid " + inputId);
            return new SolarTowerRecipe(id, input, json.get("input_amount").getAsInt(),
                    json.get("output_amount").getAsInt(), json.get("time").getAsInt());
        }
        @Override public SolarTowerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Fluid input = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
            if (input == null) throw new IllegalArgumentException("Unknown solar tower fluid");
            return new SolarTowerRecipe(id, input, buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
        }
        @Override public void toNetwork(FriendlyByteBuf buffer, SolarTowerRecipe recipe) {
            buffer.writeResourceLocation(ForgeRegistries.FLUIDS.getKey(recipe.input));
            buffer.writeVarInt(recipe.inputAmount);
            buffer.writeVarInt(recipe.outputAmount);
            buffer.writeVarInt(recipe.time);
        }
    }
}
