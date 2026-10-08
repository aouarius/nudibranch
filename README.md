# Nudibranche

Android-App, mit der Taucher Nacktschnecken (Nudibranchia) fotografieren und als Pokémon-artige Sammelkarten freischalten.

Eine Karte wird freigeschaltet, wenn ein beim Tauchen gemachtes Foto von der App als passende Art erkannt wird.

Der Plan steht in [docs/plan.md](docs/plan.md).

## Aufbau

- `core/`: reine Kotlin-Logik ohne Android (Artenliste, Fotoprüfung, Sammlung) mit Unit-Tests.
- `app/`: die Android-App (Jetpack Compose).
- `app/src/main/assets/land.json`: Landumrisse für den Globus, aus [Natural Earth](https://www.naturalearthdata.com) (gemeinfrei, 1:110 Mio.).
- `app/src/main/assets/species.json`: die Artenliste. Die Angaben zu Größe, Tiefe, Nahrung und Lebensraum sind vorläufig und müssen noch gegen eine Artdatenbank geprüft werden.

## Bauen

```
./gradlew :core:test :app:assembleDebug
```

Jeder Push baut die App auch auf GitHub Actions. Die fertige Test-APK liegt dort unter dem jeweiligen Lauf bei den Artefakten (`nudibranche-debug-apk`).
