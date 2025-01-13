package dev.marston.randomloot.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;
import java.util.Objects;


public class ToolModifier {

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != ToolModifier.class) {
            return false;
        }
        ToolModifier t = (ToolModifier) obj;

        return t.tags.equals(this.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags);
    }

    Map<String, CompoundTag> tags;

    public Map<String, CompoundTag> getTags() {
        return this.tags;
    }

    public ToolModifier(Map<String, CompoundTag> tagIn) {
        this.tags = tagIn;
    }

    public static final Codec<ToolModifier> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                            Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC).fieldOf("tags").forGetter(ToolModifier::getTags)
                    )
                    .apply(builder, ToolModifier::new)
    );





}
