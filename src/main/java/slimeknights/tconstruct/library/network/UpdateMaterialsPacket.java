package slimeknights.tconstruct.library.network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.library.MaterialRegistry;
import slimeknights.tconstruct.library.TinkerRegistries;
import slimeknights.tconstruct.library.materials.IMaterial;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class UpdateMaterialsPacket implements IThreadsafePacket {
  private final Collection<IMaterial> materials;
  public UpdateMaterialsPacket(PacketBuffer buffer) {
    int materialCount = buffer.readInt();
    this.materials = new ArrayList<>(materialCount);

    for (int i = 0; i < materialCount; i++) {
      MaterialId id = new MaterialId(buffer.readResourceLocation());
      boolean craftable = buffer.readBoolean();
      ResourceLocation fluidId = buffer.readResourceLocation();
      Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
      if (fluid == null) {
        fluid = Fluids.EMPTY;
      }
      String textColor = buffer.readString();
      int temperature = buffer.readInt();
      ModifierEntry trait = null;
      if (buffer.readBoolean()) {
        trait = new ModifierEntry(buffer.readRegistryIdUnsafe(TinkerRegistries.MODIFIERS), buffer.readVarInt());
      }
      this.materials.add(new Material(id, fluid, craftable, textColor, temperature, trait));
    }
  }

  @Override
  public void encode(PacketBuffer buffer) {
    buffer.writeInt(this.materials.size());
    this.materials.forEach(material -> {
      buffer.writeResourceLocation(material.getIdentifier());
      buffer.writeBoolean(material.isCraftable());
      buffer.writeResourceLocation(Objects.requireNonNull(material.getFluid().getRegistryName()));
      buffer.writeString(material.getTextColor());
      buffer.writeInt(material.getTemperature());
      ModifierEntry trait = material.getTrait();
      if (trait == null) {
        buffer.writeBoolean(false);
      } else {
        buffer.writeBoolean(true);
        buffer.writeRegistryIdUnsafe(TinkerRegistries.MODIFIERS, trait.getModifier());
        buffer.writeVarInt(trait.getLevel());
      }
    });
  }

  @Override
  public void handleThreadsafe(Context context) {
    MaterialRegistry.updateMaterialsFromServer(this);
  }
}
