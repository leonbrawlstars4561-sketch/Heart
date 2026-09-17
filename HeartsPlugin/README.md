# HeartsPlugin

Paper 26.2 / Java 25. Spieler starten mit 4 Herzen. Nur gültiger PvP-Tod entfernt Herzen. Bei genau 1 Herz wird bei ausreichendem Vault-Guthaben der konfigurierte Preis bezahlt und das Herz behalten; sonst erfolgt ein permanenter Bann. Die Daten werden per UUID in SQLite gespeichert.

## Voraussetzungen
- Paper 26.2
- Java 25
- Vault + ein Vault-kompatibles Economy-Plugin

## Installation
1. `mvn clean package`
2. `target/HeartsPlugin-1.0.0.jar` in `plugins/` kopieren.
3. Vault und eine Economy installieren.
4. Server starten.

## Commands
- `/heart` – Kauf-GUI
- `/hearts set <player> <amount>`
- `/hearts give <player> <amount>`
- `/hearts take <player> <amount>`
- `/hearts get <player>`
- `/hearts reload`
- `/hearts ban <player>`
- `/hearts unban <player>`

## Permission
`hearts.admin`

## Config
`starting-hearts`, `max-hearts`, `heart-price` und `pvp-timeout` sowie alle Nachrichten sind in `config.yml` konfigurierbar.
