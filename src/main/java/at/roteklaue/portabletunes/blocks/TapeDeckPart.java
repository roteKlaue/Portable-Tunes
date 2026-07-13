package at.roteklaue.portabletunes.blocks;

import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;

public enum TapeDeckPart implements StringRepresentable {
    LEFT("left"),
    RIGHT("right");

    private final String serializedName;

    TapeDeckPart(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    @Nonnull
    public String getSerializedName() {
        return serializedName;
    }
}
