package at.coljin.cjshits.client;

import at.coljin.cjshits.GameState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Big-Toni-Anruf: das Klapp-Handy fährt hoch/klappt auf, dann Angebot + Nehmen/Ablehnen. */
public class ToniScreen extends Screen {

    private final GameState st = GameState.I;
    private final Screen zurueck;
    private int anim = 0;

    public ToniScreen(Screen zurueck) { super(Component.literal("Big Toni")); this.zurueck = zurueck; }

    @Override
    protected void init() {
        int by = this.height - 56, bw = Math.min(180, (this.width - 80) / 2 - 20);
        addRenderableWidget(Button.builder(Component.literal("Nehmen (" + st.toniAngebot() + " Taler)"), b -> { st.toniLeihen(); zurueck(); })
                .bounds(this.width / 2 - bw - 10, by, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Ablehnen"), b -> zurueck())
                .bounds(this.width / 2 + 10, by, bw, 20).build());
    }

    private void zurueck() { if (minecraft != null) minecraft.gui.setScreen(zurueck != null ? zurueck : new MenuScreen(0)); }

    @Override public void tick() { super.tick(); if (anim < 14) anim++; }

    public void renderBackground(GuiGraphicsExtractor g, int mx, int my, float dt) { Ui.desk(g, this.width, this.height); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        Ui.center(g, this.font, "Anruf – Big Toni", this.width / 2, 26, 0xFFF0D060);

        float gp = Math.min(1f, (anim + delta) / 12f);                 // Handy klappt/fährt hoch
        int phFull = (int) (this.height * 0.52f);
        int ph = (int) (phFull * (0.30f + 0.70f * gp)), pw = ph * 56 / 84;
        int px = this.width - pw - 60, py = this.height / 2 - ph / 2;
        Ui.imgS(g, "prop_telefon", px, py, pw, ph, 56, 84);

        if (gp >= 1f) {
            int bx = 60, bw2 = Math.min(470, px - 90), by = this.height / 2 - 74;
            g.fill(bx, by, bx + bw2, by + 150, 0xFF120C08);
            g.fill(bx, by, bx + bw2, by + 2, 0xFF6B4A34);
            Ui.text(g, this.font, "Big Toni", bx + 14, by + 12, 0xFFE0C060);
            Ui.text(g, this.font, "\"Brauchst du wieder Kohle? Ich helf dir aus.", bx + 14, by + 34, 0xFFDDDDDD);
            Ui.text(g, this.font, "Aber du zahlst jeden Cent zurück, kapiert?\"", bx + 14, by + 48, 0xFFDDDDDD);
            Ui.text(g, this.font, "Kredit: " + st.toniAngebot() + " Taler (10% Zins).", bx + 14, by + 78, 0xFFCFCFCF);
            Ui.text(g, this.font, "Toni kassiert danach täglich einen Anteil", bx + 14, by + 96, 0xFFCFCFCF);
            Ui.text(g, this.font, "deiner Einnahmen, bis alles abbezahlt ist.", bx + 14, by + 110, 0xFFCFCFCF);
        } else {
            Ui.center(g, this.font, "verbinde ...", this.width / 2, this.height / 2 + 8, 0xFF909090);
        }
    }

    @Override public boolean isPauseScreen() { return false; }
}
