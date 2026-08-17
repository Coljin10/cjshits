package at.coljin.cjshits.client;

import at.coljin.cjshits.CjsHits;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Gemeinsame Zeichen-Helfer (nur bestätigte 26.2-APIs). */
public final class Ui {
    private Ui() {}

    public static Identifier tex(String name) { return Identifier.fromNamespaceAndPath(CjsHits.MOD_ID, "textures/gui/" + name + ".png"); }
    public static void img(GuiGraphicsExtractor g, String name, int x, int y, int w, int h) {
        g.blit(RenderPipelines.GUI_TEXTURED, tex(name), x, y, 0, 0, w, h, w, h);
    }
    /** Wie img, aber skaliert die Textur (texW×texH) auf dw×dh – für bildschirm-relative Größen. */
    public static void imgS(GuiGraphicsExtractor g, String name, int x, int y, int dw, int dh, int texW, int texH) {
        g.blit(RenderPipelines.GUI_TEXTURED, tex(name), x, y, 0f, 0f, dw, dh, texW, texH, texW, texH);
    }
    /** Zeichnet nur einen Teilbereich (u,v,regW,regH) einer Textur, skaliert auf dw×dh. */
    public static void imgReg(GuiGraphicsExtractor g, String name, int x, int y, int dw, int dh, int u, int v, int regW, int regH, int texW, int texH) {
        g.blit(RenderPipelines.GUI_TEXTURED, tex(name), x, y, (float)u, (float)v, dw, dh, regW, regH, texW, texH);
    }
    public static void text(GuiGraphicsExtractor g, Font f, String s, int x, int y, int color) { g.text(f, Component.literal(s), x, y, color, true); }
    public static void center(GuiGraphicsExtractor g, Font f, String s, int cx, int y, int color) { Component c=Component.literal(s); g.text(f, c, cx - f.width(c)/2, y, color, true); }
    public static int w(Font f, String s) { return f.width(Component.literal(s)); }

    /** Der Schreibtisch-Hintergrund (Holz, Fensterzeile oben, Deko in den Ecken).füllt den Screen. */
    public static void desk(GuiGraphicsExtractor g, int w, int h) {
        int wy = Math.max(28, (int)(h * 0.15));
        g.fill(0, 0, w, wy, 0xFF14101A);                                   // Fenster/Straße oben
        g.fill((int)(w*0.47),(int)(wy*0.40),(int)(w*0.53),(int)(wy*0.54),0xFFC03848); // kleine Leuchtschilder
        g.fill((int)(w*0.19),(int)(wy*0.55),(int)(w*0.23),(int)(wy*0.66),0xFF3A66A0);
        g.fill((int)(w*0.71),(int)(wy*0.48),(int)(w*0.745),(int)(wy*0.58),0xFF40A060);
        g.fill(0, wy-2, w, wy, 0xFF2A2028);                               // Fensterbrett
        g.fill(0, wy, w, h, 0xFF6B4A2E);                                   // Holz
        g.fill(0, wy, w, wy+3, 0xFF8A6038);                               // Vorderkante
        int step = Math.max(16, h/18);
        for (int y=wy+step; y<h; y+=step) g.fill(0, y, w, y+2, 0xFF573A24); // Maserung
        // Deko in nativen Größen (kein Skalier-blit)
        img(g,"prop_lampe",   6,        wy-2,     40, 40);
        img(g,"prop_radio",   w-54,     wy,       48, 36);
        img(g,"prop_rechner", 6,        h-68,     40, 54);
        img(g,"prop_tasse",   w-50,     h-50,     44, 44);
        img(g,"prop_papiere", 6,        h/2-20,   48, 40);
    }

    /** Tooltip-Kasten(opak) an der Maus,bleibt auf dem Screen. Erste Zeile = Titel. */
    public static void tooltip(GuiGraphicsExtractor g, Font f, String[] lines, int mx, int my, int sw, int sh) {
        int wmax=0; for (String s:lines) wmax=Math.max(wmax, w(f,s));
        int bw=wmax+16, bh=lines.length*12+8;
        int bx=Math.min(mx+12, sw-bw-4), by=Math.min(my+8, sh-bh-4);
        if (bx<2) bx=2; if (by<2) by=2;
        g.fill(bx-1,by-1,bx+bw+1,by+bh+1,0xFFF0C060);
        g.fill(bx,by,bx+bw,by+bh,0xFF120C08);
        for (int i=0;i<lines.length;i++) g.text(f, Component.literal(lines[i]), bx+8, by+6+i*12, i==0?0xFFE0C060:0xFFCFCFCF, false);
    }
}
