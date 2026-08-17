package at.coljin.cjshits.client;

import at.coljin.cjshits.GameState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Öffnet sich EINMALIG beim Bankrott: Legacy in Ringe & Armbänder investieren, dann besiegeln. */
public class PrestigeScreen extends Screen {

    private final GameState st = GameState.I;

    public PrestigeScreen() { super(Component.literal("Prestige")); }

    private int cx(){ return this.width/2; }
    private int[] pos(int i){ int cw=Math.min(340,(this.width-80)/3), sx=cx()-3*cw/2; return new int[]{ sx+(i%3)*cw, 120+(i/3)*130 }; }
    private static boolean hover(int mx,int my,int x,int y,int w,int h){ return mx>=x&&mx<=x+w&&my>=y&&my<=y+h; }
    private void reopen(){ if(minecraft!=null) minecraft.gui.setScreen(new PrestigeScreen()); }

    public void renderBackground(GuiGraphicsExtractor g, int mx, int my, float dt) { Ui.desk(g, this.width, this.height); }

    @Override
    protected void init() {
        for (int i=0;i<GameState.PRESTIGE.length;i++){ final int idx=i; int[] p=pos(i);
            String lbl = st.prestigeBesitz[i] ? "Im Besitz" : GameState.PRESTIGE[i].kosten+" Legacy";
            addRenderableWidget(Button.builder(Component.literal(lbl), b -> { st.kaufePrestige(idx); st.save(); reopen(); })
                    .bounds(p[0], p[1]+52, Math.min(200,(this.width-80)/3-16), 18).build()); }
        int bw=Math.min(240,(this.width-80)/2-20);
        addRenderableWidget(Button.builder(Component.literal("Abbrechen"), b -> { if(minecraft!=null) minecraft.gui.setScreen(new MenuScreen(0)); })
                .bounds(cx()-bw-10, this.height-30, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Bankrott besiegeln & neu starten"), b -> {
                st.bankrott(); st.save(); if(minecraft!=null) minecraft.gui.setScreen(new TischScreen()); })
                .bounds(cx()+10, this.height-30, bw, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.fill(30, 40, this.width-30, this.height-38, 0xF01A140E);
        Ui.center(g, this.font, "BANKROTT – Legacy investieren", cx(), 20, 0xFFF0C060);
        Ui.center(g, this.font, "Ringe & Armbänder bleiben dauerhaft. Übrige Legacy bleibt dir erhalten.", cx(), 38, 0xFFB0B0C0);
        Ui.center(g, this.font, "Verfügbare Legacy: " + st.legacy, cx(), 58, 0xFFB0C8F0);
        int hoverI=-1;
        for (int i=0;i<GameState.PRESTIGE.length;i++){ var pr=GameState.PRESTIGE[i]; int[] p=pos(i);
            Ui.img(g, pr.tex, p[0], p[1], 16, 16);
            Ui.text(g, this.font, pr.name, p[0]+22, p[1]+2, st.prestigeBesitz[i]?0xFFE0C060:0xFFFFFFFF);
            Ui.text(g, this.font, pr.beschr, p[0], p[1]+24, 0xFFC8C8C8);
            if (hover(mouseX,mouseY,p[0],p[1],220,48)) hoverI=i; }
        if (hoverI>=0){ var pr=GameState.PRESTIGE[hoverI];
            Ui.tooltip(g, this.font, new String[]{pr.name, pr.beschr, st.prestigeBesitz[hoverI]?"Im Besitz":"Kosten: "+pr.kosten+" Legacy"}, mouseX, mouseY, this.width, this.height); }
    }

    @Override public boolean isPauseScreen() { return false; }
}
