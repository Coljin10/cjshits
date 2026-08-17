package at.coljin.cjshits.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.coljin.cjshits.GameState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Nach jeder bezahlten Rechnung: wähle 1 aus 3 Perks (dauerhaft bis Bankrott). */
public class PerkScreen extends Screen {

    private final GameState st = GameState.I;
    private final Screen zurueck;
    private final int[] auswahl = new int[3];

    public PerkScreen(Screen zurueck) {
        super(Component.literal("Perk wählen"));
        this.zurueck = zurueck;
        List<Integer> pool = new ArrayList<>();
        for (int i = 0; i < GameState.PERKS.length; i++) pool.add(i);
        Collections.shuffle(pool);
        for (int i = 0; i < 3; i++) auswahl[i] = pool.get(i);
    }

    private int cw(){ return Math.min(220, (this.width-80)/3); }
    private int gap(){ return 24; }
    private int x0(){ return (this.width - (cw()*3 + gap()*2))/2; }

    @Override
    protected void init() {
        int y = this.height/2 + 42;
        for (int i = 0; i < 3; i++) { final int idx = auswahl[i]; int bx = x0() + i*(cw()+gap());
            addRenderableWidget(Button.builder(Component.literal("Wählen"), b -> { st.perkWaehlen(idx); zurueck(); })
                    .bounds(bx + cw()/2 - 60, y, 120, 20).build()); }
    }

    private void zurueck() { if (minecraft != null) minecraft.gui.setScreen(zurueck); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        Ui.center(g, this.font, "Rechnung bezahlt – wähle einen Perk!", this.width/2, this.height/2 - 84, 0xFFF0D060);
        int y = this.height/2 - 70;
        for (int i = 0; i < 3; i++) { GameState.Perk p = GameState.PERKS[auswahl[i]]; int bx = x0() + i*(cw()+gap());
            g.fill(bx, y, bx + cw(), y + 96, 0xFF201814);
            g.fill(bx, y, bx + cw(), y + 2, 0xFF6B4A34);
            Ui.center(g, this.font, p.name, bx + cw()/2, y + 18, 0xFFE0C060);
            Ui.center(g, this.font, p.beschr, bx + cw()/2, y + 48, 0xFFCFCFCF); }
    }

    public void renderBackground(GuiGraphicsExtractor g, int mx, int my, float dt) { Ui.desk(g, this.width, this.height); }

    @Override public boolean isPauseScreen() { return false; }
}
