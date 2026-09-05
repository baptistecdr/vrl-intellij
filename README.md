<h3 align="center">VRL (Vector Remap Language)</h3>
<p align="center">
    An IntelliJ Platform plugin that adds language support for VRL (Vector Remap Language) — syntax
    highlighting, completion, inspections, formatting, and refactoring for <code>.vrl</code> files.
    <br>
    <a href="https://github.com/baptistecdr/vrl-intellij/issues/new">Report bug</a>
    ·
    <a href="https://github.com/baptistecdr/vrl-intellij/issues/new">Request feature</a>
</p>

<div align="center">

[![Build](https://github.com/baptistecdr/vrl-intellij/actions/workflows/build.yml/badge.svg)](https://github.com/baptistecdr/vrl-intellij/actions/workflows/build.yml)
[![Version](https://img.shields.io/jetbrains/plugin/v/33859.svg)](https://plugins.jetbrains.com/plugin/33859-vrl-vector-remap-language-)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/33859.svg)](https://plugins.jetbrains.com/plugin/33859-vrl-vector-remap-language-)

</div>

This is an unofficial plugin for [VRL](https://vector.dev/docs/reference/vrl/), the observability data
transformation language from [Vector](https://vector.dev). It isn't affiliated with the official VRL project.

## Features

- Syntax highlighting for all VRL literal kinds (strings, raw strings, regexes, timestamps, string
  interpolation) and a customizable color scheme page
- Code completion for functions, in-scope variables, and named argument names
- Parameter info (Ctrl+P) showing a function's signature while typing a call
- Quick documentation on hover for every VRL function, with a link to open its page on
  vector.dev (*Shift+F1*, or the browser icon in the Quick Documentation popup)
- Go to Declaration, Find Usages, and Rename for variables and closure parameters, with new-name
  validation against VRL's actual identifier rules
- Introduce Variable refactoring: select an expression and extract it to a new variable
- Inline Variable refactoring: replace every usage of a variable with its value and remove the
  declaration (*Ctrl+Alt+N* / *⌥⌘N*)
- Structure view (⌘7 / Ctrl+F12) listing a script's statements, including nested if statements
  and closure bodies
- Breadcrumbs showing the enclosing `if` condition(s) and closure parameters at the editor's
  bottom bar
- Code folding, brace/bracket/quote matching, line commenting (`#`)
- `# TODO`-style comments show up in the TODO tool window and "Search for TODOs"
- Spellchecking inside string literals and comments
- Reformat Code support (indentation and spacing for blocks, objects, arrays, and operators), with
  its own Code Style settings page (indent size, "Keep Blank Lines", and comma spacing)
- Language injection into string, raw string, and regex literals (*Alt+Enter → Inject language or
  reference*), plus automatic RegExp support (syntax highlighting and the "Check RegExp" intention)
  inside every `r'...'` regex literal
- Automatic VRL injection into Vector's own TOML/YAML pipeline configs - `source` under a
  `type = "remap"` transform, any plain-string `condition`, and `source` under a `vrl` codec
  (`decoding.vrl.source` / `encoding.vrl.source`) - so a `.vrl` script written inline in
  `vector.toml`/`vector.yaml` gets full editor support with no manual setup (needs the bundled
  TOML and/or YAML plugin)
- Postfix templates: `expr.iferr` expands to VRL's error-destructuring pattern
  (`target, err = expr; if err != null { }`), and `expr.raise` inserts the `!` raise-on-error
  suffix
- Live templates (type the abbreviation, then Tab): `if`, `ifel` (if/else), and `foreach`
  (a `for_each(...) -> |key, value| { }` closure)
- Smart Enter (*Ctrl+Shift+Enter* / *⌥⇧⏎*) completes an `if <condition>` line with no block into
  `if <condition> { }`, caret inside
- Inline type hints on assignments, e.g. `x: string = parse_json(...)`, including VRL's
  error-destructuring form (`value, err = fallible_call()`)
- Inline parameter name hints on positional arguments for functions with more than one parameter,
  e.g. `slice(., start: 0, end: 5)`
- Inspections:
    - Unknown function name, with a "did you mean" quick fix
    - Invalid RFC 3339 timestamp literals
    - Unhandled fallible function calls, mirroring VRL's own compile errors
    - Invalid function-call arguments (unknown/duplicate named arguments, wrong argument count,
      missing required arguments)
    - Argument type mismatches, for arguments whose value is an unambiguous literal (e.g. passing
      a number where a function expects a string)
    - Unused variables, with a "Rename to `_`" quick fix
    - Unresolved variables, with a "did you mean" quick fix
    - Empty blocks (VRL disallows `{ }` with no statements)
- VRL Playground tool window (*View → Tool Windows → VRL Playground*): run the currently open
  script against a sample event using Vector's own [`vector vrl`](https://vector.dev/docs/reference/cli/#vrl)
  subcommand, showing both the final expression's value and the resulting mutated event. The
  sample event is remembered per file. Configure the path to the `vector` executable in
  *Settings/Preferences → Tools → VRL Playground* if it isn't on your `PATH`

- Create new `.vrl` files from *File → New → VRL File*

## Quick start

Install it from the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/33859-vrl-vector-remap-language-),
or from your IDE via *Settings/Preferences → Plugins → Marketplace → search for "VRL (Vector Remap
Language)"*.

You can also build it from source (see below) and install it manually via
*Settings/Preferences → Plugins → ⚙️ → Install Plugin from Disk...*

## How to build

- Install a JDK 21
- Clone the project
- Run `./gradlew buildPlugin`
- The installable plugin zip is generated in `build/distributions/`

## Development

- Run `./gradlew test` to run the test suite
- Run `./gradlew runIde` to launch a sandbox IDE with the plugin installed
- Run `./gradlew build` to build and test everything
- The VRL function metadata (`src/main/kotlin/eu/bcosp/vrlintellij/functions/*Functions.kt`) is
  generated, not hand-maintained - a monthly [GitHub Actions workflow](.github/workflows/refresh-vrl-functions.yml)
  re-derives it from vector.dev's own [function reference](https://vector.dev/docs/reference/vrl/functions/)
  and opens a PR with any changes for review. Run it manually with
  `node scripts/refresh-vrl-functions.mjs`, or trigger the workflow via *Actions → Refresh VRL
  functions → Run workflow*.

## Bugs and feature requests

Have a bug or a feature request? Please first search for existing and closed issues. If your problem or
idea is not addressed yet, [please open a new issue](https://github.com/baptistecdr/vrl-intellij/issues/new).

## Contributing

Contributions are welcome!

## Thanks to

- https://vector.dev/docs/reference/vrl/ — the VRL language reference this plugin implements support for
- https://github.com/JetBrains/intellij-platform-plugin-template — the project scaffold this plugin was built on
- Claude
