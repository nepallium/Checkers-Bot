# Deliverable 3 Files

# Checkers vs Bot

A single-player checkers game where you challenge an AI opponent at three difficulty levels, with a clean UI, undo support, and customizable aesthetics.

---

## Table of Contents

- [Getting Started](#getting-started)
- [How to Play](#how-to-play)
- [Rules of Checkers](#rules-of-checkers)
- [Difficulty Levels](#difficulty-levels)
- [Settings](#settings)
- [Menu Reference](#menu-reference)

---

## Getting Started

1. Launch the application.
2. Select a difficulty level — **Easy**, **Medium**, or **Hard** — from the dropdown or selector on the main screen.
3. Click **Play** to start the game.
4. You play as the light pieces; the bot plays as the dark pieces and moves first or second depending on the configuration.

---

## How to Play

**Moving a piece**

1. Click on one of your pieces to select it. The valid destination squares will be highlighted.
2. Click on a highlighted square to move the selected piece there.
3. If you change your mind before confirming the move, click a different one of your pieces to re-select, or click the selected piece again to deselect it.

**Undo Move**

Click the **Undo Move** button to reverse your last move and restore the board to its previous state. This can be used once per turn.

---

## Rules of Checkers

If you are unfamiliar with checkers, here is a quick overview of the rules used in this game.

**The board and pieces**

The game is played on an 8×8 board. Each player starts with 12 pieces placed on the dark squares of the three rows closest to them. Pieces may only ever occupy dark squares.

**Movement**

Ordinary pieces move diagonally forward one square at a time. They cannot move backward.

**Capture (jumping)**

If an opponent's piece is diagonally adjacent and the square directly beyond it is empty, you must jump over it and remove it from the board. This is known as a **capture**.

**Forced capture**

Captures are mandatory. If a capture is available on your turn, you must take it — you cannot make a regular move instead. If multiple captures are available, you may choose which one to take.

**Chain jumps**

After completing a capture, if your piece lands on a square from which another capture is immediately available, you must continue jumping in the same turn. This chain continues until no further captures are possible.

**Kings**

When one of your pieces reaches the farthest row on the opponent's side of the board, it is crowned a **King** and gains the ability to move and capture diagonally in any direction, both forward and backward.

**Winning**

You win by capturing all of your opponent's pieces, or by leaving them with no legal moves on their turn.

**Draw**

If neither player captures a piece for 40 consecutive moves, the game is declared a **draw**. This prevents games from continuing indefinitely when neither side can make progress.

---

## Difficulty Levels

| Level  | Description |
|--------|-------------|
| **Easy**   | The bot makes mostly random legal moves with minimal lookahead. Good for beginners or casual play. |
| **Medium** | The bot evaluates captures and basic positioning a few moves ahead. A balanced challenge. |
| **Hard**   | The bot uses a deeper search and prioritizes king promotion, forced captures, and positional control. Expect a real fight. |

You can only select a difficulty before a game begins. To change difficulty mid-session, exit the current game and start a new one.

---

## Settings

Open **Settings** from the top menu bar to customize the look of the game. Options include:

- **Board theme** — change the color scheme of the board squares.
- **Piece style** — choose between different visual styles for the pieces.
- **Background** — adjust the background color or texture of the game window.

Changes apply immediately and are saved for future sessions.

---

## Menu Reference

| Menu | Option | Action |
|------|--------|--------|
| **File** | Exit | Closes the application. |
| **Settings** | *(opens panel)* | Opens the aesthetic settings panel. |

---

## JAR Launch

To run the project using the compiled jar file, use this command :


"""bash
C:\Program Files\Java\jdk-22\bin\java.exe" --module-path "C:\Users\6305020\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar Checkers-1.0-SNAPSHOT.jar
"""
