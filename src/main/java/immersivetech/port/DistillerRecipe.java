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

public final class DistillerRecipe implements Recipe<Container> {
    public static final Serializer SERIALIZER = new Serializer();
    public static final RecipeType<DistillerRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return ImmersiveTech.MOD_ID + ":distiller";
        }
    };

    private final ResourceLocation id;
    private final Fluid input;
    private final int inputAmount;
    private final Fluid output;
    private final int outputAmount;
    private final int energy;
    private final int time;
    private final float saltChance;

    public DistillerRecipe(ResourceLocation id, Fluid input, int inputAmount, Fluid output, int outputAmount,
            int energy, int time, float saltChance) {
        this.id = id;
        this.input = input;
        this.inputAmount = inputAmount;
        this.output = output;
        this.outputAmount = outputAmount;
        this.energy = energy;
        this.time = time;
        this.saltChance = saltChance;
    }

    public Fluid input() { return input; }
    public int inputAmount() { return inputAmount; }
    public Fluid output() { return output; }
    public int outputAmount() { return outputAmount; }
    public int energy() { return energy; }
    public int time() { return time; }
    public float saltChance() { return saltChance; }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    public static final class Serializer implements RecipeSerializer<DistillerRecipe> {
        @Override
        public DistillerRecipe fromJson(ResourceLocation id, JsonObject json) {
            ResourceLocation inputId = new ResourceLocation(json.get("input_fluid").getAsString());
            ResourceLocation outputId = new ResourceLocation(json.get("output_fluid").getAsString());
            Fluid input = Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(inputId),
                    "Unknown distiller input fluid " + inputId);
            Fluid output = Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(outputId),
                    "Unknown distiller output fluid " + outputId);
            return new DistillerRecipe(id, input, json.get("input_amount").getAsInt(), output,
                    json.get("output_amount").getAsInt(), json.get("energy").getAsInt(),
                    json.get("time").getAsInt(), json.has("salt_chance") ? json.get("salt_chance").getAsFloat() : 0);
        }

        @Override
        public DistillerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Fluid input = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
            Fluid output = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
            if (input == null || output == null) throw new IllegalArgumentException("Unknown distiller fluid");
            return new DistillerRecipe(id, input, buffer.readVarInt(), output, buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readFloat());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DistillerRecipe recipe) {
            buffer.writeResourceLocation(ForgeRegistries.FLUIDS.getKey(recipe.input));
            buffer.writeResourceLocation(ForgeRegistries.FLUIDS.getKey(recipe.output));
            buffer.writeVarInt(recipe.inputAmount);
            buffer.writeVarInt(recipe.outputAmount);
            buffer.writeVarInt(recipe.energy);
            buffer.writeVarInt(recipe.time);
            buffer.writeFloat(recipe.saltChance);
        }
    }
}
