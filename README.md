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
| Выбрать категорию | Иконки в боковой панели |
| Включить / выключить функцию | ЛКМ по карточке |
| Настройки функции | Шестерёнка на карточке или ПКМ по ней |
| Привязать клавишу | ЛКМ по клавише на карточке (или СКМ по карточке), затем нужная клавиша (`Esc` — отмена, `Delete` — убрать) |
| Сменить клавишу меню | ЛКМ по `RSHIFT` в шапке |
| Изменить число | Потянуть ползунок |
| Поиск | Начать печатать или кликнуть по строке поиска |
| Темы | Клик по логотипу `N` |
| Профиль | Кнопка внизу боковой панели |
| Приближение (Zoom) | Держать `C` |

Состояние функций, настройки, клавиши, тема и список друзей сохраняются в `config/nova.json`.

## Функции

**Combat** — HitAura, TriggerBot, AutoTotem, Velocity, Criticals, Reach, HitBox, HitSounds, BowHelper, AutoGApple

**Movement** — Sprint, Speed, NoSlow, Jesus, Spider, InvMove, DragonFly, WaterSpeed, AirStuck, NoWeb, Fly, NoFall, Step, HighJump, AirJump, AutoWalk

**Visuals** — ESP, GlowESP, TargetESP, NameTags, Tracers, StorageESP, BlockESP, BlockOutline, Xray, NightVision, ChinaHat, JumpCircle, Trails, Arrows, Zoom, Hud

**Player** — FreeCamera, AutoTool, FastBreak, NoDelay, NoPush, Blink, AntiAFK, CameraClip, ElytraHelper, MiddleClick, FakePlayer, AutoFish, AutoEat, AutoRespawn, ChestStealer

**Misc** — Scaffold, Nuker, Timer, SeeInvisibles, Removals, TapeMouse

Описание функции написано на её карточке.

## Ограничения

- Xray меняет стандартную сборку чанков и не работает вместе с Sodium и другими модами, которые её заменяют.
- HitAura по умолчанию бьёт игроков и мобов, но не друзей — цели меняются в её настройках. Друзья добавляются функцией MiddleClick.
- Reach ограничен тем, что принимает сервер: до 6 блоков для ударов и 5.5 для блоков.

## Разработка

```
./gradlew runClient   # запустить игру с модом
./gradlew build       # собрать jar
```

Новая функция — это класс-наследник `Module` в `src/main/java/dev/nova/module`, добавленный в список в `Modules`.
