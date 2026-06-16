<div align="center">

# 🌸 Mudae Mod

### Coleccioná personajes de anime y manga dentro de Minecraft

Tirá rolls, clameá waifus y husbandos, y construí tu harem — todo desde un bloque interactivo con imágenes reales, powered by **AniList**.

<br>

[![Download](https://img.shields.io/badge/⬇️%20DESCARGAR%20MOD-v1.0.0-FF69B4?style=for-the-badge&logoColor=white&labelColor=1a1a2e)](https://github.com/katsu1212/mudae-mod/releases/latest/download/mudaemod-1.0.0.jar)

<br>

![NeoForge](https://img.shields.io/badge/NeoForge-21.1.233-E04E14?style=flat-square&logo=data:image/png;base64,)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square)
![Java](https://img.shields.io/badge/Java-21-5382A1?style=flat-square&logo=openjdk&logoColor=white)
![Build](https://img.shields.io/github/actions/workflow/status/katsu1212/mudae-mod/build.yml?style=flat-square&label=build)

</div>

---

## 📦 Instalación rápida

> **Requisitos:** Minecraft 1.21.1 + NeoForge 21.1.233 + Java 21

**Paso a paso:**

1. Instalá **[NeoForge 21.1.233](https://neoforged.net/)** — abrís el installer y seleccionás Minecraft 1.21.1
2. Hacé click en el botón rosa de arriba para bajar `mudaemod-1.0.0.jar`
3. Copiá el `.jar` a tu carpeta de mods:

| Sistema | Ruta |
|---------|------|
| Windows | `%AppData%\.minecraft\mods\` |
| Linux | `~/.minecraft/mods/` |
| Mac | `~/Library/Application Support/minecraft/mods/` |

4. ¡Abrí el juego — el mod se carga solo!

---

## 🏛️ El Altar de Mudae

El mod agrega un bloque especial: el **Altar de Mudae**.

Lo encontrás en el menú creativo dentro de la pestaña **"Mudae"**, o lo podés craftear (próximamente).

**Apariencia:** obsidiana llorante con tope de amatista ✨

### Cómo usarlo

1. Poné el bloque en el mundo
2. Hacé **click derecho** sobre él
3. Se abre la GUI de Mudae con imagen real del personaje, botones de roll y tu kakera

---

## 🎮 Cómo jugar

### La GUI del Altar

Cuando abrís el Altar, ves una pantalla con:

- 🖼️ **Imagen del personaje** — se descarga automáticamente de AniList
- ✨ **Nombre del personaje** y anime de origen
- 💎 **Valor en kakera** que vale ese personaje
- Tu **total de kakera** en la esquina superior

**Botones disponibles:**

| Botón | Acción |
|-------|--------|
| 🎲 **Waifu** | Invocar un personaje femenino aleatorio |
| 🎲 **Husbando** | Invocar un personaje masculino aleatorio |
| 💍 **Claim** | Quedarte el personaje que apareció |

### Cómo funciona el sistema

```
Vos tirás un roll  →  aparece el personaje en pantalla para TODOS
         ↓
Cualquiera que tenga la GUI abierta puede clickear 💍 Claim
         ↓
El primero que claimea se lo queda (ventana de 3 minutos)
         ↓
Ganás kakera igual al Mudae original
```

### Comandos de chat

| Comando | Descripción |
|---------|-------------|
| `/harem` | Ver tu colección completa de personajes (con links a imágenes) |
| `/kakera` | Ver cuántos kakera tenés |

---

## ✨ Features

- 🖼️ **Imágenes reales** — descarga la foto de cada personaje de AniList en tiempo real
- 🎲 **100k+ personajes** — waifus y husbandos de prácticamente todo el anime y manga conocido
- 💍 **Sistema de claims competitivo** — todos ven el roll y compiten por claimear
- 💎 **Kakera** — moneda que ganás al claimear, valor único por personaje
- ⏳ **Cooldowns iguales al Mudae original** — 10 rolls por hora, 1 claim cada 3 horas
- 🌐 **Multiplayer** — funciona en servidores, el roll se anuncia en el chat global
- 💾 **Datos persistentes** — tu harem y kakera se guardan entre sesiones

---

## 🤝 Multiplayer

El mod está pensado para jugarse con amigos en un servidor:

- Cuando alguien tira un roll, **aparece en el chat de todos**
- Cualquier jugador que tenga el Altar abierto puede intentar **claimear**
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

**Requisitos para compilar:** Java 21, conexión a internet (descarga NeoForge automáticamente)

---

## 📋 Changelog

### v1.0.0
- ✅ Altar de Mudae — bloque interactivo con GUI
- ✅ Imágenes reales de personajes vía AniList API
- ✅ Sistema de rolls (waifu / husbando)
- ✅ Sistema de claims con ventana de 3 minutos
- ✅ Kakera con valor único por personaje
- ✅ Cooldowns: 10 rolls/hora, 1 claim/3hs
- ✅ Persistencia de datos por jugador
- ✅ Soporte multiplayer

---

<div align="center">

Hecho con 💜 | Powered by [AniList API](https://anilist.gitbook.io/anilist-apiv2-docs/)

</div>
