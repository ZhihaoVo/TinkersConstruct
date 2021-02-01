package slimeknights.tconstruct.tools.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.fluid.Fluids;
import slimeknights.tconstruct.library.data.GenericDataProvider;
import slimeknights.tconstruct.library.materials.IMaterial;
import slimeknights.tconstruct.library.materials.MaterialManager;
import slimeknights.tconstruct.library.materials.json.MaterialJson;
import slimeknights.tconstruct.library.materials.json.TraitJson;

public class MaterialDataProvider extends GenericDataProvider {

  public MaterialDataProvider(DataGenerator gen) {
    super(gen, MaterialManager.FOLDER, MaterialManager.GSON);
  }

  @Override
  public void act(DirectoryCache cache) {
    Materials.allMaterials.forEach(material -> saveThing(cache, material.getIdentifier(), convert(material)));
  }

  private MaterialJson convert(IMaterial material) {
    TraitJson[] traits = null;
    if (!material.getTraits().isEmpty()) {
      traits = material.getTraits().stream()
                       .map(entry -> new TraitJson(entry.getModifier().getId(), entry.getLevel()))
                       .toArray(TraitJson[]::new);
    }

    // if empty, no fluid, no temperature
    if (material.getFluid() == Fluids.EMPTY) {
      return new MaterialJson(material.isCraftable(), null, material.getTextColor(), null, traits);
    }
    return new MaterialJson(material.isCraftable(), material.getFluid().getRegistryName(), material.getTextColor(), material.getTemperature(), traits);
  }

  @Override
  public String getName() {
    return "TConstruct Materials";
  }
}
