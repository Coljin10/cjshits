package at.coljin.cjshits;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Zentraler Spielzustand von CJ's hits – am Original "Bills Must Be Paid" orientiert.
 *
 * Wirtschaft (wie im Original):
 *  - GELD: aus Läufen; zahlt Rechnungen, Skilltree, Hämmer, Gadgets. Bei Bankrott weg.
 *  - LEGACY-Punkte: aus bezahlten Rechnungen; kaufen Ringe & Armbänder (bleiben bei Bankrott).
 *  - Perks: 1 aus 3 nach jeder Rechnung, dauerhaft bis Bankrott.
 *  - Sammlung (Relikte): seltene Drops, bleiben bei Bankrott.
 */
public final class GameState {

    public static final class Eff { public final String typ; public final double wert;
        public Eff(String t, double w){typ=t;wert=w;} }

    // ---------- Hämmer (Schaden, Tempo=Intervall, Crit, Radius) ----------
    public static final class Hammer { public final String name, tex; public final int schaden, intervall, radius;
        public final double crit; public final long preis;
        Hammer(String n,String t,int s,int iv,double c,int r,long p){name=n;tex=t;schaden=s;intervall=iv;crit=c;radius=r;preis=p;} }
    public static final Hammer[] HAEMMER = {
        new Hammer("Klassiker",       "hammer",        7,  8, 0.02, 40,        0),
        new Hammer("Gummihammer",     "hammer_mallet",10,  8, 0.03, 46,     1000), // ausgewogen
        new Hammer("Spitzhacke",      "hammer_pick",   9,  6, 0.05, 34,     4000), // ultraschnell, schmal
        new Hammer("Vorschlaghammer", "hammer_sledge",24, 11, 0.02, 56,    50000), // schwer, breit
        new Hammer("Riesenhammer",    "hammer_big",   40, 12, 0.02, 60,   250000), // sehr schwer, breit
        new Hammer("Richterhammer",   "hammer_gavel", 22,  8, 0.15, 42,  1000000), // Crit-fokus
        new Hammer("Goldhammer",      "hammer_gold",  45,  6, 0.10, 54, 10000000), // schnell + breit (Top)
    };

    // ---------- Gadgets (Kauf mit Geld) ----------
    public static final class Gadget { public final String name, tex, beschr; public final Eff eff; public final long preis;
        Gadget(String n,String t,String b,Eff e,long p){name=n;tex=t;beschr=b;eff=e;preis=p;} }
    public static final Gadget[] GADGETS = {
        new Gadget("Kleeblatttopf","gadget_klee","Erhöht dein Glück – bessere Chance auf wertvolle Beute.", new Eff("glueck",0.03),   400),
        new Gadget("Glückskatze",  "gadget_katze","Winkt dir mehr Glück herbei.",                           new Eff("glueck",0.05),  2500),
        new Gadget("Topfpflanze",  "gadget_pflanze","Frische Luft: +15 maximale Ausdauer.",                 new Eff("ausdauer",15),  4500),
        new Gadget("Spielautomat", "gadget_slot","Jackpot-Chance: manchmal zahlt ein Schwein das 5-Fache. Dazu +10% Geldwert.", new Eff("geld",0.10),   30000),
        new Gadget("Moai",         "gadget_moai","Steinharte Schläge: +15% Schaden.",                       new Eff("schaden",0.15),145000),
        new Gadget("Schallplatte", "gadget_disc","Guter Rhythmus: +10% Angriffsgeschwindigkeit.",           new Eff("tempo",0.10),  210000),
    };

    // ---------- Skilltree (Kauf mit GELD; geht bei Bankrott verloren) ----------
    public static final class Skill { public final int id, prereq, dx, dy; public final long kosten;
        public final String name, beschr; public final Eff eff;
        Skill(int id,int pre,int dx,int dy,long k,String n,String b,Eff e){this.id=id;prereq=pre;this.dx=dx;this.dy=dy;kosten=k;name=n;beschr=b;eff=e;} }
    public static final Skill[] SKILLS = {
        new Skill(0,-1, 0, 0,    0,"Erster Schlag","Der Anfang von allem.", new Eff("schaden",0)),
        // Tempo (oben)
        new Skill(1, 0, 0,-1,  300,"Koffein","+10% Angriffsgeschwindigkeit.", new Eff("tempo",0.10)),
        new Skill(2, 1, 0,-2,  900,"Zuckerrausch","+15% Angriffsgeschwindigkeit.", new Eff("tempo",0.15)),
        new Skill(3, 2, 0,-3, 4000,"Wachmacher","+25% Angriffsgeschwindigkeit.", new Eff("tempo",0.25)),
        new Skill(4, 2, 1,-2, 2000,"Energie-Drink","+20% Angriffsgeschwindigkeit.", new Eff("tempo",0.20)),
        new Skill(5, 2,-1,-2, 2000,"Espresso","+15% Angriffsgeschwindigkeit.", new Eff("tempo",0.15)),
        // Schaden (rechts)
        new Skill(6, 0, 1, 0,  250,"Griffkraft","+15% Schaden.", new Eff("schaden",0.15)),
        new Skill(7, 6, 2, 0,  700,"Wucht","+20% Schaden.", new Eff("schaden",0.20)),
        new Skill(8, 7, 3, 0, 1800,"Hammerzeit","+30% Schaden.", new Eff("schaden",0.30)),
        new Skill(9, 7, 2,-1, 1500,"Gym","+25% Schaden.", new Eff("schaden",0.25)),
        new Skill(10, 9, 3,-1, 3500,"Krafttraining","+35% Schaden.", new Eff("schaden",0.35)),
        new Skill(11, 7, 2, 1, 1500,"Handgelenk","+20% Schaden.", new Eff("schaden",0.20)),
        new Skill(12,11, 3, 1, 3500,"Bizeps","+30% Schaden.", new Eff("schaden",0.30)),
        // Crit / Radius (oben rechts)
        new Skill(13, 6, 1,-1,  800,"Präzision","+6 Trefferradius.", new Eff("radius",6)),
        new Skill(14, 9, 2,-2, 2200,"Scharfer Blick","+5% Crit-Chance.", new Eff("crit",0.05)),
        new Skill(15,10, 3,-2, 4500,"Zielsicher","+8% Crit-Chance.", new Eff("crit",0.08)),
        // Ausdauer (links)
        new Skill(16, 0,-1, 0,  300,"Aufwärmen","+20 max. Ausdauer.", new Eff("ausdauer",20)),
        new Skill(17,16,-2, 0,  900,"Fitness","+30 max. Ausdauer.", new Eff("ausdauer",30)),
        new Skill(18,17,-3, 0, 2400,"Marathon","+45 max. Ausdauer.", new Eff("ausdauer",45)),
        new Skill(19,16,-2,-1, 1200,"Ausdauerlauf","+25 max. Ausdauer.", new Eff("ausdauer",25)),
        new Skill(20,16,-1,-1,  700,"Zähigkeit","Jeder 10. Schlag gibt Ausdauer zurück.", new Eff("regen",0.10)),
        new Skill(21,18,-3,-1, 3000,"Zweiter Wind","Mehr Ausdauer-Rückgewinn.", new Eff("regen",0.15)),
        new Skill(22,19,-2,-2, 3800,"Steinregen","Ab und zu prasseln Steine auf mehrere Schweine.", new Eff("steinregen",1)),
        // Glück / Wirtschaft (unten)
        new Skill(23, 0, 0, 1,  400,"Kleeblatt","+5% Glück.", new Eff("glueck",0.05)),
        new Skill(24,23, 0, 2,  900,"Vier Blätter","+8% Glück.", new Eff("glueck",0.08)),
        new Skill(25,24, 0, 3, 2600,"Glücksfee","+12% Glück.", new Eff("glueck",0.12)),
        new Skill(26,23,-1, 1,  700,"Sparfuchs","+10% Geldwert.", new Eff("geld",0.10)),
        new Skill(27,26,-1, 2, 1800,"Goldgrube","+15% Geldwert.", new Eff("geld",0.15)),
        new Skill(28,26,-2, 1, 3600,"Reichtum","+20% Geldwert.", new Eff("geld",0.20)),
        new Skill(29,23, 1, 1,  700,"Glückssträhne","+6% Glück.", new Eff("glueck",0.06)),
        new Skill(30,29, 1, 2, 1600,"Zinseszins","+5% Zinsen aufs Bankguthaben.", new Eff("zins",0.05)),
        new Skill(31,30, 1, 3, 3200,"Reingewinn","+25% Geldwert.", new Eff("geld",0.25)),
        new Skill(32,29, 2, 2, 3800,"Elektrohammer","Treffer springt auf ein Schwein daneben über.", new Eff("elektro",1)),
    };

    // ---------- Prestige: Ringe & Armbänder (Kauf mit LEGACY; bleiben bei Bankrott) ----------
    public static final class Prestige { public final String name, tex, beschr; public final Eff eff; public final int kosten;
        Prestige(String n,String t,String b,Eff e,int k){name=n;tex=t;beschr=b;eff=e;kosten=k;} }
    public static final Prestige[] PRESTIGE = {
        new Prestige("Ring der Kraft",     "ring","Dauerhaft +10% Schaden.",              new Eff("schaden",0.10), 3),
        new Prestige("Ring der Ausdauer",  "ring","Dauerhaft +25 max. Ausdauer.",         new Eff("ausdauer",25),  3),
        new Prestige("Armband des Glücks", "ring","Dauerhaft +8% Glück.",                 new Eff("glueck",0.08),  4),
        new Prestige("Armband des Reichtums","ring","Dauerhaft +15% Geldwert.",           new Eff("geld",0.15),    4),
        new Prestige("Ring der Wut",       "ring","Dauerhaft +5% Crit-Chance.",           new Eff("crit",0.05),    5),
        new Prestige("Armband der Eile",   "ring","Dauerhaft +10% Angriffsgeschwindigkeit.",new Eff("tempo",0.10), 6),
    };

    // ---------- Sammlung: Relikte (seltene Drops; bleiben bei Bankrott) ----------
    public static final class Relikt { public final String name, tex, beschr; public final Eff eff;
        Relikt(String n,String t,String b,Eff e){name=n;tex=t;beschr=b;eff=e;} }
    public static final Relikt[] RELIKTE = {
        new Relikt("Erholungsmünze","relic_hammer","Jeder 10. Schlag stellt Ausdauer wieder her.", new Eff("regen",0.10)),
        new Relikt("Aufprallmünze", "relic_bolt","Erhöht die Crit-Chance um 5%.",                   new Eff("crit",0.05)),
        new Relikt("Glücksmünze",   "relic_pig","+10% Glück.",                                      new Eff("glueck",0.10)),
        new Relikt("Zinsmünze",     "relic_coins","+5% Zinsen aufs Bankguthaben.",                  new Eff("zins",0.05)),
        new Relikt("Tempomünze",    "relic_x4","+15% Angriffsgeschwindigkeit.",                     new Eff("tempo",0.15)),
        new Relikt("Reichtumsmünze","relic_star","+20% Geldwert der Beute.",                        new Eff("geld",0.20)),
    };

    // ---------- Perks (1 aus 3 nach jeder Rechnung; dauerhaft bis Bankrott) ----------
    public static final class Perk { public final String name, beschr; public final Eff eff;
        Perk(String n,String b,Eff e){name=n;beschr=b;eff=e;} }
    public static final Perk[] PERKS = {
        new Perk("Fette Beute","+15% Geldwert der Beute.",          new Eff("geld",0.15)),
        new Perk("Zweiter Atem","+15 maximale Ausdauer.",           new Eff("ausdauer",15)),
        new Perk("Harte Schläge","+12% Schaden.",                   new Eff("schaden",0.12)),
        new Perk("Glückspilz","+6% Glück.",                         new Eff("glueck",0.06)),
        new Perk("Weiter Schwung","+6 Trefferradius.",              new Eff("radius",6)),
        new Perk("Kritischer Blick","+4% Crit-Chance.",             new Eff("crit",0.04)),
        new Perk("Schneller Hammer","+8% Angriffsgeschwindigkeit.", new Eff("tempo",0.08)),
        new Perk("Zinsen","+5% Zinsen auf dein Bankguthaben (pro Lauf).", new Eff("zins",0.05)),
    };

    // ---------- Sparschwein-Arten (mit Verhalten) ----------
    // verhalten: 0 = steht, 1 = Ausreißer (bewegt sich)
    // verhalten: 0=Normalito(sanft) 1=Tourist(wandert) 2=Woody(steht,zäh) 3=El Loco(chaotisch) 4=Taurus(faul, wütend & Ausdauer-Bonus) 5=Dieb(flieht)
    public static final class PigArt { public final String name, tex; public final int hp, wert, gewicht, verhalten; public final boolean jackpot;
        PigArt(String n,String t,int hp,int w,int g,int v,boolean j){name=n;tex=t;this.hp=hp;wert=w;gewicht=g;verhalten=v;jackpot=j;} }
    public static final PigArt[] ARTEN = {
        new PigArt("Normalito",  "pig_normalito",  6,   4, 34, 0, false),
        new PigArt("Tourist",    "pig_tourist",    8,  14, 22, 1, false),
        new PigArt("Woody",      "pig_woody",     22,  30, 16, 2, false),
        new PigArt("Piñata",     "pig_pinata",    10,  20, 12, 0, true),
        new PigArt("El Loco",    "pig_loco",       8,  35, 12, 3, false),
        new PigArt("Taurus",     "pig_taurus",    16,  60, 10, 4, false),
        new PigArt("Dieb",       "pig_dieb",       9,  90,  6, 5, false),
        new PigArt("Radioaktiv", "pig_radioaktiv",14, 180,  3, 3, false),
    };

    // Singleton ERST NACH allen Datentabellen erzeugen – sonst sind die Arrays im Konstruktor noch null.
    public static final GameState I = new GameState();

    // ---------- Veränderlicher Zustand ----------
    public long geld = 0;
    public int  legacy = 0;
    public int  tag = 1;
    public long rechnungBetrag = 750;
    public int  rechnungFaellig = 9;
    public int  rechnungNr = 1;
    public int  hammerIndex = 0;
    public final boolean[] hammerBesitz  = new boolean[HAEMMER.length];
    public final boolean[] gadgetBesitz  = new boolean[GADGETS.length];
    public final Set<Integer> skills     = new HashSet<>();
    public final boolean[] prestigeBesitz= new boolean[PRESTIGE.length];
    public final boolean[] reliktBesitz  = new boolean[RELIKTE.length];
    public final List<Integer> perks     = new ArrayList<>();
    public final int[] pigMeisterschaft  = new int[ARTEN.length];
    public final boolean[] pigFrei       = new boolean[ARTEN.length];
    public long toniSchuld = 0;
    public int  toniSperreBis = 0;
    public long gesamtBezahlt = 0;

    // ---------- Abgeleitete Werte ----------
    public int schaden, intervall, maxAusdauer, trefferRadius, schlagKosten;
    public double glueck, geldBonus, zinsSatz, critChance, regenChance;
    public boolean hatSteinregen, hatElektro;

    private GameState() {
        hammerBesitz[0] = true; skills.add(0);
        pigFrei[0] = true; pigFrei[1] = true;
        load(); recompute();
    }

    public void recompute() {
        double bSch=0,bTmp=0,bGlk=0,bGeld=0,bZins=0,bCrit=0,bRegen=0; int bAus=0,bRad=0;
        hatSteinregen=false; hatElektro=false;
        List<Eff> alle = new ArrayList<>();
        for (Integer id : skills) alle.add(SKILLS[id].eff);
        for (int i=0;i<GADGETS.length;i++)  if (gadgetBesitz[i])   alle.add(GADGETS[i].eff);
        for (int i=0;i<RELIKTE.length;i++)  if (reliktBesitz[i])   alle.add(RELIKTE[i].eff);
        for (int i=0;i<PRESTIGE.length;i++) if (prestigeBesitz[i]) alle.add(PRESTIGE[i].eff);
        for (Integer p : perks) alle.add(PERKS[p].eff);
        for (Eff e : alle) {
            switch (e.typ) {
                case "schaden":  bSch+=e.wert; break;
                case "tempo":    bTmp+=e.wert; break;
                case "glueck":   bGlk+=e.wert; break;
                case "ausdauer": bAus+=(int)e.wert; break;
                case "geld":     bGeld+=e.wert; break;
                case "zins":     bZins+=e.wert; break;
                case "crit":     bCrit+=e.wert; break;
                case "regen":    bRegen+=e.wert; break;
                case "radius":   bRad+=(int)e.wert; break;
                case "steinregen": hatSteinregen=true; break;
                case "elektro":    hatElektro=true; break;
                default: break;
            }
        }
        Hammer h = HAEMMER[hammerIndex];
        schaden = Math.max(1, (int)Math.round(h.schaden * (1 + bSch)));
        intervall = Math.max(2, (int)Math.round(h.intervall * (1 - bTmp)));
        maxAusdauer = 130 + bAus;
        trefferRadius = h.radius + bRad;
        schlagKosten = Math.max(2, (int)Math.round(h.schaden/5.0));  // schwere Hämmer kosten mehr Ausdauer
        glueck = bGlk; geldBonus = bGeld; zinsSatz = bZins;
        critChance = h.crit + bCrit; regenChance = bRegen;
    }

    public int beuteWert(int artIndex, Random rng) {
        int basis = ARTEN[artIndex].wert, wert;
        if (ARTEN[artIndex].jackpot)   // Piñata: meist wenig, selten ein fetter Jackpot
            wert = rng.nextInt(100) < 75 ? 1 + rng.nextInt(6) : basis*3 + rng.nextInt(basis*8 + 1);
        else
            wert = basis + rng.nextInt(Math.max(1, basis/2) + 1);
        return Math.max(1, (int)Math.round(wert * (1 + glueck + geldBonus)));
    }

    public int wuerfleArt(Random rng) {
        int[] gew = new int[ARTEN.length]; int summe=0;
        for (int i=0;i<ARTEN.length;i++){ gew[i]=ARTEN[i].gewicht; if(i>=2) gew[i]+=(int)Math.round(ARTEN[i].gewicht*glueck*3); summe+=gew[i]; }
        int w = rng.nextInt(summe);
        for (int i=0;i<ARTEN.length;i++){ w-=gew[i]; if(w<0){ pigFrei[i]=true; return i; } }
        return 0;
    }

    // ---------- Lauf banken ----------
    public int letzteZinsen, letzterToniAbzug, letzterInkasso, letzterAnteil;
    public long bankLauf(long laufErtrag) {
        letzteZinsen = (int)Math.round(geld * zinsSatz);
        long anteil = laufErtrag + letzteZinsen;
        letzterToniAbzug=0; letzterInkasso=0;
        if (toniSchuld>0){ letzterToniAbzug=(int)Math.min(toniSchuld, Math.round(laufErtrag*0.08)); anteil-=letzterToniAbzug; toniSchuld-=letzterToniAbzug; if (toniSchuld<=0) toniSperreBis=tag+5; }
        if (rechnungUeberfaellig()){ double rate=Math.min(0.75, 0.25+0.10*Math.max(0,tag-rechnungFaellig)); letzterInkasso=(int)Math.round(laufErtrag*rate); anteil-=letzterInkasso; }
        letzterAnteil=(int)anteil; geld+=anteil; return anteil;
    }

    public void tagWeiter(){ tag++; }
    public boolean rechnungUeberfaellig(){ return tag>rechnungFaellig; }
    public boolean kannRechnungZahlen(){ return geld>=rechnungBetrag; }

    // Zahlen -> Legacy-Punkte + (Aufrufer öffnet danach die Perk-Auswahl)
    public int letzteLegacy;
    public boolean rechnungBezahlen() {
        if (!kannRechnungZahlen()) return false;
        geld -= rechnungBetrag; gesamtBezahlt += rechnungBetrag;
        letzteLegacy = (int)Math.max(1, rechnungBetrag/100);
        legacy += letzteLegacy;
        rechnungNr++;
        rechnungBetrag = Math.round(rechnungBetrag*1.6) + 250;
        rechnungFaellig = tag + 9;
        save(); return true;
    }
    public void perkWaehlen(int perkIndex){ perks.add(perkIndex); recompute(); save(); }

    // Big Toni
    public long toniAngebot(){ return Math.max(100, rechnungBetrag - geld + 50); }
    public boolean toniVerfuegbar(){ return toniSchuld==0 && tag>=toniSperreBis; }
    public void toniLeihen(){ long b=toniAngebot(); geld+=b; toniSchuld=Math.round(b*1.10); save(); }
    public void toniTilgen(){ if(toniSchuld>0 && geld>=toniSchuld){ geld-=toniSchuld; toniSchuld=0; toniSperreBis=tag+5; } }

    // Bankrott = Prestige-Reset (Legacy/Ringe/Sammlung bleiben)
    public void bankrott() {
        geld=0; hammerIndex=0;
        for (int i=0;i<hammerBesitz.length;i++) hammerBesitz[i]=(i==0);
        for (int i=0;i<gadgetBesitz.length;i++) gadgetBesitz[i]=false;
        skills.clear(); skills.add(0);
        perks.clear();
        for (int i=0;i<pigMeisterschaft.length;i++) pigMeisterschaft[i]=0;
        for (int i=0;i<pigFrei.length;i++) pigFrei[i]=(i<2);
        toniSchuld=0; tag=1; rechnungNr=1; rechnungBetrag=750; rechnungFaellig=9;
        recompute(); save();
    }

    // Kompletter Reset (Admin-Command): ALLES weg, inkl. Legacy & Sammlung, Speicherdatei löschen.
    public void komplettReset() {
        geld=0; legacy=0; tag=1; rechnungBetrag=750; rechnungFaellig=9; rechnungNr=1; hammerIndex=0;
        for (int i=0;i<hammerBesitz.length;i++) hammerBesitz[i]=(i==0);
        for (int i=0;i<gadgetBesitz.length;i++) gadgetBesitz[i]=false;
        for (int i=0;i<prestigeBesitz.length;i++) prestigeBesitz[i]=false;
        for (int i=0;i<reliktBesitz.length;i++) reliktBesitz[i]=false;
        skills.clear(); skills.add(0); perks.clear();
        for (int i=0;i<pigMeisterschaft.length;i++) pigMeisterschaft[i]=0;
        for (int i=0;i<pigFrei.length;i++) pigFrei[i]=(i<2);
        toniSchuld=0; toniSperreBis=0; gesamtBezahlt=0;
        try { Path p=datei(); if (p!=null) Files.deleteIfExists(p); } catch (Throwable ignored) {}
        recompute();
    }

    public boolean kaufeHammer(int i){ if(hammerBesitz[i]||geld<HAEMMER[i].preis) return false; geld-=HAEMMER[i].preis; hammerBesitz[i]=true; recompute(); save(); return true; }
    public void ruesteHammer(int i){ if(hammerBesitz[i]){ hammerIndex=i; recompute(); save(); } }
    public boolean kaufeGadget(int i){ if(gadgetBesitz[i]||geld<GADGETS[i].preis) return false; geld-=GADGETS[i].preis; gadgetBesitz[i]=true; recompute(); save(); return true; }

    public boolean skillFrei(int id){ return skills.contains(id); }
    public boolean skillKaufbar(int id){ if(skills.contains(id)) return false; int pre=SKILLS[id].prereq; return (pre==-1||skills.contains(pre)) && geld>=SKILLS[id].kosten; }
    public boolean kaufeSkill(int id){ if(!skillKaufbar(id)) return false; geld-=SKILLS[id].kosten; skills.add(id); recompute(); save(); return true; }

    public boolean kaufePrestige(int i){ if(prestigeBesitz[i]||legacy<PRESTIGE[i].kosten) return false; legacy-=PRESTIGE[i].kosten; prestigeBesitz[i]=true; recompute(); save(); return true; }

    public void meisterschaftPlus(int a){ pigMeisterschaft[a]++; }
    public int pigStufe(int a){ return pigMeisterschaft[a]/25; }

    // Seltener Drop schaltet ein Relikt frei
    public int reliktDropVersuch(Random rng) {
        double chance = 0.01 + glueck * 0.05;
        if (rng.nextDouble() >= chance) return -1;
        List<Integer> fehlend = new ArrayList<>();
        for (int i=0;i<RELIKTE.length;i++) if(!reliktBesitz[i]) fehlend.add(i);
        if (fehlend.isEmpty()) return -1;
        int i = fehlend.get(rng.nextInt(fehlend.size()));
        reliktBesitz[i]=true; recompute(); save(); return i;
    }

    // ---------- Persistenz ----------
    private Path datei(){ try { return FabricLoader.getInstance().getConfigDir().resolve("cjshits.properties"); } catch(Throwable t){ return null; } }
    public void save() {
        Path p=datei(); if(p==null) return;
        try {
            Properties pr=new Properties();
            pr.setProperty("geld",Long.toString(geld));
            pr.setProperty("legacy",Integer.toString(legacy));
            pr.setProperty("tag",Integer.toString(tag));
            pr.setProperty("rechnungBetrag",Long.toString(rechnungBetrag));
            pr.setProperty("rechnungFaellig",Integer.toString(rechnungFaellig));
            pr.setProperty("rechnungNr",Integer.toString(rechnungNr));
            pr.setProperty("hammerIndex",Integer.toString(hammerIndex));
            pr.setProperty("toniSchuld",Long.toString(toniSchuld));
            pr.setProperty("gesamtBezahlt",Long.toString(gesamtBezahlt));
            pr.setProperty("hammer",flags(hammerBesitz));
            pr.setProperty("gadget",flags(gadgetBesitz));
            pr.setProperty("prestige",flags(prestigeBesitz));
            pr.setProperty("relikt",flags(reliktBesitz));
            pr.setProperty("skills",liste(skills));
            pr.setProperty("perks",liste(perks));
            StringBuilder ms=new StringBuilder(); for(int m:pigMeisterschaft) ms.append(m).append(","); pr.setProperty("meisterschaft",ms.toString());
            Files.createDirectories(p.getParent());
            try (var out=Files.newOutputStream(p)) { pr.store(out,"CJ's hits"); }
        } catch(Throwable ignored){}
    }
    public void load() {
        Path p=datei(); if(p==null||!Files.exists(p)) return;
        try (var in=Files.newInputStream(p)) {
            Properties pr=new Properties(); pr.load(in);
            geld=Long.parseLong(pr.getProperty("geld","0"));
            legacy=Integer.parseInt(pr.getProperty("legacy","0"));
            tag=Integer.parseInt(pr.getProperty("tag","1"));
            rechnungBetrag=Long.parseLong(pr.getProperty("rechnungBetrag","750"));
            rechnungFaellig=Integer.parseInt(pr.getProperty("rechnungFaellig","9"));
            rechnungNr=Integer.parseInt(pr.getProperty("rechnungNr","1"));
            hammerIndex=Integer.parseInt(pr.getProperty("hammerIndex","0"));
            toniSchuld=Long.parseLong(pr.getProperty("toniSchuld","0"));
            gesamtBezahlt=Long.parseLong(pr.getProperty("gesamtBezahlt","0"));
            readFlags(pr.getProperty("hammer",""),hammerBesitz); hammerBesitz[0]=true;
            readFlags(pr.getProperty("gadget",""),gadgetBesitz);
            readFlags(pr.getProperty("prestige",""),prestigeBesitz);
            readFlags(pr.getProperty("relikt",""),reliktBesitz);
            skills.clear(); skills.add(0);
            for (String s:pr.getProperty("skills","0").split(",")) if(!s.isEmpty()) skills.add(Integer.parseInt(s));
            perks.clear();
            for (String s:pr.getProperty("perks","").split(",")) if(!s.isEmpty()) perks.add(Integer.parseInt(s));
            String[] ms=pr.getProperty("meisterschaft","").split(",");
            for (int i=0;i<pigMeisterschaft.length&&i<ms.length;i++) if(!ms[i].isEmpty()) pigMeisterschaft[i]=Integer.parseInt(ms[i]);
            for (int i=0;i<pigFrei.length;i++) pigFrei[i]=(i<2)||pigMeisterschaft[i]>0;
        } catch(Throwable ignored){}
    }
    private String flags(boolean[] a){ StringBuilder s=new StringBuilder(); for(boolean b:a) s.append(b?'1':'0'); return s.toString(); }
    private void readFlags(String s, boolean[] a){ for(int i=0;i<a.length&&i<s.length();i++) a[i]=s.charAt(i)=='1'; }
    private String liste(java.util.Collection<Integer> c){ StringBuilder s=new StringBuilder(); for(int x:c) s.append(x).append(","); return s.toString(); }
}
