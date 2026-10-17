# FireClient (replica) per Minecraft 26.2

Reimplementazione indipendente, scritta da zero, delle funzioni principali di FireClient SMP per Minecraft Java 26.2 (Fabric). Non contiene codice della mod originale.

## Cosa c'è

- **Coordinate** a schermo, con coordinate scalate Overworld/Nether e direzione
- **Kit**: salva l'inventario in file .json, copia/incolla dagli appunti, carica (il caricamento richiede Creative)
- **Comandi**: `/fkit`, `/ftext`, `/fcalc`, `/fireclient`
- **Tasti**: K apre i kit, Shift destro apre le impostazioni (cambiabili in Controlli)

## Non ancora incluso

Big Items, Mute Sounds, finestra popout delle coordinate e le correzioni di bug. Richiedono mixin sul codice interno di Minecraft e vanno scritti dopo aver verificato che la base compila.

## Compilare

Serve **Java 25**.

1. Se non c'è `gradlew`, installa Gradle e lancia una volta `gradle wrapper --gradle-version 9.5.1`
2. `./gradlew build` (su Windows `gradlew.bat build`)
3. Il file si trova in `build/libs/fireclient-replica-1.0.0.jar` (non il `-sources`)
4. Mettilo in `.minecraft/mods` insieme a Fabric Loader e Fabric API per 26.2

## Comandi

- `/fkit` apre la schermata kit; `/fkit list|save|load|delete|copy|paste <nome>`
- `/ftext <testo>` converte in ѕᴍᴀʟʟ ᴛᴇхᴛ e copia negli appunti
- `/fcalc <espressione>`: `+ - * / % ^ ( )`, con suffissi `s` (64), `sb` (1728), `dc` (3456). Esempio: `/fcalc 3sb+2s`
