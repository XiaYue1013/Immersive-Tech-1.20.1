package immersivetech.port;

import java.util.Objects;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class AdvancedCokeOvenRecipe implements Recipe<Container> {
    public static final Serializer SERIALIZER = new Serializer();
    public static final RecipeType<AdvancedCokeOvenRecipe> TYPE = new RecipeType<>() {};
    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final int time;
    private final int creosoteAmount;

    public AdvancedCokeOvenRecipe(ResourceLocation id, Ingredient input, ItemStack output, int time,
            int creosoteAmount) {
        this.id = id; this.input = input; this.output = output; this.time = time;
        this.creosoteAmount = creosoteAmount;
    }
    public ItemStack output() { return output; }
    public int time() { return time; }
    public int creosoteAmount() { return creosoteAmount; }
    @Override public boolean matches(Container container, Level level) { return input.test(container.getItem(0)); }
    @Override public ItemStack assemble(Container container, RegistryAccess registries) { return output.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess registries) { return output.copy(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return SERIALIZER; }
    @Override public RecipeType<?> getType() { return TYPE; }

    public static final class Serializer implements RecipeSerializer<AdvancedCokeOvenRecipe> {
        @Override public AdvancedCokeOvenRecipe fromJson(ResourceLocation id, JsonObject json) {
            ResourceLocation outputId = new ResourceLocation(json.get("output").getAsString());
            var item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(outputId),
                    "Unknown advanced coke oven output " + outputId);
            return new AdvancedCokeOvenRecipe(id, Ingredient.fromJson(json.get("input")),
                    new ItemStack(item, json.has("count") ? json.get("count").getAsInt() : 1),
                    json.get("time").getAsInt(),
                    json.has("creosote_amount") ? json.get("creosote_amount").getAsInt() : 0);
        }
        @Override public AdvancedCokeOvenRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new AdvancedCokeOvenRecipe(id, Ingredient.fromNetwork(buffer),
                    buffer.readItem(), buffer.readVarInt(), buffer.readVarInt());
        }
        @Override public void toNetwork(FriendlyByteBuf buffer, AdvancedCokeOvenRecipe recipe) {
            recipe.input.toNetwork(buffer); buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.time); buffer.writeVarInt(recipe.creosoteAmount);
        }
    }
}
