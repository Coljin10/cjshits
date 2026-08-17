package at.coljin.cjshits.client;

import at.coljin.cjshits.CjsHits;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class CjsHitsClient implements ClientModInitializer {

    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(CjsHits.MOD_ID, "tisch")
        );

        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.cjshits.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.gui != null) {
                    client.gui.setScreen(new TischScreen());
                }
            }
        });

        CjsHits.LOGGER.info("CJ's hits Client bereit – Taste H öffnet den Tisch.");
    }
}
