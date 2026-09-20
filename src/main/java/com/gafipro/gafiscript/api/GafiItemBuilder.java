package com.gafipro.gafiscript.api;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class GafiItemBuilder {
    private final ItemStack stack;

    private GafiItemBuilder(String itemId, int count) {
        Item item = Registries.ITEM.get(
                Identifier.of(itemId)
        );

        if (item == null) {
            throw new IllegalArgumentException(
                    "Unknown item: " + itemId
            );
        }

        this.stack = new ItemStack(
                item,
                Math.max(1, Math.min(count, 99))
        );
    }

    public static GafiItemBuilder of(String itemId) {
        return new GafiItemBuilder(itemId, 1);
    }

    public GafiItemBuilder count(int count) {
        stack.setCount(
                Math.max(
                        1,
                        Math.min(
                                count,
                                stack.getMaxCount()
                        )
                )
        );
        return this;
    }

    public GafiItemBuilder name(String name) {
        return name(Text.literal(name));
    }

    public GafiItemBuilder name(Text name) {
        stack.set(
                DataComponentTypes.CUSTOM_NAME,
                name
        );
        return this;
    }

    public GafiItemBuilder itemName(Text name) {
        stack.set(
                DataComponentTypes.ITEM_NAME,
                name
        );
        return this;
    }

    public GafiItemBuilder lore(String... lines) {
        List<Text> text =
                new ArrayList<>(
                        lines == null
                                ? List.of()
                                : java.util.Arrays.stream(lines)
                                        .map(Text::literal)
                                        .toList()
                );

        stack.set(
                DataComponentTypes.LORE,
                new LoreComponent(text)
        );

        return this;
    }

    public GafiItemBuilder lore(List<Text> lines) {
        stack.set(
                DataComponentTypes.LORE,
                new LoreComponent(
                        List.copyOf(
                                lines == null
                                        ? List.of()
                                        : lines
                        )
                )
        );

        return this;
    }

    public GafiItemBuilder customModelData(float value) {
        stack.set(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                new CustomModelDataComponent(
                        List.of(value),
                        List.of(),
                        List.of(),
                        List.of()
                )
        );

        return this;
    }

    public GafiItemBuilder maxStackSize(int size) {
        stack.set(
                DataComponentTypes.MAX_STACK_SIZE,
                Math.max(1, Math.min(size, 99))
        );
        return this;
    }

    public GafiItemBuilder glint(boolean enabled) {
        if (enabled) {
            stack.set(
                    DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
                    true
            );
        } else {
            stack.remove(
                    DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE
            );
        }

        return this;
    }

    public GafiItemBuilder component(
            net.minecraft.component.ComponentType<?> type,
            Object value
    ) {
        setUnchecked(stack, type, value);
        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setUnchecked(
            ItemStack stack,
            net.minecraft.component.ComponentType type,
            Object value
    ) {
        stack.set(type, value);
    }

    public ItemStack build() {
        return stack.copy();
    }
}
