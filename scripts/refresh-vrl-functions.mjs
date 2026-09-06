#!/usr/bin/env node
// Regenerates src/main/kotlin/eu/bcosp/vrlintellij/functions/*Functions.kt from the live VRL
// function reference at vector.dev. Run manually with `node scripts/refresh-vrl-functions.mjs`,
// or via the "Refresh VRL functions" GitHub Actions workflow, which runs this monthly and opens a
// PR with any changes for review - vector.dev's own docs are the ground truth these files mirror,
// so once generated they should not be hand-edited (a refresh will just overwrite the edit).
//
// The page (https://vector.dev/docs/reference/vrl/functions/) is server-rendered Hugo output with
// a very consistent structure per function: an `<h2 id="{category}-functions">` heading groups
// functions by category (matching this plugin's own file-per-category layout 1:1), each function
// is an `<h3 id="{name}">` with fallible/infallible and pure/impure badges, a prose description, a
// `<table class=table-fixed>` of arguments (name/type/description/default/required columns, with
// union types listed as multiple `<br>`-separated lines), and a "Function spec" block whose
// `<div class=ml-3>` holds the return type(s). This script depends on that structure staying
// stable; if vector.dev changes it, parsing will throw (loudly, per-function or outright) rather
// than silently emit wrong data - see the failure modes handled below.

import { readFile, writeFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";

const DOCS_URL = "https://vector.dev/docs/reference/vrl/functions/";
const FUNCTIONS_DIR = fileURLToPath(new URL("../src/main/kotlin/eu/bcosp/vrlintellij/functions/", import.meta.url));

// vector.dev's own function reference (scraped above) almost never spells out an argument's valid
// enum values in prose - a handful of case-conversion functions are the only exception. The real,
// authoritative list lives in vrl's own Rust stdlib source, as `Parameter::optional(...)`'s
// `.enum_variants(...)` builder call, so enum values are fetched from there instead - see
// fetchEnumVariantsByFunction below.
const VRL_REPO_TREE_URL = "https://api.github.com/repos/vectordotdev/vrl/git/trees/main?recursive=1";
const VRL_RAW_BASE = "https://raw.githubusercontent.com/vectordotdev/vrl/main/";

// A handful of stdlib functions validate an argument against a fixed set of strings (and say so in
// their own doc comment/error message) without ever calling `.enum_variants(...)` on it - an
// omission on vrl's side rather than something fetchEnumVariantsByFunction failed to find. Found
// by grepping the whole stdlib for "must be (either|one of)" outside of any `.enum_variants(...)`
// call; cross-checked directly against each function's `Parameter`/`match ... { ... }` in
// src/stdlib/encrypt_ip.rs, decrypt_ip.rs, and xxhash.rs. Each entry here is a real gap to remove
// once vrl's own Parameter definition adds the annotation - not a substitute for it.
const MANUAL_ENUM_OVERRIDES = {
    encrypt_ip: { mode: ["aes128", "pfx"] },
    decrypt_ip: { mode: ["aes128", "pfx"] },
    xxhash: { variant: ["XXH32", "XXH64", "XXH3-64", "XXH3-128"] },
};

// category id (from the page's <h2 id=...>) -> the Kotlin file/variable this plugin already uses
// for it. Deliberately an explicit table rather than a derived PascalCase/camelCase transform,
// both because "IP" isn't a plain capitalization of "ip" and so a new category vector.dev adds in
// the future fails loudly (see main()) instead of silently inventing a new file.
const CATEGORIES = {
    "array-functions": { fileBase: "Array", varName: "arrayFunctions" },
    "checksum-functions": { fileBase: "Checksum", varName: "checksumFunctions" },
    "codec-functions": { fileBase: "Codec", varName: "codecFunctions" },
    "coerce-functions": { fileBase: "Coerce", varName: "coerceFunctions" },
    "convert-functions": { fileBase: "Convert", varName: "convertFunctions" },
    "cryptography-functions": { fileBase: "Cryptography", varName: "cryptographyFunctions" },
    "debug-functions": { fileBase: "Debug", varName: "debugFunctions" },
    "enrichment-functions": { fileBase: "Enrichment", varName: "enrichmentFunctions" },
    "enumerate-functions": { fileBase: "Enumerate", varName: "enumerateFunctions" },
    "event-functions": { fileBase: "Event", varName: "eventFunctions" },
    "ip-functions": { fileBase: "IP", varName: "ipFunctions" },
    "map-functions": { fileBase: "Map", varName: "mapFunctions" },
    "metrics-functions": { fileBase: "Metrics", varName: "metricsFunctions" },
    "number-functions": { fileBase: "Number", varName: "numberFunctions" },
    "object-functions": { fileBase: "Object", varName: "objectFunctions" },
    "parse-functions": { fileBase: "Parse", varName: "parseFunctions" },
    "path-functions": { fileBase: "Path", varName: "pathFunctions" },
    "random-functions": { fileBase: "Random", varName: "randomFunctions" },
    "string-functions": { fileBase: "String", varName: "stringFunctions" },
    "system-functions": { fileBase: "System", varName: "systemFunctions" },
    "timestamp-functions": { fileBase: "Timestamp", varName: "timestampFunctions" },
    "type-functions": { fileBase: "Type", varName: "typeFunctions" },
};

async function main() {
    const [response, enumVariantsByFunction] = await Promise.all([fetch(DOCS_URL), fetchEnumVariantsByFunction()]);
    if (!response.ok) throw new Error(`Failed to fetch ${DOCS_URL}: HTTP ${response.status}`);
    const html = await response.text();

    const byCategory = new Map();
    for (const [categoryId, sectionHtml] of splitByCategory(html)) {
        if (!(categoryId in CATEGORIES)) {
            throw new Error(
                `Unknown VRL function category '${categoryId}' - vector.dev added a new one. ` +
                    "Add it to CATEGORIES in this script and create the matching Kotlin file first.",
            );
        }
        byCategory.set(categoryId, parseFunctions(sectionHtml, enumVariantsByFunction));
    }

    let changedCount = 0;
    for (const [categoryId, { fileBase, varName }] of Object.entries(CATEGORIES)) {
        const functions = byCategory.get(categoryId);
        if (!functions || functions.length === 0) {
            throw new Error(`No functions parsed for category '${categoryId}' - the page structure may have changed.`);
        }

        const filePath = new URL(`${fileBase}Functions.kt`, `file://${FUNCTIONS_DIR}`);
        const content = renderKotlin(varName, functions);
        const existing = await readFile(filePath, "utf8").catch(() => null);
        if (existing !== content) {
            await writeFile(filePath, content, "utf8");
            changedCount++;
            console.log(`updated ${fileBase}Functions.kt (${functions.length} functions)`);
        }
    }

    console.log(changedCount === 0 ? "No changes - VRL function metadata is already up to date." : `${changedCount} file(s) updated.`);
}

/** Slices the page into one HTML chunk per `<h2 id="{category}-functions">` section. */
function splitByCategory(html) {
    const heading = /<h2[^>]*\sid=([a-z-]+)[^>]*><span>[^<]*<\/span><\/h2>/g;
    const matches = [...html.matchAll(heading)].filter((m) => m[1].endsWith("-functions"));
    return matches.map((m, i) => {
        const start = m.index + m[0].length;
        const end = i + 1 < matches.length ? matches[i + 1].index : html.length;
        return [m[1], html.slice(start, end)];
    });
}

/** Slices a category chunk into one HTML block per `<h3 id="{name}">` function. */
function parseFunctions(sectionHtml, enumVariantsByFunction) {
    const heading = /<h3\s+x-data="\{ show: false \}"[^>]*\sid=([a-z_0-9]+)\s[^>]*><a href=#\1>\1<\/a><\/h3>/g;
    const matches = [...sectionHtml.matchAll(heading)];
    const functions = [];
    for (let i = 0; i < matches.length; i++) {
        const name = matches[i][1];
        const start = matches[i].index;
        const end = i + 1 < matches.length ? matches[i + 1].index : sectionHtml.length;
        try {
            functions.push(parseFunction(name, sectionHtml.slice(start, end), enumVariantsByFunction.get(name) ?? {}));
        } catch (e) {
            // A single function's markup not matching the expected shape (an exotic
            // sub-parameter table, say) shouldn't block every other function in the run - it's
            // skipped (keeping whatever this file already has for it) and reported so a human
            // notices in the workflow's log/PR description.
            console.warn(`skipping '${name}': ${e.message}`);
        }
    }
    return functions.sort((a, b) => a.name.localeCompare(b.name));
}

function parseFunction(name, block, enumsForFunction) {
    const badges = block.match(/rounded[^>]*>(fallible|infallible)\s*<\/span>[\s\S]*?rounded[^>]*>(pure|impure)\s*<\/span>/);
    if (!badges) throw new Error("fallible/pure badges not found");

    const descriptionMatch = block.match(/<div class="mt-6 prose prose-md dark:prose-invert max-w-none">([\s\S]*?)<\/div>/);
    if (!descriptionMatch) throw new Error("description not found");

    return {
        name,
        isFallible: badges[1] === "fallible",
        isPure: badges[2] === "pure",
        description: cleanDescription(descriptionMatch[1]),
        arguments: parseArguments(block, enumsForFunction),
        returnTypes: parseReturnTypes(name, block),
    };
}

/** [cleanHtml] applied to a whole function description, plus a blank line between adjacent
 * top-level paragraphs - some descriptions nest further structure inside a paragraph (a list
 * whose items are themselves `<li><p>...</p></li>`, e.g. get_enrichment_table_record's), so
 * paragraphs can't just be re-extracted with a flat `<p>...</p>` match the way this used to: that
 * would silently drop everything between them (the actual list markup) along with anything that
 * doesn't happen to be wrapped in its own `<p>` at all (a bare `<li>value</li>`). Splicing in the
 * blank line as a final, purely cosmetic pass over the already-sanitized string keeps all of that
 * intact - `</p><p>` (immediately adjacent, exactly how the source HTML joins two sibling
 * paragraphs) is the only sequence this touches, so a `<p>` nested one level down inside a `<li>`
 * is untouched either way. It's for the generated .kt source's own readability, not the rendered
 * popup's - HTML collapses the extra whitespace right back down at render time regardless. */
function cleanDescription(html) {
    return cleanHtml(html).replace(/<\/p><p>/g, "</p>\n\n<p>");
}

function parseArguments(block, enumsForFunction) {
    const table = block.match(/<table class=table-fixed>[\s\S]*?<tbody>([\s\S]*?)<\/tbody>/);
    if (!table) return []; // a zero-argument function, e.g. now()

    const args = [];
    for (const row of table[1].matchAll(/<tr>([\s\S]*?)<\/tr>/g)) {
        const cells = [...row[1].matchAll(/<td[^>]*>([\s\S]*?)<\/td>/g)].map((c) => c[1]);
        if (cells.length !== 5) throw new Error(`argument row has ${cells.length} cells, expected 5`);

        const types = new Set(cleanText(cells[1]).split(/\s+/).filter(Boolean));
        const defaultCell = cells[3].match(/<code>([\s\S]*?)<\/code>/);
        const name = cleanText(cells[0]);
        args.push({
            name,
            types,
            description: cleanHtml(cells[2]),
            isRequired: cleanText(cells[4]).toLowerCase() === "yes",
            defaultValue: defaultCell ? defaultLiteral(cleanText(defaultCell[1]), types) : null,
            enumValues: enumsForFunction[name] ?? [],
        });
    }
    return args;
}

/** Picks the right Kotlin literal form for a parsed default value using the argument's own
 * declared types (bare `true`/`1`/`1.5` when both the type and the text itself agree; a quoted
 * Kotlin string otherwise) - some "Default" cells are prose rather than a literal (e.g. `slice`'s
 * `end` documents its default as "String length"), so the type alone isn't a safe enough signal. */
function defaultLiteral(text, types) {
    if (types.has("boolean") && /^(true|false)$/.test(text)) return text;
    if (types.has("integer") && /^-?\d+$/.test(text)) return text;
    if (types.has("float") && /^-?\d+\.\d+$/.test(text)) return text;
    return kotlinString(text);
}

function parseReturnTypes(name, block) {
    const specIndex = block.indexOf(`${name}-function-spec`);
    if (specIndex < 0) throw new Error("function spec not found");
    const start = block.indexOf("<div class=ml-3>", specIndex);
    if (start < 0) throw new Error("return type block not found");
    const end = block.indexOf("</div>", start);
    if (end < 0) throw new Error("return type block not closed");

    const text = cleanText(block.slice(start, end));
    const types = new Set();
    for (const group of text.matchAll(/<([^<>]+)>/g)) {
        for (const type of group[1].split("|")) {
            const trimmed = type.trim();
            if (trimmed) types.add(trimmed);
        }
    }
    if (types.size === 0) throw new Error("no return types parsed");
    return types;
}

/** Fetches every enum-valued string argument the VRL stdlib actually validates against, keyed by
 * function identifier then argument name (e.g. `{ encode_base64: { charset: ["standard",
 * "url_safe"] } }`). See the VRL_REPO_TREE_URL/VRL_RAW_BASE comment above for why this reads vrl's
 * own Rust source rather than vector.dev's docs.
 *
 * Two passes over the fetched files: casing/mod.rs's `ORIGINAL_CASE` is a `Parameter` shared by
 * five sibling files (camelcase.rs, kebabcase.rs, pascalcase.rs, screamingsnakecase.rs,
 * snakecase.rs), each of which lists it in their own `parameters()` array by bare name rather
 * than declaring their own `Parameter::optional("original_case", ...)` call - so those files have
 * nothing for [parseParameterEnumVariants] to find directly, and resolving `ORIGINAL_CASE` must
 * wait until casing/mod.rs itself has been parsed first (there's no ordering guarantee that it's
 * fetched/processed before the files that reference it, since fetches run concurrently).
 *
 * Deliberately BYTES-only (a plain string argument, e.g. `unit: "seconds"`): the one enum-valued
 * ARRAY argument in the stdlib (snakecase's `excluded_boundaries`, whose *elements* are drawn from
 * an enum) needs array-literal-aware completion/inspection this plugin doesn't have yet, so it's
 * skipped rather than attached to the wrong argument shape.
 */
async function fetchEnumVariantsByFunction() {
    const treeResponse = await fetch(VRL_REPO_TREE_URL);
    if (!treeResponse.ok) throw new Error(`Failed to fetch the VRL stdlib file tree: HTTP ${treeResponse.status}`);
    const tree = await treeResponse.json();
    const rustFiles = tree.tree.map((entry) => entry.path).filter((path) => path.startsWith("src/stdlib/") && path.endsWith(".rs"));
    if (rustFiles.length === 0) throw new Error("found no .rs files under src/stdlib/ - the VRL repo layout may have changed");

    const files = await mapWithConcurrency(rustFiles, 20, async (path) => {
        const response = await fetch(`${VRL_RAW_BASE}${path}`);
        if (!response.ok) throw new Error(`Failed to fetch ${path}: HTTP ${response.status}`);
        return { path, text: await response.text() };
    });

    const casingModule = files.find((f) => f.path === "src/stdlib/casing/mod.rs");
    if (!casingModule) throw new Error("expected src/stdlib/casing/mod.rs, but it's gone from the VRL repo");
    const sharedOriginalCase = parseParameterEnumVariants(casingModule.text, parseLocalEnumConstants(casingModule.text))["original_case"];
    if (!sharedOriginalCase) throw new Error("casing/mod.rs no longer defines ORIGINAL_CASE the expected way");

    const byFunction = new Map();
    for (const { path, text } of files) {
        const identifiers = [...text.matchAll(/fn identifier\(&self\) -> &'static str\s*\{\s*"([a-z_0-9]+)"/g)].map((m) => m[1]);
        if (identifiers.length === 0) continue;

        const argumentEnums = parseParameterEnumVariants(text, parseLocalEnumConstants(text));
        // camelcase.rs/kebabcase.rs/pascalcase.rs/screamingsnakecase.rs/snakecase.rs each list
        // ORIGINAL_CASE by bare name in their own parameters() array instead of declaring their
        // own Parameter for it - resolve those against casing/mod.rs's constant (found above).
        // The `in` guard is a no-op everywhere except casing/mod.rs itself, which already found
        // its own "original_case" directly and shouldn't have it clobbered.
        if (!("original_case" in argumentEnums) && /\bORIGINAL_CASE\s*,/.test(text)) {
            argumentEnums["original_case"] = sharedOriginalCase;
        }

        for (const identifier of identifiers) {
            byFunction.set(identifier, { ...(byFunction.get(identifier) ?? {}), ...argumentEnums });
        }
    }

    // Applied so the override loses to real source data for the same argument if one ever
    // appears (e.g. vrl adds .enum_variants(...) for it later) rather than silently shadowing it.
    for (const [fn, overrideArgs] of Object.entries(MANUAL_ENUM_OVERRIDES)) {
        byFunction.set(fn, { ...overrideArgs, ...(byFunction.get(fn) ?? {}) });
    }
    return byFunction;
}

/** Named `static`/`const NAME: &[EnumVariant] = &[...]` declarations in one stdlib source file,
 * e.g. hmac.rs's `static ALGORITHM_ENUM: &[EnumVariant] = &[...]` - `.enum_variants(...)` calls
 * reference these by name instead of repeating the list inline whenever it's used more than once
 * or is long enough to warrant pulling out. Keyed by the constant's name. */
function parseLocalEnumConstants(text) {
    const constants = {};
    for (const m of text.matchAll(/(?:static|const)\s+(\w+):\s*&\[EnumVariant]\s*=\s*&\[(.*?)];/gs)) {
        constants[m[1]] = extractEnumValues(m[2]);
    }
    return constants;
}

/** Finds every `Parameter::(optional|required)("name", kind::BYTES, "...")` in a stdlib source
 * file and, for each one whose builder chain includes `.enum_variants(...)`, resolves the values -
 * either an inline `&[EnumVariant { value: "...", ... }, ...]` literal, or a reference to a
 * same-file constant from [localEnums]. A `Parameter::...(...)` call's own trailing `.method(...)`
 * chain isn't delimited by anything specific to a single parameter (they're just array elements
 * separated by commas), so each parameter's search range is bounded by the *next* parameter's
 * `Parameter::(optional|required)(` instead of any fixed terminator. */
function parseParameterEnumVariants(text, localEnums) {
    const starts = [...text.matchAll(/Parameter::(?:optional|required)\(\s*"([a-zA-Z_0-9]+)"\s*,\s*(kind::\w+)\s*,/g)];
    const result = {};
    for (let i = 0; i < starts.length; i++) {
        const [full, argName, kind] = starts[i];
        if (kind !== "kind::BYTES") continue;

        const segmentStart = starts[i].index + full.length;
        const segmentEnd = i + 1 < starts.length ? starts[i + 1].index : text.length;
        const segment = text.slice(segmentStart, segmentEnd);

        const inline = segment.match(/\.enum_variants\(&\[(.*?)]\)/s);
        if (inline) {
            result[argName] = extractEnumValues(inline[1]);
            continue;
        }
        const ref = segment.match(/\.enum_variants\(\s*(\w+)\s*\)/);
        if (ref && localEnums[ref[1]]) {
            result[argName] = localEnums[ref[1]];
        }
    }
    return result;
}

function extractEnumValues(body) {
    return [...body.matchAll(/value:\s*"((?:[^"\\]|\\.)*)"/g)].map((m) => m[1]);
}

/** Runs `fn` over `items` with at most `concurrency` calls in flight at once - 213 individual
 * raw.githubusercontent.com fetches at full concurrency is a lot of simultaneous connections to
 * open for no real benefit, and this is a small, generic enough shape to not warrant a dependency. */
async function mapWithConcurrency(items, concurrency, fn) {
    const results = new Array(items.length);
    let nextIndex = 0;
    async function worker() {
        while (nextIndex < items.length) {
            const current = nextIndex++;
            results[current] = await fn(items[current]);
        }
    }
    await Promise.all(Array.from({ length: Math.min(concurrency, items.length) }, worker));
    return results;
}

// Descriptions (function and argument) use exactly this set of tags on the live page - checked
// directly against every one of the ~213 functions, not assumed - and every one of them is
// supported by IntelliJ's Swing-based doc-popup HTML renderer.
const ALLOWED_TAGS = new Set(["a", "code", "em", "strong", "p", "ul", "ol", "li"]);

function cleanText(html) {
    // Only genuine word/paragraph separators (<br>, the end of a <p>) become a space; every other
    // tag (<code>, <a href=...>, <span>, ...) is inline and must vanish with no space left behind,
    // or "Upcases <code>value</code>, where" would turn into the stray "Upcases value , where".
    const withBreaks = html.replace(/<br\s*\/?>/gi, " ").replace(/<\/p>/gi, " ");
    const stripped = withBreaks.replace(/<[^>]+>/g, "");
    return decodeEntities(stripped).replace(/\s+/g, " ").trim();
}

/** Unlike [cleanText] (which strips every tag down to plain text), this keeps [ALLOWED_TAGS] as
 * real HTML - VRLDocumentationProvider renders `description` directly rather than escaping it, so
 * `<code>value</code>` shows as actual monospaced text in the Quick Documentation popup, matching
 * how vector.dev itself renders it, instead of "Upcases <code>value</code>..." literally. Every
 * other tag/attribute is dropped; an `<a>`'s `href` is kept but resolved to an absolute vector.dev
 * URL, since a relative link means nothing inside the IDE's doc popup. */
function cleanHtml(html) {
    return decodeCosmeticEntities(sanitizeHtml(html)).replace(/\s+/g, " ").trim();
}

function sanitizeHtml(html) {
    return html.replace(/<(\/?)([a-zA-Z][a-zA-Z0-9]*)([^>]*)>/g, (_full, closing, rawTag, attrs) => {
        const tag = rawTag.toLowerCase();
        if (!ALLOWED_TAGS.has(tag)) return "";
        if (closing) return `</${tag}>`;
        if (tag === "a") {
            const hrefMatch = attrs.match(/href=("[^"]*"|'[^']*'|[^\s>]+)/);
            const href = hrefMatch ? absoluteUrl(hrefMatch[1].replace(/^['"]|['"]$/g, "")) : null;
            return href ? `<a href="${href}">` : "<a>";
        }
        return `<${tag}>`;
    });
}

function absoluteUrl(href) {
    if (/^https?:\/\//.test(href)) return href;
    if (href.startsWith("#")) return `${DOCS_URL}${href}`;
    return `https://vector.dev${href}`;
}

// "Smart typography" entities decode to their literal Unicode character either way - safe both as
// plain text and inside HTML.
function decodeCosmeticEntities(text) {
    return text
        .replace(/&nbsp;/g, " ")
        .replace(/&rsquo;/g, "’")
        .replace(/&lsquo;/g, "‘")
        .replace(/&rdquo;/g, "”")
        .replace(/&ldquo;/g, "“")
        .replace(/&mdash;/g, "—")
        .replace(/&ndash;/g, "–")
        .replace(/&mldr;|&hellip;/g, "…");
}

// Structural entities (&lt; &gt; &amp; &quot; &#39;) are only decoded for [cleanText]'s plain-text
// fields, which have no HTML left to stay valid for - [cleanHtml] deliberately leaves them
// encoded, both because they're already valid inside the HTML it keeps and because, once embedded
// in the generated Kotlin string, encoded entities contain no character [kotlinString] needs to
// escape (a literal `"` from a decoded &quot; would).
function decodeEntities(text) {
    return decodeCosmeticEntities(text)
        .replace(/&lt;/g, "<")
        .replace(/&gt;/g, ">")
        .replace(/&quot;/g, '"')
        .replace(/&#34;/g, '"')
        .replace(/&#39;/g, "'")
        .replace(/&#96;/g, "`")
        .replace(/&amp;/g, "&");
}

function kotlinString(text) {
    const escaped = text
        .replace(/\\/g, "\\\\")
        .replace(/"/g, '\\"')
        .replace(/\$/g, "\\$")
        .replace(/\n/g, "\\n")
        .replace(/\t/g, "\\t");
    return `"${escaped}"`;
}

function renderKotlin(varName, functions) {
    const entries = functions.map((fn) => renderFunction(fn)).join(",\n");
    return (
        "// Generated by scripts/refresh-vrl-functions.mjs from https://vector.dev/docs/reference/vrl/functions/ - do not hand-edit.\n" +
        "package eu.bcosp.vrlintellij.functions\n\n" +
        `val ${varName} = mapOf(\n${entries}\n)\n`
    );
}

function renderFunction(fn) {
    const args = fn.arguments.length === 0 ? "listOf()" : `listOf(\n${fn.arguments.map(renderArgument).join(",\n")}\n        )`;
    return (
        `    ${kotlinString(fn.name)} to VRLFunction(\n` +
        `        name = ${kotlinString(fn.name)},\n` +
        `        description = ${kotlinString(fn.description)},\n` +
        `        isFallible = ${fn.isFallible},\n` +
        `        isPure = ${fn.isPure},\n` +
        `        arguments = ${args},\n` +
        `        returnTypes = ${renderStringSet(fn.returnTypes, { errorLast: true })}\n` +
        `    )`
    );
}

function renderArgument(arg) {
    const namedParams = [];
    if (arg.defaultValue != null) namedParams.push(`defaultValue = ${arg.defaultValue}`);
    if (arg.enumValues.length > 0) namedParams.push(`enumValues = ${renderStringList(arg.enumValues)}`);
    const trailing = namedParams.length > 0 ? `,\n                ${namedParams.join(",\n                ")}` : "";
    return (
        "            VRLFunctionArgument(\n" +
        `                ${kotlinString(arg.name)},\n` +
        `                ${renderStringSet(arg.types)},\n` +
        `                ${kotlinString(arg.description)},\n` +
        `                ${arg.isRequired}${trailing}\n` +
        "            )"
    );
}

// Both argument and return types keep the order the documentation itself lists them in (the order
// parseArguments/parseReturnTypes encountered them, preserved by Set insertion order) rather than
// being alphabetized - `split`'s `pattern: string | regex` reads as documented, not resorted.
// Return types additionally pull `error` (a fallible function's failure case) to the trailing
// entry if it isn't already there, called out after the success types rather than wherever it
// happens to fall.
function renderStringSet(values, { errorLast = false } = {}) {
    const ordered = [...values];
    const result = errorLast && ordered.includes("error")
        ? [...ordered.filter((v) => v !== "error"), "error"]
        : ordered;
    return `setOf(${result.map(kotlinString).join(", ")})`;
}

// Enum values keep the source's declaration order (a completion list matching how the stdlib
// itself lists variants), unlike renderStringSet's types/returnTypes which have no such ordering
// to preserve.
function renderStringList(values) {
    return `listOf(${values.map(kotlinString).join(", ")})`;
}

main().catch((e) => {
    console.error(e);
    process.exitCode = 1;
});
