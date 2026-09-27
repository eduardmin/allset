import { launch, attachDiagnostics, newSink, shot, dumpInteractive, BASE } from "./lib.mjs";
import fs from "node:fs";

const ctx = await launch();
const page = ctx.pages()[0] || (await ctx.newPage());
const sink = newSink();
attachDiagnostics(page, sink);

// Capture a Bearer token if the SPA sends one (confirms authenticated state).
let token = null;
page.on("request", (req) => {
  const auth = req.headers()["authorization"];
  if (req.url().includes("api.allset.am") && auth && /^bearer /i.test(auth) && !token) {
    token = auth.replace(/^bearer /i, "");
    console.log("✅ authenticated API call seen (token captured)");
  }
});

async function step(name, url) {
  console.log(`\n==== STEP ${name} -> ${url || "(no nav)"} ====`);
  if (url) await page.goto(url, { waitUntil: "networkidle" }).catch((e) => console.log("nav warn:", e.message));
  await page.waitForTimeout(3000);
  await shot(page, name);
  await dumpInteractive(page, name);
}

// 1. Templates listing (builder entry)
await step("10-templates", BASE + "/en/build/templates");

// Try to enter the builder by clicking the first visible "Start"/select control.
const starters = page.locator(
  'button:has-text("Start"), button:has-text("Select"), a:has-text("Start"), [role=button]:has-text("Start")'
);
const n = await starters.count();
console.log(`\n"Start"-like controls found: ${n}`);
if (n > 0) {
  await starters.first().click({ timeout: 8000 }).catch((e) => console.log("click warn:", e.message));
  await page.waitForTimeout(4000);
  await shot(page, "11-after-start");
  console.log("URL after Start:", page.url());
  await dumpInteractive(page, "11-after-start");
}

console.log("\n=== diagnostics ===");
console.log(JSON.stringify(sink, null, 2));
if (token) fs.writeFileSync("./auth.json", JSON.stringify({ token }, null, 2));

console.log("\nLeaving browser open 15s...");
await page.waitForTimeout(15000);
await ctx.close();
console.log("done.");
