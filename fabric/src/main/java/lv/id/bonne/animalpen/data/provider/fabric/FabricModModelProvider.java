package lv.id.bonne.animalpen.data.provider.fabric;


import org.jetbrains.annotations.NotNull;
import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.blocks.AquariumBlock;
import lv.id.bonne.animalpen.blocks.AviaryBlock;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.Condition;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;


public class FabricModModelProvider extends FabricModelProvider
{
    public FabricModModelProvider(FabricDataOutput generator)
    {
        super(generator);
    }


    @Override
    public void generateBlockStateModels(BlockModelGenerators generator)
    {
        this.generateAnimalPen(generator);
        this.generateAquarium(generator);
        this.generateAviary(generator);
    }


    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator)
    {
        itemModelGenerator.generateFlatItem(AnimalPenBlockRegistry.AQUARIUM.get().asItem(),
            new ModelTemplate(Optional.of(AnimalPen.resourceOf("block/aquarium/base")),
                Optional.of("parent")));

        itemModelGenerator.generateFlatItem(AnimalPenBlockRegistry.AVIARY.get().asItem(),
            new ModelTemplate(Optional.of(AnimalPen.resourceOf("block/aviary/iron")),
                Optional.of("parent")));
        itemModelGenerator.generateFlatItem(AnimalPenBlockRegistry.GOLD_AVIARY.get().asItem(),
            new ModelTemplate(Optional.of(AnimalPen.resourceOf("block/aviary/gold")),
                Optional.of("parent")));

        for (WeatheringCopper.WeatherState state : WeatheringCopper.WeatherState.values())
        {
            itemModelGenerator.generateFlatItem(AnimalPenBlockRegistry.COPPER_AVIARIES.get(state).get().asItem(),
                new ModelTemplate(Optional.of(AnimalPen.resourceOf("block/aviary/copper_" + state.name().toLowerCase())),
                    Optional.of("parent")));
            itemModelGenerator.generateFlatItem(AnimalPenBlockRegistry.WAXED_COPPER_AVIARIES.get(state).get().asItem(),
                new ModelTemplate(Optional.of(AnimalPen.resourceOf("block/aviary/copper_" + state.name().toLowerCase())),
                    Optional.of("parent")));
        }

        AnimalPenBlockRegistry.ANIMAL_PENS.forEach((wood, block) ->
        {
            itemModelGenerator.generateFlatItem(block.get().asItem(),
                new ModelTemplate(Optional.of(AnimalPen.resourceOf("block/animal_pen/" + wood.name())),
                    Optional.of("parent")));
        });
    }


    private void generateAnimalPen(@NotNull BlockModelGenerators generator)
    {
        AnimalPenBlockRegistry.ANIMAL_PENS.forEach((wood, block) -> {
            this.generateAnimalPen(generator,
                block.get(),
                AnimalPen.resourceOf(  "block/animal_pen/" + wood.name()));
        });
    }


    private void generateAnimalPen(@NotNull BlockModelGenerators generator,
        Block block,
        ResourceLocation modelLocation)
    {
        ResourceLocation farmModel = AnimalPen.resourceOf( "block/animal_pen/decoration/farm");
        ResourceLocation hayModel = AnimalPen.resourceOf(  "block/animal_pen/decoration/hay");

        MultiPartGenerator multipart = MultiPartGenerator.multiPart(block);

        Direction.Plane.HORIZONTAL.forEach(direction ->
        {
            VariantProperties.Rotation rotation = rotationTranslate(direction);

            multipart.with(
                Condition.condition().
                    term(AnimalPenBlock.FACING, direction),
                Variant.variant().
                    with(VariantProperties.MODEL, modelLocation).
                    with(VariantProperties.Y_ROT, rotation)
            );

            // Decorations
            multipart.with(Condition.condition().
                    term(AnimalPenBlock.FACING, direction).
                    term(AnimalPenBlock.DECORATION, AnimalPenBlock.Decoration.FARM),
                Variant.variant().
                    with(VariantProperties.MODEL, farmModel).
                    with(VariantProperties.Y_ROT, rotationTranslate(direction)));

            multipart.with(Condition.condition().
                    term(AnimalPenBlock.FACING, direction).
                    term(AnimalPenBlock.DECORATION, AnimalPenBlock.Decoration.HAYBALE),
                Variant.variant().
                    with(VariantProperties.MODEL, hayModel).
                    with(VariantProperties.Y_ROT, rotationTranslate(direction)));
        });

        generator.blockStateOutput.accept(multipart);
    }


    private void generateAquarium(@NotNull BlockModelGenerators generator)
    {
        ResourceLocation baseModel = AnimalPen.resourceOf( "block/aquarium/base");

        ResourceLocation waterModel = AnimalPen.resourceOf( "block/aquarium/decoration/water");
        ResourceLocation caveModel = AnimalPen.resourceOf( "block/aquarium/decoration/cave");
        ResourceLocation shipwreckModel = AnimalPen.resourceOf( "block/aquarium/decoration/shipwreck");
        ResourceLocation coralModel = AnimalPen.resourceOf(  "block/aquarium/decoration/coral");

        MultiPartGenerator multipart = MultiPartGenerator.multiPart(AnimalPenBlockRegistry.AQUARIUM.get());

        Direction.Plane.HORIZONTAL.forEach(direction ->
        {
            VariantProperties.Rotation rotation = rotationTranslate(direction);

            multipart.with(
                Condition.condition().
                    term(AquariumBlock.FACING, direction),
                Variant.variant().
                    with(VariantProperties.MODEL, baseModel).
                    with(VariantProperties.Y_ROT, rotation)
            );

            // Water
            multipart.with(
                Condition.condition().
                    term(AquariumBlock.FACING, direction).
                    term(AquariumBlock.FILLED, true),
                Variant.variant().
                    with(VariantProperties.MODEL, waterModel).
                    with(VariantProperties.Y_ROT, rotation)
            );

            // Decorations
            multipart.with(Condition.condition().
                    term(AquariumBlock.FACING, direction).
                    term(AquariumBlock.DECORATION, AquariumBlock.Decoration.CAVE),
                Variant.variant().
                    with(VariantProperties.MODEL, caveModel).
                    with(VariantProperties.Y_ROT, rotationTranslate(direction)));
            multipart.with(Condition.condition().
                    term(AquariumBlock.FACING, direction).
                    term(AquariumBlock.DECORATION, AquariumBlock.Decoration.SHIPWRECK),
                Variant.variant().
                    with(VariantProperties.MODEL, shipwreckModel).
                    with(VariantProperties.Y_ROT, rotationTranslate(direction)));
            multipart.with(Condition.condition().
                    term(AquariumBlock.FACING, direction).
                    term(AquariumBlock.DECORATION, AquariumBlock.Decoration.CORAL),
                Variant.variant().
                    with(VariantProperties.MODEL, coralModel).
                    with(VariantProperties.Y_ROT, rotationTranslate(direction)));
        });

        generator.blockStateOutput.accept(multipart);
    }


    private void generateAviary(@NotNull BlockModelGenerators generator)
    {
        this.generateAviary(generator,
            AnimalPenBlockRegistry.AVIARY.get(),
            AnimalPen.resourceOf(  "block/aviary/iron"));
        this.generateAviary(generator,
            AnimalPenBlockRegistry.GOLD_AVIARY.get(),
            AnimalPen.resourceOf(  "block/aviary/gold"));

        for (WeatheringCopper.WeatherState state : WeatheringCopper.WeatherState.values())
        {
            this.generateAviary(generator,
                AnimalPenBlockRegistry.COPPER_AVIARIES.get(state).get(),
                AnimalPen.resourceOf("block/aviary/copper_" + state.name().toLowerCase()));
            this.generateAviary(generator,
                AnimalPenBlockRegistry.WAXED_COPPER_AVIARIES.get(state).get(),
                AnimalPen.resourceOf("block/aviary/copper_" + state.name().toLowerCase()));
        }
    }


    private void generateAviary(@NotNull BlockModelGenerators generator, Block block, ResourceLocation modelLocation)
    {
        ResourceLocation hiveModel = AnimalPen.resourceOf( "block/aviary/decoration/hive");
        ResourceLocation nestModel = AnimalPen.resourceOf(  "block/aviary/decoration/nest");

        MultiPartGenerator multipart = MultiPartGenerator.multiPart(block);

        Direction.Plane.HORIZONTAL.forEach(direction ->
        {
            VariantProperties.Rotation rotation = rotationTranslate(direction);

            multipart.with(
                Condition.condition().
                    term(AviaryBlock.FACING, direction),
                Variant.variant().
                    with(VariantProperties.MODEL, modelLocation).
                    with(VariantProperties.Y_ROT, rotation)
            );

            // Decorations
            multipart.with(Condition.condition().
                    term(AviaryBlock.FACING, direction).
                    term(AviaryBlock.DECORATION, AviaryBlock.Decoration.HIVE),
                Variant.variant().
                    with(VariantProperties.MODEL, hiveModel).
                    with(VariantProperties.Y_ROT, rotationTranslate(direction)));

            multipart.with(Condition.condition().
                    term(AviaryBlock.FACING, direction).
                    term(AviaryBlock.DECORATION, AviaryBlock.Decoration.NEST),
                Variant.variant().
                    with(VariantProperties.MODEL, nestModel).
                    with(VariantProperties.Y_ROT, rotationTranslate(direction)));
        });

        generator.blockStateOutput.accept(multipart);
    }


    private static VariantProperties.Rotation rotationTranslate(@NotNull Direction direction)
    {
        return switch (direction)
        {
            case NORTH -> VariantProperties.Rotation.R0;
            case EAST -> VariantProperties.Rotation.R90;
            case SOUTH -> VariantProperties.Rotation.R180;
            case WEST -> VariantProperties.Rotation.R270;
            default -> throw new IllegalArgumentException(
                "Invalid aquarium direction: " + direction
            );
        };
    }
}
