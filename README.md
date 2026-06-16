<div align="center">

# 🌸 Mudae Mod

### Coleccioná personajes de anime y manga dentro de Minecraft

Tirá rolls, clameá waifus y husbandos, y construí tu harem — todo desde una **Terminal de Mudae** interactiva con imágenes reales, powered by **AniList**.

<br>

[![Download](https://img.shields.io/badge/⬇️%20DESCARGAR%20MOD-v1.3-FF69B4?style=for-the-badge&logoColor=white&labelColor=1a1a2e)](https://github.com/katsu1212/mudae-mod/releases/latest/download/mudaemod-1.3.jar)

<br>

![NeoForge](https://img.shields.io/badge/NeoForge-21.1.233-E04E14?style=flat-square)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square)
![Java](https://img.shields.io/badge/Java-21-5382A1?style=flat-square&logo=openjdk&logoColor=white)
![Build](https://img.shields.io/github/actions/workflow/status/katsu1212/mudae-mod/build.yml?style=flat-square&label=build)
![Release](https://img.shields.io/github/v/release/katsu1212/mudae-mod?style=flat-square&color=FF69B4)

</div>

---

## 📦 Instalación rápida

> **Requisitos:** Minecraft 1.21.1 · NeoForge 21.1.233 · Java 21

1. Instalá **[NeoForge 21.1.233](https://neoforged.net/)** — abrís el installer y seleccionás Minecraft 1.21.1
2. Hacé click en el botón de arriba para bajar `mudaemod-1.3.jar`
3. Copiá el `.jar` a tu carpeta de mods:

| Sistema | Ruta |
|---------|------|
| Windows | `%AppData%\.minecraft\mods\` |
| Linux   | `~/.minecraft/mods/` |
| Mac     | `~/Library/Application Support/minecraft/mods/` |

4. ¡Abrí el juego — el mod se carga solo!

---

## 🖥️ Terminal de Mudae

El mod agrega la **Terminal de Mudae**, un bloque 3D con forma de PC/monitor.

### Crafteo

```
[Bloque de Amatista] [Fragmento de Amatista] [Bloque de Amatista]
[Fragmento de Amatista]    [Ojo de Ender]    [Fragmento de Amatista]
[Bloque de Amatista] [Fragmento de Amatista] [Bloque de Amatista]
```

> También disponible en el menú creativo en la pestaña **"Mudae Mod"**

### Características del bloque

- 🖥️ Forma 3D de monitor PC (pantalla de obsidiana llorante, casing de amatista, base de hierro)
- 🔄 Se orienta hacia vos al colocarlo, como una cama o un horno
- ✨ Emite luz suave
- 🔊 Hace un sonido de amatista al abrirse

---

## 🎮 Cómo jugar

### Abrir la Terminal

Poné el bloque y hacé **click derecho** → se abre la GUI

### La GUI

```
┌─────────────────────────────────────────────────┐
│  ✨ TERMINAL DE MUDAE ✨              💎 450    │
│─────────────────────────────────────────────────│
│                                                  │
│  ┌──────────┐   Rem                              │
│  │          │   📺 Re:Zero                       │
│  │  [FOTO]  │   💎 +65 kakera                    │
│  │          │   ⏳ 3 min para claimear           │
│  └──────────┘   ─────────────────               │
│                 ✨ ¡Sé el primero en clickear!  │
│─────────────────────────────────────────────────│
│  [🎲 Waifu]  [🎲 Husbando]  [💍 ¡Claim!]      │
└─────────────────────────────────────────────────┘
```

### Flujo del juego

```
Abrís la Terminal y clickeás 🎲 Waifu o 🎲 Husbando
              ↓
El personaje aparece en pantalla para TODOS en el servidor
              ↓
Cualquiera puede clickear 💍 Claim (ventana de 3 minutos)
              ↓
El primero en claimear se lo queda y gana kakera
```

### Comandos de chat

| Comando | Descripción |
|---------|-------------|
| `/harem` | Ver tu colección completa (con links a imágenes) |
| `/kakera` | Ver cuántos kakera tenés |

---

## ✨ Features

- 🖥️ **Terminal 3D** con modelo personalizado de PC/monitor
- 🖼️ **Imágenes reales** descargadas de AniList en tiempo real, escaladas con aspect ratio correcto
- 🎲 **100k+ personajes** de prácticamente todo el anime y manga conocido
- 💍 **Claims competitivos** — todos ven el roll y compiten por claimear primero
- 💎 **Kakera** con valor único por personaje (entre 20 y 99)
- ⏳ **Cooldowns** — 10 rolls por hora, 1 claim cada 3 horas
- 🔄 **Bloque orientable** — siempre mira al jugador al ser colocado
- 🌐 **Multiplayer** — funciona en servidores, rolls anunciados en el chat global
- 💾 **Datos persistentes** — harem y kakera se guardan entre sesiones

---

## 🤝 Multiplayer

- Cuando alguien tira un roll, **aparece en el chat de todos**
- Cualquier jugador con la Terminal abierta puede **claimear**
- Si nadie claimea en **3 minutos**, el personaje desaparece
- Cada jugador tiene **su propio cooldown** de rolls y claims

---

## 🔧 Compilar desde el código

```bash
git clone https://github.com/katsu1212/mudae-mod
cd mudae-mod
./gradlew build
# El JAR aparece en build/libs/mudaemod-1.0.0.jar
```

**Requisitos para compilar:** Java 21 · Conexión a internet

---

## 📋 Changelog

### v1.3 — Actual
- ✅ Bloque 3D de PC/monitor (pantalla + casing + base metálica)
- ✅ El bloque se orienta hacia el jugador al colocarlo
- ✅ Crafteo con amatistas + Ojo de Ender
- ✅ Fix de imágenes: carga correcta desde CDN de AniList con redirects
- ✅ Imágenes escaladas respetando aspect ratio (personajes retrato/paisaje)
- ✅ Botones con fondo opaco, mucho más visibles
- ✅ Nombre del bloque: "Terminal de Mudae"
- ✅ Tooltip al hacer hover sobre el botón Claim

### v1.2
- ✅ GUI del Altar con imágenes, botones integrados y kakera display
- ✅ Sistema de red cliente/servidor para rolls y claims
- ✅ Soporte multiplayer completo

### v1.0
- ✅ Sistema de rolls y claims por comandos de chat
- ✅ Integración con AniList API

---

<div align="center">

Hecho con 💜 · Powered by [AniList API](https://anilist.gitbook.io/anilist-apiv2-docs/) · [Ver releases](https://github.com/katsu1212/mudae-mod/releases)

</div>
