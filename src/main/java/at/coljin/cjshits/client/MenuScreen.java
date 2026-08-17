package at.coljin.cjshits.client;

import at.coljin.cjshits.GameState;
import at.coljin.cjshits.GameState.PigArt;
import at.coljin.cjshits.GameState.Skill;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Menü mit allen Reitern */
public class MenuScreen extends Screen {

    private static final String[] TABS = {"Rechnungen","Skilltree","Sparschweine","Shop","Sammlung"};
    private final GameState st = GameState.I;
    private final int tab;
    private final boolean bankrottConfirm;
    private static double skillZoom = 0.85;
    private static int skillPanX = 0, skillPanY = 0;

    public MenuScreen(int tab) { this(tab, false); }
    public MenuScreen(int tab, boolean bankrottConfirm) {
        super(Component.literal("CJ's hits")); this.tab = tab; this.bankrottConfirm = bankrottConfirm;
    }

    private int cx(){ return this.width/2; }
    private int cy(){ return this.height/2+16; }
    private int tabW(){ return Math.min(110, (this.width-16)/TABS.length); }
    private int tabX0(){ return cx() - tabW()*TABS.length/2; }
    private void reopen(int t, boolean c){ if(minecraft!=null) minecraft.gui.setScreen(new MenuScreen(t, c)); }
    private static boolean hover(int mx,int my,int x,int y,int w,int h){ return mx>=x&&mx<=x+w&&my>=y&&my<=y+h; }
    private static String verhaltenText(int v){ switch(v){ case 1: return "wandert"; case 2: return "steht (zäh)"; case 3: return "schnell & chaotisch"; case 4: return "faul, wird wütend"; case 5: return "flieht"; default: return "ruhig"; } }

    private int skillSp(){ return Math.max(24, (int)(64*skillZoom)); }
    private int nodeR(){ return Math.max(8, (int)(12*skillZoom)); }
    private int[] nodePos(Skill s){ int sp=skillSp(); return new int[]{cx()+skillPanX+s.dx*sp, cy()+skillPanY+s.dy*sp}; }
    private int hammerX(int i){ int sw=80; return cx()-7*sw/2 + i*sw; }
    private int gadgetX(int i){ int gw=92; return cx()-6*gw/2 + i*gw; }
    private int[] gridPos(int i){ int cw=Math.min(360,(this.width-60)/3), sx=cx()-3*cw/2; return new int[]{ sx+(i%3)*cw, 130+(i/3)*120 }; }

    public void renderBackground(GuiGraphicsExtractor g, int mx, int my, float dt) { Ui.desk(g, this.width, this.height); }

    @Override
    protected void init() {
        int tw=tabW(), x=tabX0();
        for (int i=0;i<TABS.length;i++){ final int idx=i;
            addRenderableWidget(Button.builder(Component.literal(TABS[i]), b -> reopen(idx,false)).bounds(x+i*tw, 6, tw-4, 18).build()); }
        addRenderableWidget(Button.builder(Component.literal("Weiter (nächster Lauf)"), b -> weiter()).bounds(this.width-210, this.height-26, 190, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Schließen"), b -> this.onClose()).bounds(20, this.height-26, 120, 20).build());
        switch (tab) { case 0: initRechnungen(); break; case 1: initSkilltree(); break;
                       case 3: initShop(); break; default: break; }
    }

    private void weiter(){ st.tagWeiter(); st.save(); if(minecraft!=null) minecraft.gui.setScreen(new TischScreen()); }

    private void initRechnungen() {
        int y=this.height-64, bw=Math.min(200,(this.width-80)/3-10), g=20, tot=bw*3+g*2, x=cx()-tot/2;
        if (st.kannRechnungZahlen())
            addRenderableWidget(Button.builder(Component.literal("Bezahlen ("+st.rechnungBetrag+" Taler)"), b -> {
                if (st.rechnungBezahlen() && minecraft!=null) minecraft.gui.setScreen(new PerkScreen(new MenuScreen(0)));
            }).bounds(x, y, bw, 20).build());
        if (st.toniVerfuegbar())
            addRenderableWidget(Button.builder(Component.literal("Big Toni anrufen"), b -> { if(minecraft!=null) minecraft.gui.setScreen(new ToniScreen(new MenuScreen(0))); }).bounds(x+bw+g, y, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Bankrott erklären"), b -> {
            if (minecraft!=null) minecraft.gui.setScreen(new PrestigeScreen());
        }).bounds(x+2*(bw+g), y, bw, 20).build());
    }

    private void initSkilltree() {
        int r=nodeR();
        for (Skill s : GameState.SKILLS) { if (s.id==0) continue; final int id=s.id; int[] p=nodePos(s);
            addRenderableWidget(Button.builder(Component.literal(""), b -> { st.kaufeSkill(id); reopen(1,false); }).bounds(p[0]-r, p[1]-r, 2*r, 2*r).build()); }
        int rx=this.width-92, ry=44;
        addRenderableWidget(Button.builder(Component.literal("+"), b -> { skillZoom=Math.min(1.8,skillZoom+0.15); reopen(1,false); }).bounds(rx, ry, 22, 18).build());
        addRenderableWidget(Button.builder(Component.literal("-"), b -> { skillZoom=Math.max(0.5,skillZoom-0.15); reopen(1,false); }).bounds(rx+24, ry, 22, 18).build());
        addRenderableWidget(Button.builder(Component.literal("^"), b -> { skillPanY+=70; reopen(1,false); }).bounds(rx+8, ry+22, 22, 16).build());
        addRenderableWidget(Button.builder(Component.literal("v"), b -> { skillPanY-=70; reopen(1,false); }).bounds(rx+8, ry+60, 22, 16).build());
        addRenderableWidget(Button.builder(Component.literal("<"), b -> { skillPanX+=70; reopen(1,false); }).bounds(rx-16, ry+41, 22, 16).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> { skillPanX-=70; reopen(1,false); }).bounds(rx+32, ry+41, 22, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Zentrieren"), b -> { skillZoom=0.85; skillPanX=0; skillPanY=0; reopen(1,false); }).bounds(rx-16, ry+80, 88, 16).build());
    }

    private void initShop() {
        for (int i=0;i<GameState.HAEMMER.length;i++){ final int idx=i; int bx=hammerX(i);
            String lbl = !st.hammerBesitz[i] ? GameState.HAEMMER[i].preis+" Taler" : (i==st.hammerIndex?"Ausgerüstet":"Ausrüsten");
            addRenderableWidget(Button.builder(Component.literal(lbl), b -> {
                if (!st.hammerBesitz[idx]){ if (st.kaufeHammer(idx)) st.ruesteHammer(idx); } else st.ruesteHammer(idx); reopen(3,false);
            }).bounds(bx, 98, 76, 18).build()); }
        for (int i=0;i<GameState.GADGETS.length;i++){ final int idx=i; int bx=gadgetX(i);
            String lbl = st.gadgetBesitz[i] ? "Im Besitz" : GameState.GADGETS[i].preis+" Taler";
            addRenderableWidget(Button.builder(Component.literal(lbl), b -> { st.kaufeGadget(idx); reopen(3,false); }).bounds(bx, 188, 88, 18).build()); }
    }

    private void initPrestige() {
        for (int i=0;i<GameState.PRESTIGE.length;i++){ final int idx=i; int[] p=gridPos(i);
            String lbl = st.prestigeBesitz[i] ? "Im Besitz" : GameState.PRESTIGE[i].kosten+" Legacy";
            addRenderableWidget(Button.builder(Component.literal(lbl), b -> { st.kaufePrestige(idx); reopen(5,false); }).bounds(p[0], p[1]+44, Math.min(200,(this.width-40)/3-16), 18).build()); }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int tw=tabW(), x0=tabX0();
        g.fill(x0+tab*tw, 25, x0+tab*tw+tw-4, 27, 0xFFF0B040);
        String hud=st.geld+" Taler   Legacy "+st.legacy+"   Tag "+st.tag;
        int hudX=this.width-16-Ui.w(this.font,hud); Ui.imgS(g,"coin_gold",hudX-15,31,12,12,24,24);
        Ui.text(g, this.font, hud, hudX, 32, 0xFFFFE070);

        switch (tab) { case 0: drawRechnungen(g); break; case 1: drawSkilltree(g,mouseX,mouseY); break;
            case 2: drawSparschweine(g,mouseX,mouseY); break; case 3: drawShop(g,mouseX,mouseY); break;
            case 4: drawSammlung(g,mouseX,mouseY); break; }
    }

    private void drawRechnungen(GuiGraphicsExtractor g) {
        int pw=Math.min(420,this.width-80), px=cx()-pw/2, py=56;
        g.fill(px,py,px+pw,py+220,0xFFEAD9B0);
        Ui.center(g,this.font,"Sunrise Hausverwaltung – Mietzahlung",cx(),py+14,0xFF503A20);
        Ui.center(g,this.font,"Rechnungen müssen bezahlt werden, oder Bankrott droht.",cx(),py+36,0xFF6A4A2A);
        Ui.center(g,this.font,"Fälliger Betrag",cx(),py+72,0xFF9A2A2A);
        Ui.center(g,this.font,st.rechnungBetrag+" Taler",cx(),py+88,0xFF201810);
        Ui.center(g,this.font,"Beim Zahlen: +"+Math.max(1,st.rechnungBetrag/100)+" Legacy und ein Perk (1 aus 3).",cx(),py+112,0xFF3A7A3A);
        Ui.center(g,this.font,"Nächste Rechnung danach: ~"+(Math.round(st.rechnungBetrag*1.6)+250)+" Taler",cx(),py+130,0xFF8A7A5A);
        int ueber = Math.max(0, st.tag - st.rechnungFaellig);
        String f = st.rechnungUeberfaellig()?("ÜBERFÄLLIG ("+ueber+" Tag"+(ueber==1?"":"e")+") – Inkasso "+(int)(Math.min(0.75,0.25+0.10*ueber)*100)+"%!"):"Fällig an Tag "+st.rechnungFaellig+" (heute Tag "+st.tag+")";
        Ui.center(g,this.font,f,cx(),py+150,st.rechnungUeberfaellig()?0xFFCC3030:0xFF503A20);
        if (st.toniSchuld>0) Ui.center(g,this.font,"Big-Toni-Schuld: "+st.toniSchuld+" Taler",cx(),py+168,0xFF8A3030);
        if (bankrottConfirm) Ui.center(g,this.font,"Bankrott: Geld/Hämmer/Skills weg – Legacy, Ringe & Sammlung bleiben.",cx(),py+192,0xFFCC5050);
        // Big Tonis Telefon
        if ((st.toniVerfuegbar() || st.toniSchuld>0) && px+pw+56 < this.width) {
            Ui.img(g,"prop_telefon", px+pw+12, py+24, 40, 60);
            Ui.center(g,this.font,"Big Toni",px+pw+32,py+90,0xFFE0C060);
            if (st.toniVerfuegbar()) Ui.center(g,this.font,"\"Kohle gefällig? Aber du zahlst zurück, capisce?\"",cx(),py+236,0xFFB0A090);
        }
    }

    private void drawSkilltree(GuiGraphicsExtractor g, int mx, int my) {
        Ui.center(g,this.font,"Skilltree – kaufe mit Geld ("+st.geld+" Taler)   ·   Zoom +/- , verschieben mit Pfeilen",cx(),40,0xFFE0C060);
        for (Skill s : GameState.SKILLS) { if (s.prereq<0) continue;
            int[] a=nodePos(GameState.SKILLS[s.prereq]), b=nodePos(s);
            int col = st.skillFrei(s.id)?0xFFF0B040:0xFF554A40;
            g.fill(Math.min(a[0],b[0]),a[1]-1,Math.max(a[0],b[0]),a[1]+1,col);
            g.fill(b[0]-1,Math.min(a[1],b[1]),b[0]+1,Math.max(a[1],b[1]),col); }
        Skill hoverS=null; int r=nodeR();
        for (Skill s : GameState.SKILLS) { int[] p=nodePos(s);
            boolean owned=st.skillFrei(s.id), buy=st.skillKaufbar(s.id);
            int base=kat(s.eff.typ), col=owned?base:(buy?dim(base,0.75):0xFF2A2420);
            g.fill(p[0]-r-1,p[1]-r-1,p[0]+r+1,p[1]+r+1,0xFF000000);
            g.fill(p[0]-r,p[1]-r,p[0]+r,p[1]+r,col);
            if (buy){ g.fill(p[0]-r,p[1]-r,p[0]+r,p[1]-r+2,0xFFF0D060); g.fill(p[0]-r,p[1]+r-2,p[0]+r,p[1]+r,0xFFF0D060); }
            int is=Math.max(10,2*r-4); Ui.imgS(g,"skill_"+s.eff.typ,p[0]-is/2,p[1]-is/2,is,is,24,24);
            if (hover(mx,my,p[0]-r,p[1]-r,2*r,2*r)) hoverS=s; }
        if (hoverS!=null) Ui.tooltip(g,this.font,new String[]{hoverS.name, hoverS.beschr, st.skillFrei(hoverS.id)?"Freigeschaltet":"Kosten: "+hoverS.kosten+" Taler"},mx,my,this.width,this.height);
    }
    private int kat(String t){ switch(t){ case "tempo": return 0xFF3A6AB0; case "schaden": case "radius": return 0xFFB04A3A;
        case "glueck": case "geld": case "zins": return 0xFF3A9A5A; case "ausdauer": case "regen": return 0xFF8A6A3A;
        case "steinregen": case "elektro": return 0xFF9A5AB0; default: return 0xFF7A6A5A; } }
    private int dim(int argb,double f){ int a=(argb>>>24)&0xFF,r=(int)(((argb>>16)&0xFF)*f),g=(int)(((argb>>8)&0xFF)*f),b=(int)((argb&0xFF)*f); return (a<<24)|(r<<16)|(g<<8)|b; }

    private void drawSparschweine(GuiGraphicsExtractor g, int mx, int my) {
        Ui.center(g,this.font,"Sparschweine – Meisterschaft durchs Zerkloppen",cx(),46,0xFFE0C060);
        int n=GameState.ARTEN.length, cw=Math.min(150,(this.width-40)/n), x0=cx()-(n*cw)/2, hoverI=-1;
        for (int i=0;i<n;i++){ PigArt a=GameState.ARTEN[i]; int bx=x0+i*cw+cw/2, by=130;
            if (st.pigFrei[i]){
                Ui.imgS(g,a.tex,bx-26,by-24,52,46,128,112);
                Ui.center(g,this.font,a.name,bx,by+26,0xFFFFFFFF);
                Ui.center(g,this.font,"Stufe "+st.pigStufe(i),bx,by+40,0xFF80D080);
                int prog=st.pigMeisterschaft[i]%25;
                g.fill(bx-40,by+54,bx+40,by+60,0xFF3A3230); g.fill(bx-40,by+54,bx-40+(int)(80*prog/25f),by+60,0xFF60C080);
            } else { g.fill(bx-22,by-22,bx+22,by+21,0xFF201A16); Ui.center(g,this.font,"Gesperrt",bx,by+26,0xFF7A6A5A); }
            if (hover(mx,my,bx-30,by-24,60,90)) hoverI=i; }
        if (hoverI>=0){ var a=GameState.ARTEN[hoverI];
            Ui.tooltip(g,this.font, st.pigFrei[hoverI]
                ? new String[]{a.name,"HP: "+a.hp,"Wert: ~"+a.wert+" Taler", verhaltenText(a.verhalten),"Meisterschaft: Stufe "+st.pigStufe(hoverI)}
                : new String[]{"Gesperrt","Noch nicht entdeckt"}, mx,my,this.width,this.height); }
    }

    private void drawShop(GuiGraphicsExtractor g, int mx, int my) {
        Ui.center(g,this.font,"Shop – Hämmer & Gadgets (mit Geld)",cx(),46,0xFFE0C060);
        Ui.text(g,this.font,"Hämmer",hammerX(0)-4,58,0xFFD0B080);
        int hoverH=-1, hoverG=-1;
        for (int i=0;i<GameState.HAEMMER.length;i++){ int bx=hammerX(i);
            Ui.imgReg(g,GameState.HAEMMER[i].tex,bx+21,68,34,27,0,0,72,56,72,150);
            if (hover(mx,my,bx-2,64,80,54)) hoverH=i; }
        Ui.text(g,this.font,"Gadgets",gadgetX(0)-4,150,0xFFD0B080);
        for (int i=0;i<GameState.GADGETS.length;i++){ int bx=gadgetX(i);
            Ui.img(g,GameState.GADGETS[i].tex,bx+34,160,24,24);
            if (hover(mx,my,bx-2,156,92,54)) hoverG=i; }
        if (hoverH>=0){ var h=GameState.HAEMMER[hoverH];
            Ui.tooltip(g,this.font,new String[]{h.name,"Schaden: "+h.schaden,"Tempo: "+h.intervall+" (kleiner=schneller)","Crit: "+(int)(h.crit*100)+"%","Radius: "+h.radius,"Ausdauer/Schlag: "+Math.max(2,(int)Math.round(h.schaden/5.0)),
                st.hammerBesitz[hoverH]?(hoverH==st.hammerIndex?"Ausgerüstet":"Im Besitz"):"Preis: "+h.preis+" Taler"}, mx,my,this.width,this.height); }
        else if (hoverG>=0){ var ga=GameState.GADGETS[hoverG];
            Ui.tooltip(g,this.font,new String[]{ga.name,ga.beschr, st.gadgetBesitz[hoverG]?"Im Besitz":"Preis: "+ga.preis+" Taler"}, mx,my,this.width,this.height); }
    }

    private void drawSammlung(GuiGraphicsExtractor g, int mx, int my) {
        g.fill(30, 54, this.width-30, this.height-40, 0xF01A140E);
        Ui.center(g,this.font,"Sammlung – Spezialmünzen (seltene Drops, bleiben bei Bankrott)",cx(),46,0xFFE0C060);
        int hoverI=-1;
        for (int i=0;i<GameState.RELIKTE.length;i++){ var r=GameState.RELIKTE[i]; int[] p=gridPos(i); boolean hat=st.reliktBesitz[i];
            Ui.img(g,r.tex,p[0],p[1],20,20);
            Ui.text(g,this.font,r.name,p[0]+26,p[1]+4,hat?0xFFE0C060:0xFF6A6A6A);
            Ui.text(g,this.font,hat?r.beschr:"Noch nicht gefunden",p[0],p[1]+26,hat?0xFFA0A0A0:0xFF6A6A6A);
            if (hover(mx,my,p[0],p[1],190,40)) hoverI=i; }
        if (hoverI>=0){ var r=GameState.RELIKTE[hoverI];
            Ui.tooltip(g,this.font,new String[]{r.name, st.reliktBesitz[hoverI]?r.beschr:"Noch nicht gefunden", st.reliktBesitz[hoverI]?"Gefunden":"Seltener Drop im Lauf"}, mx,my,this.width,this.height); }
    }

    private void drawPrestige(GuiGraphicsExtractor g, int mx, int my) {
        Ui.center(g,this.font,"Prestige – Ringe & Armbänder (mit Legacy, bleiben bei Bankrott)",cx(),46,0xFFE0C060);
        Ui.center(g,this.font,"Legacy-Punkte bekommst du fürs Bezahlen von Rechnungen.",cx(),62,0xFF8090C0);
        int hoverI=-1;
        for (int i=0;i<GameState.PRESTIGE.length;i++){ var pr=GameState.PRESTIGE[i]; int[] p=gridPos(i);
            Ui.img(g,pr.tex,p[0],p[1],16,16);
            Ui.text(g,this.font,pr.name,p[0]+22,p[1]+2,st.prestigeBesitz[i]?0xFFE0C060:0xFFCFCFCF);
            Ui.text(g,this.font,pr.beschr,p[0],p[1]+22,0xFFA0A0A0);
            if (hover(mx,my,p[0],p[1],200,40)) hoverI=i; }
        if (hoverI>=0){ var pr=GameState.PRESTIGE[hoverI];
            Ui.tooltip(g,this.font,new String[]{pr.name, pr.beschr, st.prestigeBesitz[hoverI]?"Im Besitz":"Kosten: "+pr.kosten+" Legacy"}, mx,my,this.width,this.height); }
    }

    @Override public boolean isPauseScreen() { return false; }
}
