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

</div>

This is an unofficial plugin for [VRL](https://vector.dev/docs/reference/vrl/), the observability data
transformation language from [Vector](https://vector.dev). It isn't affiliated with the official VRL project.

## Features

- Syntax highlighting for all VRL literal kinds (strings, raw strings, regexes, timestamps, string
  interpolation) and a customizable color scheme page
- Code completion for functions, in-scope variables, and named argument names
- Quick documentation on hover for every VRL function
- Go to Declaration, Find Usages, and Rename for variables and closure parameters
- Code folding, brace/bracket/quote matching, line commenting (`#`)
- Reformat Code support (indentation and spacing for blocks, objects, arrays, and operators)
- Inline type hints on assignments, e.g. `x: string = parse_json(...)`, including VRL's
  error-destructuring form (`value, err = fallible_call()`)
- Inspections:
    - Unknown function name, with a "did you mean" quick fix
    - Invalid RFC 3339 timestamp literals
    - Unhandled fallible function calls, mirroring VRL's own compile errors

## Quick start

This plugin isn't published on the JetBrains Marketplace yet. Until then, build it from source (see
below) and install it manually via *Settings/Preferences → Plugins → ⚙️ → Install Plugin from Disk...*

## How to build

- Install a JDK 21
- Clone the project
- Run `./gradlew buildPlugin`
- The installable plugin zip is generated in `build/distributions/`

## Development

- Run `./gradlew test` to run the test suite
- Run `./gradlew runIde` to launch a sandbox IDE with the plugin installed
- Run `./gradlew build` to build and test everything

## Bugs and feature requests

Have a bug or a feature request? Please first search for existing and closed issues. If your problem or
idea is not addressed yet, [please open a new issue](https://github.com/baptistecdr/vrl-intellij/issues/new).

## Contributing

Contributions are welcome!

## Thanks to

- https://vector.dev/docs/reference/vrl/ — the VRL language reference this plugin implements support for
- https://github.com/JetBrains/intellij-platform-plugin-template — the project scaffold this plugin was built on
- Claude
