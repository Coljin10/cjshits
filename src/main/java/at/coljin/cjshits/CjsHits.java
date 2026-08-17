package at.coljin.cjshits;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Gemeinsamer Einstiegspunkt(Client + Server). Registriert den Admin-Reset-Command. */
public class CjsHits implements ModInitializer {
    public static final String MOD_ID = "cjshits";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("CJ's hits geladen.");
        // /cjshits reset  – setzt den komplettenSpielstand zurück.
        // (In Einzelspieler durch den Cheats-Schalter geschützt; strikte Op-Gate-API in 26.2 noch im Umbau.)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                Commands.literal("cjshits")
                    .then(Commands.literal("reset").executes(ctx -> {
                        GameState.I.komplettReset();
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("CJ's hits: Spielstand komplett zurückgesetzt (inkl. Legacy & Sammlung)."), true);
                        return 1;
                    }))
            )
        );
    }
}
