import { launch, attachDiagnostics, newSink, shot, dumpInteractive, waitForLogin, BASE } from "./lib.mjs";
import fs from "node:fs";

const ctx = await launch();
const page = ctx.pages()[0] || (await ctx.newPage());
const sink = newSink();
attachDiagnostics(page, sink);

console.log("Opening", BASE + "/en", "...");
await page.goto(BASE + "/en", { waitUntil: "domcontentloaded" }).catch((e) => console.log("goto warn:", e.message));
await page.waitForTimeout(2500);
await shot(page, "01-landing");

console.log("\n>>> Please LOG IN with Google in the Chrome window that just opened.");
console.log(">>> Waiting for an authenticated API call (up to 6 minutes)...\n");

const token = await waitForLogin(page);
if (!token) {
  console.log("!! No authenticated API call detected (login not completed / timed out).");
} else {
  console.log("✅ Login detected. Captured a Bearer token for later API checks.");
  fs.writeFileSync("./auth.json", JSON.stringify({ token }, null, 2));
}

await page.waitForTimeout(2000);
await shot(page, "02-after-login");
console.log("Current URL after login:", page.url());
await dumpInteractive(page, "home (logged in)");

// Try to find the entry into the invitation builder.
const candidates = [
  "text=/create/i",
  "text=/new invitation/i",
  "text=/build/i",
  "text=/templates?/i",
  "text=/get started/i",
  "text=/ստեղծ/i", // hy: create
];
for (const sel of candidates) {
  const loc = page.locator(sel).first();
  if (await loc.count()) {
    console.log(`Found candidate entry: ${sel} ->`, (await loc.innerText().catch(() => "")).slice(0, 40));
  }
}

// Also list nav links that look builder-related.
const links = await page.evaluate(() =>
  [...document.querySelectorAll("a[href]")]
    .map((a) => ({ href: a.getAttribute("href"), text: a.innerText.trim().slice(0, 30) }))
    .filter((l) => /build|create|invitation|template|dashboard|profile|my-/i.test(l.href + " " + l.text))
);
console.log("\n=== builder-related links ===");
for (const l of links) console.log(`  ${l.href}  (${l.text})`);

console.log("\n=== diagnostics so far ===");
console.log(JSON.stringify(sink, null, 2));

console.log("\nExploration capture complete. Leaving browser open for 20s...");
await page.waitForTimeout(20000);
await ctx.close();
console.log("done.");
