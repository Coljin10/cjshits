# CJ's hits – Grundgerüst (Phase 0/1)

Fabric-Mod für **Minecraft 26.2**, **Java 25**. Ziel: „Bills Must Be Paid" als GUI-Minigame nachbauen.

Dieses Paket ist das leere, lauffähige Fundament: Die Mod lädt, und mit der Taste **H**
öffnet sich ein (noch leerer) Tisch-Screen. Ab hier bauen wir das Gameplay schrittweise ein.

---

## Ehrliche Vorab-Info

Ich konnte in meiner Umgebung **nicht selbst kompilieren** (kein Zugriff auf die Gradle-/Fabric-/
Minecraft-Server von dort). Außerdem ist 26.2 brandneu. Heißt:

- Die **vier Versionswerte** in `gradle.properties` bitte einmal gegen <https://fabricmc.net/develop>
  prüfen – vor allem `fabric_version` (dort steht der aktuelle Wert; ich hab einen Platzhalter gesetzt).
- Falls der erste Build eine **API-Fehlermeldung** wirft (falscher Klassen-/Methodenname), ist das
  normal bei einer frischen MC-Version. Schick mir einfach die Fehlerzeilen, dann fixen wir das in 2 Minuten.

---

## Voraussetzungen

- **JDK 25** installiert (z. B. Temurin 25). Prüfen: `java -version` → sollte 25 zeigen.
- **VS Code** mit der Erweiterung *Extension Pack for Java* (und optional *Gradle for Java*).

## Projekt lauffähig machen (Windows)

Der Gradle-Wrapper ist **schon dabei** – du musst also nichts installieren außer JDK 25.

1. Zip in einen Ordner **ohne Leerzeichen/Apostroph** im Pfad entpacken (z. B. `C:\dev\cjshits`).
   Grund: `...\CJ's Hits` mit Leerzeichen und `'` macht in Terminals oft Ärger.
2. Genau **einen** Wert eintragen: In `gradle.properties` die Zeile
   `fabric_version=FABRIC_API_VERSION_FUER_26.2_EINTRAGEN` durch den echten Wert von
   <https://fabricmc.net/develop> ersetzen. (`minecraft_version`, `loader_version` stehen schon;
   `loom` = `1.17-SNAPSHOT` steckt in `settings.gradle` und passt.)
3. Ordner in **VS Code** öffnen (Datei → Ordner öffnen → den `cjshits`-Ordner).

## Bauen & Testen

Terminal in VS Code öffnen (Terminal → Neues Terminal – ist automatisch im Projektordner). Dann:

- **Fertige .jar bauen:** `.\gradlew build`  → liegt danach unter `build\libs\`.
- **Dev-Client direkt starten:** `.\gradlew runClient`

Der **erste** Aufruf lädt Gradle 9.5.1 + Minecraft + alle Abhängigkeiten – das dauert ein paar
Minuten und braucht Internet. Danach geht's schnell.

Falls du das Terminal *nicht* in VS Code nutzt: erst mit `cd` in den Projektordner wechseln, z. B.
`cd C:\dev\cjshits`, dann `.\gradlew build`.

- **Über CurseForge spielen:** dort ein **Fabric-Profil für 26.2** anlegen, **Fabric API** dazu,
  die gebaute `.jar` aus `build\libs\` in den `mods`-Ordner legen, Welt starten, **H** drücken.

## Was schon drin ist

- Projekt-/Build-Konfiguration (Fabric Loom, Java 25, Mojang-Mappings)
- `CjsHits` (gemeinsamer Init) + `CjsHitsClient` (Client-Init mit Taste **H**)
- `TischScreen` – der leere Haupt-Screen, in den als Nächstes alles reinwächst
- Mod-Icon, Sprachdateien (de/en)

## Nächste Schritte (grob)

1. Tisch-Screen mit Ausdauer-Leiste + auto-schlagendem Hammer + ersten Sparschweinen
2. Beute/Münzen (1/5/25/100) + „Müde Hand"-Auswertung mit Zinsen
3. Reiter Rechnungen · Skilltree · Shop · Sammlung
4. Prestige/Legacy, dann Ausbau (großer Skilltree, eigene Features)
