# Plan (Stand 2026-10-08)

## Erkennung
- Eigenes Modell auf dem Gerät (LiteRT), trainiert mit Fotos aus iNaturalist (CC-Lizenzen beachten).
- Bei unsicherem Ergebnis zeigt die App 2–3 Vorschläge, der Taucher wählt.
- Kleine, ähnliche Arten werden teils nur bis zur Gattung bestimmt.

## Schutz vor Schummeln
- EXIF-Prüfung (Datum, Kameramodell, GPS falls vorhanden).
- Bilder ohne EXIF oder mit Screenshot-Merkmalen werden abgelehnt.
- Abgleich mit bekannten Online-Fotos (Bild-Hash, Rückwärtssuche).
- Optional: Logbuch-Eintrag oder Import vom Tauchcomputer als Nachweis.
- Import aus der Galerie muss möglich sein.

## Karten
- Seltenheit aus der Zahl der iNat-Beobachtungen.
- Typen nach Region oder Familie.
- Infos: Größe, Tiefe, Nahrung, Lebensraum (keine Abwehr-Werte).
- Holo-Variante für besonders gute Fotos oder Erstfunde in einer Region.
- Kartenbild ist das eigene Foto des Tauchers.
- Stil: dunkler Manga-Look mit Schraffur und Rasterton.
- Alle Regionen offen, Arten als Liste mit Region als Feld.

## Technik
- Kotlin, Jetpack Compose, Room (lokal), LiteRT (Erkennung).
- Supabase oder Firebase für Konten und Ranglisten.
- Kartenlayout und Texte als Konfiguration, nicht fest im Code.

## MVP
1. Kleine Artenliste (eine Region als Startpunkt).
2. Kartenkatalog mit gesperrten und freigeschalteten Karten.
3. Foto importieren, EXIF prüfen, Modell schlägt Art vor, Karte wird freigeschaltet.
4. Danach: Anti-Cheat ausbauen, weitere Regionen, Tausch und Social.

## Offen
- Ob ein KI-Bildgenerator für Karten-Motive genutzt wird (Lizenz und Kennzeichnung klären).
- Lizenz der Trainingsbilder: bei kommerzieller Nutzung nur CC-BY/CC0.
