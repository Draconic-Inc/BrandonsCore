package com.brandon3055.brandonscore.blocks.properties;

import com.brandon3055.brandonscore.utils.ArrayUtils;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.properties.PropertyHelper;

import java.util.*;

/**
 * Created by covers1624 on 6/2/2016.
 * Tweaked by brandon3055 on 31/3/2016.
 */
public class PropertyString extends PropertyHelper<String> {

    private final List<String> valuesSet;

    public PropertyString(String name, String... values) {
        super(name, String.class);
        valuesSet = new ArrayList<String>();
        Collections.addAll(valuesSet, ArrayUtils.arrayToLowercase(values));
    }

    @Override
    public Collection<String> getAllowedValues() {
        return ImmutableSet.copyOf(valuesSet);
    }

    @Override
    public Optional<String> parseValue(String value) {
        if (valuesSet.contains(value)) {
            return Optional.of(value);
        }
        return Optional.absent();
    }

    @Override
    public String getName(String value) {
        return value;
    }

    public int toMeta(String value) {
        return valuesSet.contains(value) ? valuesSet.indexOf(value) : 0;
    }

    public String fromMeta(int meta) {
        if (meta >= 0 && meta < valuesSet.size()) {
            return valuesSet.get(meta);
        }
        LogHelperBC.error("[PropertyString] Attempted to load property for invalid meta value");
        return valuesSet.get(0);
    }
}
