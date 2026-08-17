package at.coljin.cjshits.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import at.coljin.cjshits.GameState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Ein Lauf. WICHTIG für flüssige Optik: Bewegung passiert im tick() (20 Hz), gezeichnet wird aber
 * mit Interpolation (prev + (cur-prev)*delta), damit alles bei hoher FPS geschmeidig läuft. Maybe für Server als Zusatzfunktion.
 */
public class TischScreen extends Screen {

    private static final int ANZAHL = 14;
    private static final int[] SHARD = {0xFFF098AC, 0xFFECCEB2, 0xFFB08454, 0xFFEEE0CE, 0xFFE05448, 0xFF9696A0, 0xFF7C808C, 0xFF96DC78};

    private final GameState st = GameState.I;
    private final Random rng = new Random();
    private final List<Schwein> schweine = new ArrayList<>();
    private final List<Floater> floaters = new ArrayList<>();
    private final List<Partikel> partikel = new ArrayList<>();

    private int ausdauer, tickCounter = 0, schlagNr = 0, steinCounter = 0, swingT = 0;
    private boolean laufend = true, beendet = false;

    private long ertrag = 0;
    private int beute = 0, zerstoert = 0, schwuenge = 0, treffer = 0;
    private final int[] muenzen = new int[5];
    private final int[] artZerstoert = new int[GameState.ARTEN.length];

    private float mausX, mausY;

    private static final class Schwein { float x, y, px, py, vx, vy; int artIndex, hp, maxHp, rage;
        void merken(){ px=x; py=y; } }
    private static final class Floater { static final int LIFE = 22; String text; int x; float y, py; int color, life = LIFE; }
    private static final class Partikel { float x, y, px, py, vx, vy; int life, maxLife, color, typ, phase, timer; String tex; }

    public TischScreen() { super(Component.literal("CJ's hits")); }

    @Override
    protected void init() {
        st.recompute();
        ausdauer = st.maxAusdauer;
        if (schweine.isEmpty()) for (int i = 0; i < ANZAHL; i++) schweine.add(neu());
    }

    private int[] tisch() { int wy=Math.max(28,(int)(this.height*0.15)); return new int[]{40, wy+10, this.width - 40, this.height - 30}; }

    private Schwein neu() {
        int[] t = tisch();
        Schwein s = new Schwein();
        s.artIndex = st.wuerfleArt(rng);
        int mx=(t[0]+t[2])/2, my=(t[1]+t[3])/2, spanX=(int)((t[2]-t[0])*0.30), spanY=(int)((t[3]-t[1])*0.30);
        s.x = mx + rng.nextInt(2*spanX+1) - spanX; s.y = my + rng.nextInt(2*spanY+1) - spanY;
        s.px = s.x; s.py = s.y;
        s.maxHp = GameState.ARTEN[s.artIndex].hp; s.hp = s.maxHp;
        float sp = GameState.ARTEN[s.artIndex].verhalten==1 ? 3.0f : 1.3f;
        s.vx = (rng.nextFloat()*2-1)*sp; s.vy = (rng.nextFloat()*2-1)*sp;
        return s;
    }

    @Override
    public void tick() {
        super.tick();
        if (swingT > 0) swingT--;
        for (Floater f : floaters) { f.py=f.y; f.y-=1.4f; f.life--; }
        floaters.removeIf(f -> f.life <= 0);
        int wyC = Math.max(28,(int)(this.height*0.15));
        for (Partikel p : partikel) {
            p.px=p.x; p.py=p.y;
            if (p.typ==1) {                                   // Münze: fliegen -> liegen -> zum Stapel am Fenster
                if (p.phase==0){ p.vy+=0.35f; p.x+=p.vx; p.y+=p.vy; if (--p.timer<=0){ p.phase=1; p.vx=0; p.vy=0; p.timer=45+rng.nextInt(35); } }
                else if (p.phase==1){ if (--p.timer<=0) p.phase=2; }
                else { float tx=this.width/2f, ty=wyC-4f, dx=tx-p.x, dy=ty-p.y, dd=(float)Math.sqrt(dx*dx+dy*dy);
                       if (dd<14) p.life=0; else { p.x+=dx/dd*9f; p.y+=dy/dd*9f; } }
            } else if (p.typ==2){ p.life--; }                 // Aufprall-Blitz
            else { p.vy+=0.35f; p.x+=p.vx; p.y+=p.vy; p.life--; }  // Splitter/Staub
        }
        partikel.removeIf(p -> p.life <= 0);

        int[] t = tisch();
        for (Schwein s : schweine) {
            s.merken();
            int v = GameState.ARTEN[s.artIndex].verhalten;
            if (s.rage>0) {                   // Taurus: wütend -> stürmt herum
                s.rage--;
                if (rng.nextInt(22)==0){ float a=rng.nextFloat()*6.2832f; s.vx=(float)Math.cos(a)*4.6f; s.vy=(float)Math.sin(a)*4.6f; }
            } else if (v==1) {                // Tourist: wandert stetig
                if (rng.nextInt(50)==0){ s.vx=(rng.nextFloat()*2-1)*1.9f; s.vy=(rng.nextFloat()*2-1)*1.9f; }
            } else if (v==2) {                // Woody: steht fast still
                if (rng.nextInt(110)==0){ s.vx=(rng.nextFloat()*2-1)*0.5f; s.vy=(rng.nextFloat()*2-1)*0.5f; }
                s.vx*=0.9f; s.vy*=0.9f;
            } else if (v==3) {                // El Loco: schnell & chaotisch
                if (rng.nextInt(11)==0){ s.vx=(rng.nextFloat()*2-1)*3.1f; s.vy=(rng.nextFloat()*2-1)*3.1f; }
            } else if (v==4) {                // Taurus (ruhig): faul
                if (rng.nextInt(120)==0){ s.vx=(rng.nextFloat()*2-1)*0.4f; s.vy=(rng.nextFloat()*2-1)*0.4f; }
                s.vx*=0.9f; s.vy*=0.9f;
            } else if (v==5) {                // Dieb: flieht schnell vor der Maus
                float dx=s.x-mausX, dy=s.y-mausY, dd=(float)Math.sqrt(dx*dx+dy*dy);
                if (dd<170 && dd>0.01f){ s.vx=dx/dd*4.2f; s.vy=dy/dd*4.2f; }
                else if (rng.nextInt(20)==0){ s.vx=(rng.nextFloat()*2-1)*2f; s.vy=(rng.nextFloat()*2-1)*2f; }
            } else {                          // Normalito: sanftes Trippeln
                if (rng.nextInt(70)==0){ s.vx=(rng.nextFloat()*2-1)*0.9f; s.vy=(rng.nextFloat()*2-1)*0.9f; }
                s.vx*=0.96f; s.vy*=0.96f;
            }
            s.x+=s.vx; s.y+=s.vy;
            if (s.x<t[0]+20||s.x>t[2]-20){ s.vx=-s.vx; s.x=Math.max(t[0]+20,Math.min(t[2]-20,s.x)); }
            if (s.y<t[1]+20||s.y>t[3]-20){ s.vy=-s.vy; s.y=Math.max(t[1]+20,Math.min(t[3]-20,s.y)); }
        }

        if (!laufend || ausdauer <= 0) { if (ausdauer <= 0) beenden(); return; }

        if (st.hatSteinregen && ++steinCounter >= 45) { steinCounter=0;
            List<Schwein> z = new ArrayList<>(schweine);
            for (int k=0;k<3&&k<z.size();k++) trefferAnSchwein(z.get(k), Math.max(1, st.schaden/2), 0xFFB0B0FF); }

        if (++tickCounter >= st.intervall) { tickCounter=0; schlag(); }
    }

    private void schlag() {
        schwuenge++; schlagNr++;
        if (st.regenChance>0 && schlagNr%10==0) ausdauer=Math.min(st.maxAusdauer, ausdauer+(int)Math.round(st.maxAusdauer*0.02));
        ausdauer = Math.max(0, ausdauer-st.schlagKosten);
        swingT = 5;                                          // Hammer schwingt IMMER im Takt

        // Primär-Treffer: die Maus muss WIRKLICH über einem Schwein sein
        float body = Math.max(24f, this.width/26f);          // halbe Schwein-Breite
        Schwein primary=null; double bestD=Double.MAX_VALUE;
        for (Schwein s : schweine){ double dx=s.x-mausX, dy=s.y-mausY, d=dx*dx+dy*dy; if (d<=body*body && d<bestD){ bestD=d; primary=s; } }
        if (primary==null){ partikelStaub((int)mausX,(int)mausY); if (ausdauer<=0) beenden(); return; }  // Tisch getroffen, kein Splash

        // Splash: getroffenes Schwein + umliegende im Trefferradius UM DIESES Schwein
        double r2 = st.trefferRadius*(double)st.trefferRadius;
        List<Schwein> getroffen = new ArrayList<>();
        for (Schwein s : schweine){ double dx=s.x-primary.x, dy=s.y-primary.y; if (dx*dx+dy*dy<=r2) getroffen.add(s); }

        treffer++;
        partikelBlitz((int)mausX,(int)mausY);
        for (Schwein z : getroffen){ boolean krit=rng.nextDouble()<st.critChance;
            trefferAnSchwein(z, krit?st.schaden*2:st.schaden, krit?0xFFFFE060:0xFFFFFFFF); }
        if (st.hatElektro && !schweine.isEmpty()){ Schwein nah=null; double best=100*100; Schwein a=getroffen.get(0);
            for (Schwein s : schweine){ if (getroffen.contains(s)) continue; double dx=s.x-a.x,dy=s.y-a.y,dd=dx*dx+dy*dy; if (dd<best){best=dd;nah=s;} }
            if (nah!=null) trefferAnSchwein(nah, Math.max(1, st.schaden/2), 0xFF80D0FF); }
        if (ausdauer<=0) beenden();
    }

    private void trefferAnSchwein(Schwein z, int dmg, int color) {
        if (!schweine.contains(z)) return;
        z.hp -= dmg;
        addFloater(String.valueOf(dmg), (int)z.x, (int)z.y-28, color);
        if (z.hp > 0 && GameState.ARTEN[z.artIndex].verhalten==4) {   // Taurus wird wütend & stürmt los
            z.rage = 70; float a=rng.nextFloat()*6.2832f; z.vx=(float)Math.cos(a)*4.6f; z.vy=(float)Math.sin(a)*4.6f;
        }
        if (z.hp <= 0) {
            int wert = st.beuteWert(z.artIndex, rng);
            ertrag+=wert; verteile(wert); zerstoert++; artZerstoert[z.artIndex]++; st.meisterschaftPlus(z.artIndex);
            if (GameState.ARTEN[z.artIndex].verhalten==4) ausdauer=Math.min(st.maxAusdauer, ausdauer+15);  // Taurus gibt Ausdauer zurück
            if (st.gadgetBesitz[3] && rng.nextInt(100) < 10) {   // Spielautomat: Jackpot-Chance
                int bonus = wert*4; ertrag+=bonus; verteile(bonus);
                addFloater("JACKPOT +"+bonus, (int)z.x, (int)z.y-62, 0xFFFFD040);
            }
            addFloater("+"+wert, (int)z.x, (int)z.y-6, 0xFF8CFF8C);
            partikelSplitter(z);
            if (st.reliktDropVersuch(rng)>=0) addFloater("Seltene Münze!", (int)z.x, (int)z.y-44, 0xFFF0C860);
            schweine.remove(z); schweine.add(neu());
        }
    }

    private void partikelSplitter(Schwein z) {
        int col = SHARD[z.artIndex];
        for (int i=0;i<10;i++){ Partikel p=new Partikel(); p.x=p.px=z.x; p.y=p.py=z.y;
            p.vx=(rng.nextFloat()*2-1)*3.4f; p.vy=-rng.nextFloat()*4.5f-1; p.color=col; p.typ=0; p.maxLife=p.life=18+rng.nextInt(8); partikel.add(p); }
        for (int i=0, n=2+rng.nextInt(3); i<n; i++){ Partikel p=new Partikel(); p.x=p.px=z.x; p.y=p.py=z.y;
            p.vx=(rng.nextFloat()*2-1)*2.4f; p.vy=-rng.nextFloat()*4-2; p.typ=1; p.phase=0; p.timer=12+rng.nextInt(6);
            p.tex=rng.nextInt(3)==0?"coin_gold":"coin_silber"; p.maxLife=p.life=9999; partikel.add(p); }
    }
    private void partikelBlitz(int x,int y){ Partikel p=new Partikel(); p.x=p.px=x; p.y=p.py=y; p.typ=2; p.maxLife=p.life=6; partikel.add(p); }
    private void partikelStaub(int x,int y){ for (int i=0;i<5;i++){ Partikel p=new Partikel(); p.x=p.px=x; p.y=p.py=y;
        p.vx=(rng.nextFloat()*2-1)*2.0f; p.vy=-rng.nextFloat()*2.2f-0.5f; p.color=0xFF9A8A78; p.typ=0; p.maxLife=p.life=10+rng.nextInt(6); partikel.add(p); } }

    private void verteile(int wert) {
        int b=wert/1000; wert%=1000; int r=wert/100; wert%=100; int g=wert/25; wert%=25; int go=wert/5; wert%=5;
        muenzen[4]+=b; muenzen[3]+=r; muenzen[2]+=g; muenzen[1]+=go; muenzen[0]+=wert; beute+=b+r+g+go+wert;
    }
    private void addFloater(String t,int x,int y,int c){ Floater f=new Floater(); f.text=t; f.x=x; f.y=f.py=y; f.color=c; floaters.add(f); }

    private void beenden() {
        if (beendet) return; beendet=true; laufend=false;
        int gen = schwuenge==0?100:Math.round(treffer*100f/schwuenge);
        if (this.minecraft!=null) this.minecraft.gui.setScreen(new MuedeHandScreen(ertrag, gen, beute, muenzen, zerstoert, artZerstoert));
    }

    private static float ip(float prev, float cur, float d){ if (d<0) d=0; else if (d>1) d=1; return prev + (cur-prev)*d; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        mausX = mouseX; mausY = mouseY;
        Ui.desk(g, this.width, this.height);

        // HUD
        int barX=20, barY=30, barW=220, barH=10;
        g.fill(barX-1,barY-1,barX+barW+1,barY+barH+1,0xFF201812);
        g.fill(barX,barY,barX+barW,barY+barH,0xFF3A2A20);
        int filled=Math.round(barW*(ausdauer/(float)st.maxAusdauer));
        g.fill(barX,barY,barX+filled,barY+barH, ausdauer<=st.maxAusdauer/5?0xFFE04030:0xFFF0B040);
        Ui.text(g,this.font,"Ausdauer: "+ausdauer+"/"+st.maxAusdauer,barX,barY+barH+3,0xFFFFFFFF);
        Ui.center(g,this.font,"CJ's hits",this.width/2,12,0xFFFFFFFF);
        String geld=st.geld+" Taler"; int gx=this.width-20-Ui.w(this.font,geld); Ui.imgS(g,"coin_gold",gx-15,11,12,12,24,24); Ui.text(g,this.font,geld,gx,12,0xFFFFE070);
        String tg="Tag "+st.tag+"   Legacy "+st.legacy; Ui.text(g,this.font,tg,this.width-20-Ui.w(this.font,tg),26,0xFFB0B0B0);

        // Münzstapel im Fenstersims (wächst mit der Beute) – wie im Original
        int wyy=Math.max(28,(int)(this.height*0.15)), coinsN=Math.min(40,beute);
        for (int i=0;i<coinsN;i++){ int col=i%8, row=i/8; Ui.imgS(g,"coin_gold", this.width/2-36+col*9, wyy-6-row*4, 9, 9, 24, 24); }

        // Schweine
        int pigW=Math.max(48,this.width/13), pigH=pigW*112/128;
        for (Schwein s : schweine){ float rx=ip(s.px,s.x,delta), ry=ip(s.py,s.y,delta);
            Ui.imgS(g, GameState.ARTEN[s.artIndex].tex, (int)(rx-pigW/2), (int)(ry-pigH/2), pigW, pigH, 128, 112); }

        // Partikel
        for (Partikel p : partikel){ int ix=(int)ip(p.px,p.x,delta), iy=(int)ip(p.py,p.y,delta);
            int a=Math.max(0,Math.min(255,255*p.life/p.maxLife));
            if (p.typ==0){ int c=(a<<24)|(p.color&0xFFFFFF); int ss=Math.max(2,this.width/300); g.fill(ix-ss,iy-ss,ix+ss,iy+ss,c); }
            else if (p.typ==1){ int cs=Math.max(14,this.width/40); Ui.imgS(g,p.tex,ix-cs/2,iy-cs/2,cs,cs,24,24); }
            else { int s2=(p.maxLife-p.life)*4+4, c=(a<<24)|0xFFF0C0;
                g.fill(ix-s2,iy-s2,ix+s2,iy-s2+2,c); g.fill(ix-s2,iy+s2-2,ix+s2,iy+s2,c);
                g.fill(ix-s2,iy-s2,ix-s2+2,iy+s2,c); g.fill(ix+s2-2,iy-s2,ix+s2,iy+s2,c); } }

        // Hammer folgt live der Maus, mit weichem Schwung-Bogen
        if (laufend) {
            int hamH=Math.max(96,(int)(this.height*0.34)), hamW=hamH*72/150;
            float dip=0;
            if (swingT>0){ float tt=1f-Math.max(0f,Math.min(1f,(swingT-delta)/5f)); dip=(float)(Math.sin(tt*Math.PI)*hamH*0.30); }
            Ui.imgS(g, GameState.HAEMMER[st.hammerIndex].tex, (int)(mausX-hamW*0.47f), (int)(mausY-hamH*0.13f+dip), hamW, hamH, 72, 150);
        }

        // Fliegende Zahlen
        for (Floater f : floaters){ int a=Math.max(0,Math.min(255,255*f.life/Floater.LIFE));
            Ui.center(g,this.font,f.text,f.x,(int)ip(f.py,f.y,delta),(a<<24)|(f.color&0xFFFFFF)); }

        // Warn-Vignette bei niedriger Ausdauer
        if (laufend && ausdauer <= st.maxAusdauer/5) {
            int a = 34 + (int)(26*Math.abs(Math.sin(System.currentTimeMillis()/220.0)));
            int col=(a<<24)|0xE03020, e=Math.max(10,this.height/14);
            g.fill(0,0,this.width,e,col); g.fill(0,this.height-e,this.width,this.height,col);
            g.fill(0,0,e,this.height,col); g.fill(this.width-e,0,this.width,this.height,col);
        }
    }

    public void renderBackground(GuiGraphicsExtractor g, int mx, int my, float dt) { Ui.desk(g, this.width, this.height); }

    @Override public boolean isPauseScreen() { return false; }
}
