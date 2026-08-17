package at.coljin.cjshits.client;

import java.util.Random;

import at.coljin.cjshits.GameState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Auswertung: Zinsen, Anteil, Beute, Sparschwein-Statistik, Rechnung zahlen (→ Perk), Doppelt oder nix. */
public class MuedeHandScreen extends Screen {

    private static final String[] MUENZ = {"coin_silber","coin_gold","coin_gruen","coin_rot","coin_blau"};
    private final GameState st = GameState.I;
    private final Random rng = new Random();

    private final long ertrag;
    private final int genauigkeit, beute, zerstoert;
    private final int[] muenzen, artZerstoert;
    private boolean doppeltGenutzt = false, pendingWin = false;
    private int doppeltErgebnis = 0, doppeltAnim = 0;

    public MuedeHandScreen(long ertrag, int genauigkeit, int beute, int[] muenzen, int zerstoert, int[] artZerstoert) {
        super(Component.literal("Müde Hand!"));
        this.ertrag=ertrag; this.genauigkeit=genauigkeit; this.beute=beute;
        this.muenzen=muenzen; this.zerstoert=zerstoert; this.artZerstoert=artZerstoert;
        st.bankLauf(ertrag);
    }

    private int cx(){ return this.width/2; }

    @Override
    protected void init() {
        int gap=8, bw=Math.min(150,(this.width-40)/4-gap), total=bw*4+gap*3, x=(this.width-total)/2, by=this.height-34;
        addRenderableWidget(Button.builder(Component.literal("Verbesserungen"), b -> open(3)).bounds(x, by, bw, 20).build());
        String rl = st.kannRechnungZahlen() ? "Rechnung "+st.rechnungBetrag+" Taler" : st.rechnungBetrag+" Taler – Tag "+st.rechnungFaellig;
        addRenderableWidget(Button.builder(Component.literal(rl), b -> open(0)).bounds(x+(bw+gap), by, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Weiter"), b -> weiter()).bounds(x+2*(bw+gap), by, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Doppelt oder nix"), b -> doppelt()).bounds(x+3*(bw+gap), by, bw, 20).build());
    }

    private void open(int tab){ if(minecraft!=null) minecraft.gui.setScreen(new MenuScreen(tab)); }
    private void weiter(){ st.tagWeiter(); st.save(); if(minecraft!=null) minecraft.gui.setScreen(new TischScreen()); }
    private void doppelt(){
        if (doppeltGenutzt) return; doppeltGenutzt=true;
        pendingWin = rng.nextBoolean(); doppeltAnim = 34;   // erst Münzwurf, Ergebnis danach
    }

    @Override public void tick(){
        super.tick();
        if (doppeltAnim>0 && --doppeltAnim==0){
            if (pendingWin){ st.geld+=st.letzterAnteil; doppeltErgebnis=1; } else { st.geld=Math.max(0,st.geld-st.letzterAnteil); doppeltErgebnis=-1; }
            st.save();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        Ui.center(g, this.font, "MÜDE HAND!", cx(), 30, 0xFFFFFFFF);

        int pw=Math.min(300, this.width/2-30), lx=cx()-pw-8, rx=cx()+8, py=60, ph=200;
        // Links
        g.fill(lx,py,lx+pw,py+ph,0xFF141010);
        int y=py+14;
        zeile(g,lx+16,y,pw,"Genauigkeit:",genauigkeit+"%"); y+=22;
        zeile(g,lx+16,y,pw,"Beute:",""+beute); y+=20;
        int step=(pw-30)/5, cxp=lx+16;
        for (int i=0;i<5;i++){ Ui.imgS(g,MUENZ[i],cxp,y,14,14,24,24); Ui.text(g,this.font,""+muenzen[i],cxp+16,y+3,0xFFCFCFCF); cxp+=step; }
        y+=26;
        zeile(g,lx+16,y,pw,"Gesamt:",ertrag+" Taler"); y+=20;
        zeile(g,lx+16,y,pw,"Zinsen:","+"+st.letzteZinsen+" Taler"); y+=18;
        if (st.letzterToniAbzug>0){ zeile(g,lx+16,y,pw,"Big Toni:","-"+st.letzterToniAbzug+" Taler"); y+=18; }
        if (st.letzterInkasso>0){ zeile(g,lx+16,y,pw,"Inkasso:","-"+st.letzterInkasso+" Taler"); y+=18; }
        Ui.text(g,this.font,"Dein Anteil:",lx+16,y,0xFF8CFF8C);
        String an=st.letzterAnteil+" Taler"; Ui.text(g,this.font,an,lx+pw-16-Ui.w(this.font,an),y,0xFF8CFF8C);
        // Rechts
        g.fill(rx,py,rx+pw,py+ph,0xFF141010);
        Ui.text(g,this.font,"Sparschweine zerstört: "+zerstoert,rx+16,py+14,0xFFFFFFFF);
        int sx=rx+16, sy=py+40;
        for (int i=0;i<GameState.ARTEN.length;i++){ if (artZerstoert[i]==0) continue;
            Ui.imgS(g,GameState.ARTEN[i].tex,sx,sy,32,28,128,112);
            Ui.text(g,this.font,""+artZerstoert[i],sx+32,sy+10,0xFFCFCFCF);
            sx+=Math.max(70,pw/3); if (sx>rx+pw-50){ sx=rx+16; sy+=34; } }
        Ui.text(g,this.font,"Legacy: "+st.legacy,rx+16,py+ph-18,0xFFB0C8F0);

        if (doppeltAnim>0){
            int cw=(int)(Math.abs(Math.cos(doppeltAnim*0.55))*44)+4;
            Ui.imgS(g,"coin_gold",cx()-cw/2,this.height/2-22,cw,44,24,24);
            Ui.center(g,this.font,"Doppelt oder nix ...",cx(),this.height/2+30,0xFFF0D060);
        } else if (doppeltErgebnis==1) Ui.center(g,this.font,"GEWONNEN!  +"+st.letzterAnteil+" Taler",cx(),this.height-54,0xFF8CFF8C);
        else if (doppeltErgebnis==-1) Ui.center(g,this.font,"VERLOREN!  -"+st.letzterAnteil+" Taler",cx(),this.height-54,0xFFE06060);
    }

    private void zeile(GuiGraphicsExtractor g,int x,int y,int pw,String label,String wert){
        Ui.text(g,this.font,label,x,y,0xFFE0C88A);
        Ui.text(g,this.font,wert,x+pw-16-Ui.w(this.font,wert),y,0xFFFFFFFF);
    }

    public void renderBackground(GuiGraphicsExtractor g, int mx, int my, float dt) { Ui.desk(g, this.width, this.height); }

    @Override public boolean isPauseScreen() { return false; }
}
