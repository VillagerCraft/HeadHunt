# HeadHunt



A Paper plugin for Minecraft that runs **head hunts**: admins hide **player skull blocks** in the world; players search for them and earn **configurable rewards** when they click to register a find.



> **Status:** **v1 ready** — Sprints **1–9** complete. Product spec: [`.cursor/prd/`](.cursor/prd/).

## Implementation status

**Current milestone:** Sprint 9 — Hardening (complete) — v1 ready

**MVP:** Sprints 1–5 · **v1:** Sprints 1–9 (complete)

## How it works



- **One active hunt** at a time, made up of one or more **named sets** of heads (sets created via command).

- Admins configure a hunt template in `config.yml`, then `/headhunt create <name>` (name must match config). The hunt starts **inactive** with **no schedule** (null start/end in runtime data).

- Admins create sets and register heads on that hunt while it is inactive, then `/headhunt activate` when ready for players to find heads.

- Admins create sets with `/headhunt set create <name>`, then register skull blocks with `/headhunt add <set> <headName>` (after create, before or after activate).

- Players **click** heads to register finds (left/right/both configurable; requires `headhunt.play.find`).

- **Console commands** run as rewards on head find, set completion, and hunt completion.

- **Leaderboard**, progress inspection, and “most found head” stats — all via chat commands for now.

- Hunt **schedule** (start/end) is optional in runtime `plugins/HeadHunt/data/active-hunt.yml` (template in `config.yml` is not applied on create); use `/headhunt pause` / `resume` to freeze finds.



See [`.cursor/prd/product-spec.md`](.cursor/prd/product-spec.md) for the full design.



## Features (by sprint)

Update checkboxes when each sprint completes (see the sprint doc for the exact list).

### MVP (Sprints 1–5)

- [x] **Sprint 1 — Bootstrap** — plugin foundation, config/messages, storage interfaces, MiniMessage, command scaffold
- [x] **Sprint 2 — Hunt lifecycle** — create (inactive), activate, pause/resume, schedule state
- [x] **Sprint 3 — Sets and heads** — set create/list, add/remove, break protection
- [x] **Sprint 4 — Click-to-find** — configurable interaction mode, find records
- [x] **Sprint 5 — Rewards** — three-tier console commands (head → set → hunt)

### Post-MVP v1 (Sprints 6–9)

- [x] **Sprint 6 — Delete, archive & reset** — delete hunt, archive finds + completions, `--delete-heads`, progress reset
- [x] **Sprint 7 — Banlist** — ban/unban, silent block on finds/rewards, leaderboard exclusion
- [x] **Sprint 8 — Query & stats** — leaderboard, progress, inspect, popular heads
- [x] **Sprint 9 — Hardening** — offline names, edge cases, reload messages, v1 test matrix



## Requirements



- **Java 21**

- **Paper** server **1.21.11** (or compatible build)

No PlaceholderAPI required. Reward commands use built-in placeholders (`{player}`, `{head}`, etc.).



## Build



```bash

mvn clean package

```



The shaded plugin JAR is written to `target/`. Copy it into your server's `plugins/` directory and restart (or use a plugin manager reload if appropriate).



## Installation



1. Build or download the latest `HeadHunt-*.jar`.

2. Drop the file into `plugins/`.

3. Start or restart the Paper server.

4. Edit `plugins/HeadHunt/config.yml` and `messages.yml`, then create and run a hunt (see [product spec](.cursor/prd/product-spec.md#admin-workflows)).



## Commands

### Implemented

| Command | Permission | Description |
|---------|------------|-------------|
| `/headhunt create <name>` | `headhunt.admin.hunt.create` | Create hunt from config snapshot (inactive, no schedule) |
| `/headhunt reload` | `headhunt.admin.hunt.create` | Reload `messages.yml` from disk (does not change hunt or config snapshot) |
| `/headhunt activate` | `headhunt.admin.hunt.activate` | Enable find registration (requires at least one registered head) |
| `/headhunt pause` / `resume` | `headhunt.admin.hunt.schedule` | Pause or resume find registration |
| `/headhunt set create <name>` | `headhunt.admin.set.create` | Create a named set in the current hunt |
| `/headhunt set list` | `headhunt.admin.set.list` | List sets in the current hunt |
| `/headhunt add <set> <headName>` | `headhunt.admin.head.add` | Register targeted player skull in the current hunt |
| `/headhunt remove` | `headhunt.admin.head.remove` | Unregister targeted head |
| `/headhunt delete [--delete-heads]` | `headhunt.admin.hunt.delete` | Delete hunt; archive finds + completions; optional block removal |
| `/headhunt reset <player> <hunt\|set\|head> <identifier>` | `headhunt.admin.player.reset` | Reset player progress (online or cached offline name) |
| `/headhunt ban <player>` / `unban <player>` | `headhunt.admin.player.ban` | Hunt banlist (online or cached offline name) |
| `/headhunt leaderboard` | `headhunt.play.leaderboard` | Active hunt leaderboard |
| `/headhunt progress [player]` | `headhunt.play.progress.self` / `.other` | Found heads for a player (requires ≥1 find) |
| `/headhunt inspect` | `headhunt.admin.head.inspect` | Who found the targeted head |
| `/headhunt popular` | `headhunt.play.stats.popular` | Heads ranked by distinct finders |

Unknown subcommands return the `command-usage` message.

**Reload:** `/headhunt reload` reloads `messages.yml` only. `config.yml` is re-read on `/headhunt create` only; the active hunt snapshot and schedule in `data/active-hunt.yml` are unchanged by reload.

**Schedule (v1):** Optional start/end in `data/active-hunt.yml` (ISO-8601). Values in shipped `config.yml` are a template and are not copied on create.



## Permissions



| Permission | Default | Description |

|------------|---------|-------------|

| `headhunt.admin.hunt.create` | op | Create hunt (inactive) |

| `headhunt.admin.hunt.activate` | op | Activate hunt for finds |

| `headhunt.admin.hunt.delete` | op | Delete hunt |

| `headhunt.admin.hunt.schedule` | op | Pause, resume |

| `headhunt.admin.set.create` | op | Create set |

| `headhunt.admin.set.list` | op | List sets |

| `headhunt.admin.head.add` | op | Register head |

| `headhunt.admin.head.remove` | op | Unregister head |

| `headhunt.admin.head.inspect` | op | Inspect head finders |

| `headhunt.admin.player.reset` | op | Reset player progress |

| `headhunt.admin.player.ban` | op | Ban/unban players |

| `headhunt.play.find` | true | Register finds by clicking |

| `headhunt.play.progress.self` | true | View own progress |

| `headhunt.play.progress.other` | op | View another player's progress |

| `headhunt.play.leaderboard` | true | View leaderboard |

| `headhunt.play.stats.popular` | true | View most-found heads |



## Configuration



| File | Purpose |

|------|---------|

| `config.yml` | Click mode, hunt name, schedule, reward commands, broadcast toggles |

| `messages.yml` | MiniMessage player- and admin-facing text |



Message placeholders (in `messages.yml`, substituted before MiniMessage parsing): `{head}`, `{set}`, `{sets}`, `{hunt}`, `{player}`, `{name}`, `{expected}`, `{reason}`.

See [configuration.md](.cursor/prd/configuration.md#messagesyml-conceptual) for the full key list.

Reward command placeholders (console only): `{player}`, `{uuid}`, `{head}`, `{set}`, `{hunt}`, `{finds}`, `{set_finds}`, `{set_total}`.

On `/headhunt create`, config is snapshotted into runtime data under `plugins/HeadHunt/data/` (hunt inactive, schedule null in `active-hunt.yml`). Use `/headhunt activate` to enable finds (at least one head required). Optional schedule: edit `active-hunt.yml`. Use `/headhunt reload` to refresh `messages.yml` without touching hunt data. Deleted hunts archive finds and completions under `data/archives/<hunt-name>.yml` (not queryable in-game).



## License



This project is licensed under the **MIT License** — see [LICENSE](LICENSE).



## Development

- Product spec: [`.cursor/prd/`](.cursor/prd/)
- Implementation sprints: [`.cursor/prd/sprints/`](.cursor/prd/sprints/)
- Cursor rules: [`.cursor/rules/`](.cursor/rules/)
- Agent notes: [AGENTS.md](AGENTS.md)


