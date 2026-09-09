---
name: implement-vrl-feature
description: Implements a new feature or bugfix in the vrl-intellij plugin (VRL language support for IntelliJ) with evidence-based verification instead of guesswork. Use this whenever the user asks to add, implement, or fix a plugin feature - completion, inspections, injection, formatting, refactoring, documentation, code style, diagnostics, and anything else touching VRL/Vector semantics or the IntelliJ Platform API - even if they don't say "use the skill" or name it explicitly. Also use it when the user reports a bug in existing plugin behavior ("X doesn't work", "Y is wrong for this case"), since fixing a bug follows the same verify-then-implement discipline. Do not use it for pure documentation edits, dependency bumps, or CI/build-config changes that don't touch VRL or IntelliJ Platform behavior.
---

# Implement a vrl-intellij feature

This plugin's entire value is that it tells the user true things about VRL and about
their IDE. A completion suggestion, an inspection warning, or a quick fix that's wrong is
worse than no feature at all - it actively misleads someone editing a `.vrl` file. So the
rule that governs everything below is: **verify against a real, authoritative source before
writing code that encodes a claim about VRL or the platform.** Vector's docs are useful but
not authoritative - they lag the compiler and occasionally state things the compiler
disagrees with. The compiler and the source are authoritative.

Treat every step below as sequential and don't skip ahead - implementing before verifying,
or committing before the suite is green, is exactly the failure mode this skill exists to
prevent.

## 1. Research the ground truth

Before writing any implementation code, pin down what's actually true. Depending on what the
feature touches:

- **VRL language behavior** (a function's arguments, fallibility, return type, an enum of
  accepted values, syntax acceptance, literal formats): check both
  [vector.dev's docs](https://vector.dev/docs/reference/vrl/) *and* the
  [vectordotdev/vrl](https://github.com/vectordotdev/vrl) source, since the two sometimes
  disagree and the source wins. Fetch real files from
  `https://raw.githubusercontent.com/vectordotdev/vrl/main/<path>` rather than trusting
  memory of what a stdlib function or the parser does. If a third-party crate is involved
  (chrono for timestamps, `regex`/`regex-syntax` for regex literals, etc.), check
  `vrl/Cargo.lock` for the exact pinned version and fetch *that* version's source - a crate's
  behavior can and does change between versions.
- **Vector config semantics** (TOML/YAML shapes, injection targets, what a transform/source/sink
  accepts): check both vector.dev's config reference and the
  [vectordotdev/vector](https://github.com/vectordotdev/vector) source (`src/`, `lib/codecs/`).
- **IntelliJ Platform API behavior** (an inspection host interface, an extension point's exact
  attribute name, what a platform annotator actually checks): the platform's own source and
  bytecode are authoritative over assumptions about how an EP "probably" works. See the
  decompilation recipe below - guessing platform API signatures wastes a compile-fail cycle at
  best and silently no-ops at worst.

Write down what you find as you go; it becomes the evidence you cite in code comments, test
names, and the commit message later. Don't discard it once implementation starts.

### Decompiling the pinned Platform SDK when its behavior is unclear

The compile target is pinned in `build.gradle.kts` (`intellijIdea("...")`). When you need to
know a platform interface's exact method signatures, which of them are actually consulted by
a given annotator/inspection, or what extension point wires a capability in - don't guess or
rely on a newer/older cached SDK build. Find the *exact pinned version* first:

```bash
find ~/.gradle/caches -maxdepth 6 -type d -iname "ideaIU-<pinned-version>*"
```

If it's not cached yet, `./gradlew compileKotlin` (or any Gradle task that resolves the IDE
dependency) will fetch and cache it. Once you have the directory, the classes you want are
usually bundled inside `lib/app.jar` or `lib/product.jar` rather than one class-per-jar (newer
IDE builds merge most modules into a few big archives) - `unzip -l` chokes on these, so prefer
Python's `zipfile` module to search and extract:

```python
import zipfile
z = zipfile.ZipFile(".../lib/app.jar")
[n for n in z.namelist() if "SomeClass" in n]
```

Extract what you need and read it with `javap -p` (signatures) or `javap -c -p` (bytecode, for
answering "does X actually call Y" - e.g. whether a capability method is ever consulted, or is
dead/unused in this platform version). Plugin descriptors (`plugin.xml`-equivalents) are also
inside these jars as `META-INF/*.xml` resources - searching them for a real usage example of an
extension point (which attribute names it takes, which class other bundled plugins register)
is far more reliable than guessing from the EP's own declaration.

Delete anything you extracted for this once you're done; it doesn't belong in the repo.

## 2. Cross-check against the real binary

Once you think you know the truth, confirm it by actually running `vector`, not by reasoning
about what it should do. This project already depends on it being installed
(`vector vrl --version` should work). Prefer `VECTOR_LOG=off` to keep stderr clean, and note
that `$?` after a pipeline reflects the *last* command in the pipe, not `vector` - redirect to
a file first if you need the real exit code.

- **VRL language questions**: write the smallest program that isolates the thing you're
  checking and run it with `vector vrl '<program>'` or `vector vrl -p file.vrl`. To find a
  function's true fallibility, argument names, or accepted argument types, calling it with a
  bogus keyword argument makes the compiler print its real parameter list back at you
  (`error[E108]`); calling it with well-typed arguments and no error handling reveals
  fallibility via `error[E100]`. Don't trust a doc badge or a memory of "this seems like it
  should be fallible" - check with a real call.
- **Config injection/validation questions**: write a real `vector.toml`/`vector.yaml` covering
  every shape the feature should (and shouldn't) recognize, and run
  `vector validate --no-environment <file>`. A shape that doesn't validate isn't a shape this
  plugin should support, no matter how plausible it looks.
- When checking many cases at once (e.g. every stdlib function, or several timestamp/regex
  edge cases), write a throwaway shell/Python script in the scratchpad directory rather than
  running dozens of one-off commands - it's faster and leaves a record of exactly what was
  tested. Delete it once you've captured what you learned; scratch probes don't belong in the
  repo, only the tests you write from what they proved do.

If what you find contradicts the docs, the existing code, or your own initial assumption,
trust the binary - and say so explicitly in the eventual commit message, the same way past
commits in this repo have (e.g. citing the exact `vector vrl` error and code that disagreed
with a doc claim). If something genuinely can't be verified this way - a platform capability
with no observable effect, a construct `vector` can't be coaxed into exercising - say that
plainly instead of implementing it as if it were confirmed.

## 3. Implement

With the ground truth pinned down, implement the feature. Follow the codebase's existing
patterns rather than introducing new ones for something already-solved:
- Grep for a similar existing inspection/injector/provider before writing a new one from
  scratch - VRL's function metadata, PSI accessors, and quick-fix patterns are all
  established elsewhere in `src/main/kotlin/eu/bcosp/vrlintellij/`.
- If the fix touches generated code (`scripts/refresh-vrl-functions.mjs` output, `src/main/gen/`),
  fix the generator, not the generated file - it gets regenerated and hand-edits are lost.
- Keep comments focused on *why*, citing the evidence from steps 1-2 (a compiler error code,
  a source file and line, a crate version) rather than restating what the code does.

## 4. Test

Add or update tests that encode what you actually verified, not just what you implemented -
a test that passes with wrong plugin behavior because it never bothered to compare against
`vector vrl` is worse than no test. When a feature's correctness fix inverts previously-wrong
behavior, invert the corresponding test assertion rather than leaving a stale test asserting
the old, disproven behavior.

## 5. Run a clean build

```bash
./gradlew clean test
```

Confirm it exits 0 with zero failures - don't rely on partial/incremental test runs as the
final check, since a clean build catches staleness that incremental runs can hide. Get the
actual pass/fail count rather than trusting silence:

```bash
python3 -c "
import glob, re
t = f = s = 0
for p in glob.glob('build/test-results/test/*.xml'):
    h = open(p).read(600)
    m = re.search(r'tests=\"(\d+)\" skipped=\"(\d+)\" failures=\"(\d+)\" errors=\"(\d+)\"', h)
    if m: t += int(m.group(1)); s += int(m.group(2)); f += int(m.group(3)) + int(m.group(4))
print(f'tests={t} failures={f} skipped={s}')
"
```

## 6. If a test is flaky, investigate - don't paper over it

A test that fails once and passes on rerun, or fails only in the full suite but not in
isolation (or vice versa), is telling you something real: shared mutable state between tests,
an ordering dependency, a timing assumption, or a genuine race in the feature itself. Reproduce
it deliberately (rerun just that test class several times, or run the full suite twice) to
confirm it's actually flaky and not a one-off environment hiccup, then find the root cause
before moving on. Do not:
- Rerun until it happens to pass and call it done.
- Add a `Thread.sleep`, retry loop, or `@Ignore` to make the symptom go away without
  understanding the cause.
- Loosen an assertion just to stop the failure.

If you genuinely cannot pin down the cause after real investigation, say so explicitly to the
user rather than quietly shipping a flaky test.

## 7. Commit

Only once the clean build is green. Match this repo's existing commit style (`git log` on
this repo is the reference, not generic conventions): a short imperative subject naming the
fix, then a body that leads with the *why* - the concrete evidence from step 2 (a `vector vrl`
error message, a source file/line, a crate version) that shows the old behavior was wrong and
the new behavior is right. No marketing language, no restating the diff. If the fix corrects a
previous misunderstanding (including one from an earlier session), it's fine to say so plainly.

Follow the standard git safety rules already in force for this session: stage specific files
by name, review `git status`/`git diff` before committing, and never commit unless asked to.
