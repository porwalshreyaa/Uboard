# Uboard – Your Own Space

Uboard is a lightweight desktop blackboard application built using **Java Swing**, inspired by tools like Excalidraw, Eraser, and Microsoft Paint. It provides an infinite canvas with smooth pan and zoom, multiple drawing tools, inline text editing, and a clean dark-mode interface.

The project is fully self-contained, requires no external dependencies, and serves both as a usable drawing tool and a reference implementation for interactive 2D canvas systems in Java.




## Features

### Canvas & Navigation
- Infinite canvas using world-space coordinates
- Smooth panning
  - Alt + drag (laptop / trackpad friendly)
  - Middle mouse drag
  - Shift + right mouse drag
  - Two-finger trackpad scroll
- Smooth zooming
  - Ctrl / Cmd + mouse wheel
  - Zooms relative to cursor position
- Dark background theme

### Drawing Tools
- Pen (freehand)
- Line
- Rectangle
- Circle
- Text (inline editor)
- Eraser
- Selection tool

### Editing & Interaction
- Click to select shapes
- Visual selection bounds
- Delete selected shape using Delete key
- Inline text editing
  - Enter to commit
  - Escape or focus loss to cancel
- Adjustable stroke width
- Color presets and custom color picker
- Scale-aware eraser radius

### UI
- Toolbar with toggle buttons
- Minimal dark UI
- Keyboard-focus aware canvas
- Symbol-based tool icons




## Project Structure

```text
.
├── assets/
│   ├── pen.svg
│   └── screenshot.png
├── dist/
│   └── Uboard.jar
├── icons/
│   └── icon.png
├── releases/
│   └── uboard_1.0_amd64.deb
├── src/
│   ├── Uboard.java
│   ├── CanvasPanel.java
│   ├── ToolbarPanel.java
│   ├── Tool.java
│   ├── ShapeBase.java
│   ├── EllipseShape.java
│   ├── RectShape.java
│   ├── LineShape.java
│   ├── FreehandShape.java
│   ├── TextShape.java
│   └── manifest.txt
├── website/
│   └── index.html
└── README.md

```

## Requirements

- Java 8 or later
- Desktop environment capable of running Swing applications


## Installation & Running

### Linux

#### A prebuilt .deb package is available.

1. Download the .deb file from the releases/ directory

2. Install it using:
    ``` bash
    sudo dpkg -i uboard_1.0_amd64.deb
    ```
    You can also rebuild from source if needed.

### Windows

#### Windows builds are not packaged.
You must build from source using the files in the src/ directory.

1. Open Command Prompt in project root
```bash
cd path\to\uboard
```
2. Compile source files
```bash
mkdir dist
javac -d dist src\*.java
```
3. Package JAR with manifest
```bash
jar cfm dist\Uboard.jar src\manifest.txt -C dist .
```
4. Run
```bash
java -jar dist\Uboard.jar
```

(Or If you have Java 14 or newer)

You can build Native Windows EXE:
```bash
jpackage --type exe --input dist --name Uboard --main-jar Uboard.jar

```
(Add icon if you want:)
```
jpackage --type exe --input dist --name Uboard --main-jar Uboard.jar --icon icons\icon.png
```

## Running from source (Linux)

### Compile

    javac *.java

### Run

    java Uboard

### Run from JAR

    java -jar Uboard.jar

## Controls Reference

### Mouse & Trackpad

| Action | Input |
|------|------|
| Draw | Left click + drag |
| Select | Single left click |
| Pan | Alt + drag |
| Pan | Middle mouse drag |
| Pan | Shift + right mouse drag |
| Pan (trackpad) | Two-finger scroll |
| Zoom | Ctrl / Cmd + scroll |

### Keyboard

| Key | Action |
|----|-------|
| Delete | Remove selected shape |
| Escape | Cancel inline text editing |



## Architecture Overview

### Coordinate System

    screen = world * scale + translation

- Shapes are stored in world coordinates
- Pan and zoom modify a global affine transform
- Hit-testing and erasing are scale-aware

### CanvasPanel
Responsible for:
- Mouse, wheel, and keyboard input
- Pan and zoom logic
- Shape creation and management
- Inline text editor lifecycle
- Rendering and selection visualization

### ToolbarPanel
Responsible for:
- Tool selection
- Color and stroke configuration
- UI feedback and layout



## Known Limitations

- Undo button is currently a placeholder (no history stack)
- No save/load functionality
- No multi-select or grouping
- No resize handles for shapes



## Possible Extensions

- Undo/redo command stack
- Save/load (JSON, SVG)
- Export to PNG or PDF
- Shape resize and transform handles
- Multi-select and grouping
- Native touch gesture support
- Performance optimizations for large drawings



## License

Provided as-is for educational and experimental use.
You are free to modify, extend, or integrate this project.
