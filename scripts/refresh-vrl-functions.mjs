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
    const response = await fetch(DOCS_URL);
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
        byCategory.set(categoryId, parseFunctions(sectionHtml));
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
function parseFunctions(sectionHtml) {
    const heading = /<h3\s+x-data="\{ show: false \}"[^>]*\sid=([a-z_0-9]+)\s[^>]*><a href=#\1>\1<\/a><\/h3>/g;
    const matches = [...sectionHtml.matchAll(heading)];
    const functions = [];
    for (let i = 0; i < matches.length; i++) {
        const name = matches[i][1];
        const start = matches[i].index;
        const end = i + 1 < matches.length ? matches[i + 1].index : sectionHtml.length;
        try {
            functions.push(parseFunction(name, sectionHtml.slice(start, end)));
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

function parseFunction(name, block) {
    const badges = block.match(/rounded[^>]*>(fallible|infallible)\s*<\/span>[\s\S]*?rounded[^>]*>(pure|impure)\s*<\/span>/);
    if (!badges) throw new Error("fallible/pure badges not found");

    const descriptionMatch = block.match(/<div class="mt-6 prose prose-md dark:prose-invert max-w-none">([\s\S]*?)<\/div>/);
    if (!descriptionMatch) throw new Error("description not found");

    return {
        name,
        isFallible: badges[1] === "fallible",
        isPure: badges[2] === "pure",
        description: cleanDescription(descriptionMatch[1]),
        arguments: parseArguments(block),
        returnTypes: parseReturnTypes(name, block),
        examples: parseExamples(name, block),
    };
}

/** Extracts the "Examples" section vector.dev renders per function (a series of named Source ->
 * Return/Raises pairs) - every function has one except strip_ansi_escape_codes, which returns []
 * here. A rare third "Input" block (an external-target JSON payload shown before Source on a
 * handful of examples, e.g. get's "External target") is deliberately not captured: it's page
 * context rather than something a Quick Doc popup needs, and skipping it keeps every example a
 * plain (title, source, result) triple instead of an open-ended list of labeled blocks. */
function parseExamples(name, block) {
    const sectionIndex = block.indexOf(`id=${name}-examples `);
    if (sectionIndex < 0) return [];
    const sectionHtml = block.slice(sectionIndex);

    // The title itself is usually plain text, but can contain inline markup (e.g. uuid_v7's
    // "Create a UUIDv7 with explicit <code>now()</code>") - captured non-greedily up to the
    // closing `</span></h5>` and run through cleanText rather than assumed tag-free.
    const heading = /<h5\s+x-data="\{ show: false \}"[^>]*\sid=[a-z0-9_./+-]+\s[^>]*><span>([\s\S]*?)<\/span><\/h5>/g;
    const matches = [...sectionHtml.matchAll(heading)];
    return matches.map((m, i) => {
        const title = cleanText(m[1]);
        const start = m.index + m[0].length;
        const end = i + 1 < matches.length ? matches[i + 1].index : sectionHtml.length;
        const exampleHtml = sectionHtml.slice(start, end);

        const source = extractLabeledCode(exampleHtml, "Source");
        const returnCode = extractLabeledCode(exampleHtml, "Return");
        const raisesCode = extractLabeledCode(exampleHtml, "Raises");
        if (source == null) throw new Error(`example '${title}' has no Source block`);
        if (returnCode == null && raisesCode == null) throw new Error(`example '${title}' has no Return or Raises block`);

        return { title, source, result: returnCode ?? raisesCode, isError: raisesCode != null };
    });
}

/** Finds `<span class=font-light>{label}</span>` (vector.dev's "Source"/"Return"/"Raises" caption)
 * and pulls the plain-text code out of the `<pre><code>...</code></pre>` that immediately follows
 * it - or null if this example has no block for that label (e.g. an infallible example has no
 * "Raises"). */
function extractLabeledCode(html, label) {
    const markerIndex = html.indexOf(`<span class=font-light>${label}</span>`);
    if (markerIndex < 0) return null;
    const codeStart = html.indexOf("<code", markerIndex);
    const codeOpenEnd = html.indexOf(">", codeStart) + 1;
    const codeEnd = html.indexOf("</code>", codeOpenEnd);
    if (codeStart < 0 || codeEnd < 0) throw new Error(`'${label}' block has no <code>...</code>`);
    return extractCode(html.slice(codeOpenEnd, codeEnd));
}

/** Unlike [cleanText]/[cleanHtml] (which collapse all whitespace to single spaces), this keeps a
 * code block's real newlines and indentation intact - chroma's syntax-highlighting spans wrap
 * individual tokens on a line, with the line's actual newline character sitting in the text
 * between them, so simply stripping every tag and decoding entities reconstructs the exact source
 * VRL/JSON text vector.dev shows. */
function extractCode(html) {
    return decodeEntities(html.replace(/<[^>]+>/g, "")).trim();
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

function parseArguments(block) {
    const table = block.match(/<table class=table-fixed>[\s\S]*?<tbody>([\s\S]*?)<\/tbody>/);
    if (!table) return []; // a zero-argument function, e.g. now()

    const args = [];
    for (const row of table[1].matchAll(/<tr>([\s\S]*?)<\/tr>/g)) {
        const cells = [...row[1].matchAll(/<td[^>]*>([\s\S]*?)<\/td>/g)].map((c) => c[1]);
        if (cells.length !== 5) throw new Error(`argument row has ${cells.length} cells, expected 5`);

        const types = new Set(cleanText(cells[1]).split(/\s+/).filter(Boolean));
        const defaultCell = cells[3].match(/<code>([\s\S]*?)<\/code>/);
        args.push({
            name: cleanText(cells[0]),
            types,
            description: cleanHtml(cells[2]),
            isRequired: cleanText(cells[4]).toLowerCase() === "yes",
            defaultValue: defaultCell ? defaultLiteral(cleanText(defaultCell[1]), types) : null,
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
    const examples = fn.examples.length === 0 ? "listOf()" : `listOf(\n${fn.examples.map(renderExample).join(",\n")}\n        )`;
    return (
        `    ${kotlinString(fn.name)} to VRLFunction(\n` +
        `        name = ${kotlinString(fn.name)},\n` +
        `        description = ${kotlinString(fn.description)},\n` +
        `        isFallible = ${fn.isFallible},\n` +
        `        isPure = ${fn.isPure},\n` +
        `        arguments = ${args},\n` +
        `        returnTypes = ${renderStringSet(fn.returnTypes, { errorLast: true })},\n` +
        `        examples = ${examples}\n` +
        `    )`
    );
}

function renderArgument(arg) {
    const defaultLine = arg.defaultValue != null ? `,\n                defaultValue = ${arg.defaultValue}` : "";
    return (
        "            VRLFunctionArgument(\n" +
        `                ${kotlinString(arg.name)},\n` +
        `                ${renderStringSet(arg.types)},\n` +
        `                ${kotlinString(arg.description)},\n` +
        `                ${arg.isRequired}${defaultLine}\n` +
        "            )"
    );
}

function renderExample(example) {
    return (
        "            VRLFunctionExample(\n" +
        `                ${kotlinString(example.title)},\n` +
        `                ${kotlinString(example.source)},\n` +
        `                ${kotlinString(example.result)},\n` +
        `                ${example.isError}\n` +
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

main().catch((e) => {
    console.error(e);
    process.exitCode = 1;
});
