# Nova Client

Клиентский мод с читами для **Minecraft 1.21.11 (Fabric)**. Сделан для своего одиночного мира и игры с друзьями по LAN: всё работает на стороне клиента, обходов античитов нет. На чужих серверах за такие моды банят — не используй его там.

## Установка

1. Поставь [Fabric Loader](https://fabricmc.net/use/installer/) для 1.21.11 (нужна Java 21).
2. Собери мод: `./gradlew build` (в Windows — `gradlew.bat build`).
3. Скопируй `build/libs/nova-client-1.0.0.jar` в папку `.minecraft/mods`.

Fabric API не нужен. Друзьям достаточно положить тот же jar себе в `mods`.

## Управление

| Действие | Как |
| --- | --- |
| Открыть меню | `Right Shift` |
| Включить / выключить функцию | ЛКМ по названию |
| Настройки и привязка клавиши | ПКМ по названию |
| Привязать клавишу | ЛКМ по строке `Bind`, затем нужная клавиша (`Esc` / `Delete` — убрать) |
| Изменить число | Потянуть ползунок |
| Свернуть панель | ПКМ по заголовку |
| Передвинуть панель | Перетащить за заголовок |
| Приближение (Zoom) | Держать `C` |

Состояние функций, настройки и клавиши сохраняются в `config/nova.json`.

## Функции

**Combat** — KillAura, TriggerBot, Criticals, Velocity, AutoTotem, Reach

**Movement** — Fly, Speed, Sprint, NoFall, Step, HighJump, AirJump, Spider, Jesus, NoSlow, AutoWalk

**Render** — Fullbright, ESP (контур / рамки), Tracers, StorageESP, Xray, Zoom, HUD

**Player** — AutoTool, FastPlace, FastBreak, AutoEat, AutoRespawn, ChestStealer, AutoFish

**World** — Nuker, Scaffold, Timer

Описание каждой функции показывается внизу меню при наведении.

## Ограничения

- Xray меняет стандартную сборку чанков и не работает вместе с Sodium и другими модами, которые её заменяют.
- KillAura по умолчанию не трогает игроков — включи `Players` в её настройках, если нужно.
- Reach ограничен тем, что принимает сервер: до 6 блоков для ударов и 5.5 для блоков.

## Разработка

```
./gradlew runClient   # запустить игру с модом
./gradlew build       # собрать jar
```

Новая функция — это класс-наследник `Module` в `src/main/java/dev/nova/module`, добавленный в список в `Modules`.
