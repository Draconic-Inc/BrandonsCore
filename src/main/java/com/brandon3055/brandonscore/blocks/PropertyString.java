package com.brandon3055.brandonscore.blocks;

import codechicken.lib.math.MathHelper;
import codechicken.lib.util.ArrayUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import net.minecraft.state.Property;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by covers1624 on 2/6/2016.
 */
@Deprecated //Move to enums.
public class PropertyString extends Property<String> {

    private final Set<String> valuesSet;
    private final String[] metaLookup;

    public PropertyString(String name, Collection<String> values) {
        super(name, String.class);
        metaLookup = values.stream().map(String::intern).toArray(String[]::new);
        valuesSet = new HashSet<>();
        Collections.addAll(valuesSet, metaLookup);
    }

    public PropertyString(String name, String... values) {
        super(name, String.class);
        metaLookup = Arrays.stream(values).map(String::intern).toArray(String[]::new);
        valuesSet = new HashSet<>();
        Collections.addAll(valuesSet, metaLookup);
    }

    public List<String> values() {
        return Lists.newArrayList(metaLookup);
    }

    @Nonnull
    @Override
    public Collection<String> getPossibleValues() {
        return Collections.unmodifiableSet(valuesSet);
    }

    @Nonnull
    @Override
    public Optional<String> getValue(@Nonnull String value) {
        if (valuesSet.contains(value.intern())) {
            return Optional.of(value.intern());
        }
        return Optional.empty();
    }

    @Nonnull
    @Override
    public String getName(@Nonnull String value) {
        return value.intern();
    }

    public int toMeta(String value) {
        return ArrayUtils.indexOf(metaLookup, value.intern());
    }

    public String fromMeta(int meta) {
        if (!MathHelper.between(0, meta, metaLookup.length)) {
            throw new IllegalArgumentException(String.format("Meta data out of bounds. Meta: %s, Lookup: %s.", meta, Joiner.on(",").join(metaLookup)));
        }
        return metaLookup[meta];
    }
}

