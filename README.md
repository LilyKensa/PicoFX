# PicoFX

My another attempt at recreating [Pico-8](https://www.lexaloffle.com/pico-8.php) on other platforms

Well, actually it's just the runner, no console, no editor :)

---

## Origin

One of my stupid courses in college requires us to make a game on JavaFX as the project of the semester

Not gonna lie, it's just ridiculous. I mean, this shit is released at May 2007, it's freaking older than me! 

I couldn't help but think that the teachers are too lazy to learn anything so they just keep on teaching the same thing every year...

So if you're checking this out because you wanna make something yourself, don't choose JavaFX, I 100% do not recommend. Use SvelteKit + Tauri if you wanna make a frontend app, Unity if you wanna make a game

Alright, that's enough yapping

## How It Works

I couldn't find a way to write shaders, so the rendering is all on CPU, I basically just put a low resolution canvas and draw an image with the same size, so it can be perfectly pixelated. All the work is done on the pixel buffer of that image

### Lifecycle

The main class, `PicoFX`, basically does nothing, it's just a loader that runs `Entry`

The `Entry` class is the main wrapper, it has the screen buffer, tick cycle, and key listeners

### Bases

| Class       | Description                                       |
|-------------|---------------------------------------------------|
| `Vec`       | 2D vector                                         |
| `Lifecycle` | Anything that has `start`, `update`, and `render` |
| `Game`      | Now just `Lifecycle` with name                    |

### Items

| Class     | Explaination                                       |
|-----------|----------------------------------------------------|
| `Sprite`  | Images, can be cut off from bigger sprites (sheet) |
| `Font`    | Fonts for printing                                 |
| `Sound`   | Sounds for emitting                                |
| `TileMap` | Tiles for building maps                            |

### Modules

| Class      | Scenario                             |
|------------|--------------------------------------|
| `Config`   | Configuration                        |
| `Graphics` | Drawing anything on the screen       |
| `Inputs`   | Getting pressed keys                 |
| `Audios`   | Emitting sounds                      |
| `Utils`    | Utilities mainly for internal usages |

---

There's not a lot of methods, my code should be self-explanatory, please refer to the `SuperDiscBox` class for example!

## Known Issues

- Rare screen flickering

## To-do List

- Add controller support