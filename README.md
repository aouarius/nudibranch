# Nudibranche

Android-App, mit der Taucher Nacktschnecken (Nudibranchia) fotografieren und als Pokémon-artige Sammelkarten freischalten.

Eine Karte wird freigeschaltet, wenn ein beim Tauchen gemachtes Foto von der App als passende Art erkannt wird.

Der Plan steht in [docs/plan.md](docs/plan.md).

## Aufbau

- `core/`: reine Kotlin-Logik ohne Android (Artenliste, Fotoprüfung, Sammlung) mit Unit-Tests.
- `app/`: die Android-App (Jetpack Compose).
- `app/src/main/assets/land.json`: Landumrisse für den Globus, aus [Natural Earth](https://www.naturalearthdata.com) (gemeinfrei, 1:110 Mio.).
- `app/src/main/assets/species.json`: die Artenliste. Die Angaben zu Größe, Tiefe, Nahrung und Lebensraum sind vorläufig und müssen noch gegen eine Artdatenbank geprüft werden.
- `app/src/main/assets/species_photos/`: ein Referenzfoto pro Art für die Bibliothek, von [iNaturalist](https://www.inaturalist.org) unter CC0, CC BY oder CC BY-SA. Urheber, Lizenz und Quelle stehen pro Art in `species.json` und werden in der App angezeigt.
- `app/src/main/assets/species_model.tflite` und `species_labels.txt`: das Erkennungsmodell für die Top-3-Vorschläge beim Foto-Import (MobileNetV3 mit eigenem Kopf, ca. 3 MB, läuft auf dem Handy). Trainiert mit bis zu 300 Creative-Commons-Fotos pro Art von iNaturalist über den Workflow „Train species model“ (Push auf den Branch `train-species-model`); Modell, Labels und ein Bericht mit der Trefferquote landen dort in `model-output/` und werden von Hand nach `assets` kopiert. Die Trainingsfotos selbst kommen nicht in die App. Neue Arten brauchen ein neues Training.
- `tools/fetch_species_photos.py`: holt Fotokandidaten für neue Arten. Läuft über den Workflow „Fetch species photos“ (Push auf den Branch `fetch-species-photos`), der die Kandidaten zum Aussuchen auf diesen Branch committet. Liegt dort `tools/new_species_candidates.json` (Liste aus `id`, `latinName`), werden statt der vorhandenen Arten diese nachgeschlagen; der Bericht enthält dann auch Familie, Namen und die Zahl der iNaturalist-Beobachtungen.
- Seltenheit neuer Arten: ab 500 iNaturalist-Beobachtungen „Häufig“, ab 100 „Selten“, darunter „Legendär“ (Zahl in `species.json` als `inatObservations`).

## Bauen

```
./gradlew :core:test :app:assembleDebug
```

Jeder Push baut die App auch auf GitHub Actions. Die fertige Test-APK liegt dort unter dem jeweiligen Lauf bei den Artefakten (`nudibranche-debug-apk`).
