import { chromium } from "playwright";
import fs from "node:fs";
import path from "node:path";

export const BASE = "https://allset.am";
export const API = "https://api.allset.am";
export const PROFILE_DIR = path.resolve("./.profile");
export const ARTIFACTS = path.resolve("./artifacts");

fs.mkdirSync(ARTIFACTS, { recursive: true });

// Launch the user's real Google Chrome with a persistent profile so a manual
// Google login only has to happen once and is reused on later runs.
export async function launch() {
  const ctx = await chromium.launchPersistentContext(PROFILE_DIR, {
    headless: false,
    channel: "chrome",
    viewport: { width: 1440, height: 900 },
    args: ["--disable-blink-features=AutomationControlled"],
  });
  return ctx;
}

// Collects runtime problems surfaced by the app: JS exceptions, console errors,
// and failed / 4xx-5xx network responses to the API.
export function attachDiagnostics(page, sink) {
  page.on("console", (msg) => {
    if (msg.type() === "error") sink.consoleErrors.push({ url: page.url(), text: msg.text() });
  });
  page.on("pageerror", (err) => {
    sink.pageErrors.push({ url: page.url(), text: String(err) });
  });
  page.on("requestfailed", (req) => {
    sink.requestFailures.push({ url: req.url(), method: req.method(), error: req.failure()?.errorText });
  });
  page.on("response", async (res) => {
    const u = res.url();
    if (u.includes("api.allset.am") && res.status() >= 400) {
      sink.apiErrors.push({ status: res.status(), method: res.request().method(), url: u });
    }
  });
}

export function newSink() {
  return { consoleErrors: [], pageErrors: [], requestFailures: [], apiErrors: [] };
}

export async function shot(page, name) {
  const file = path.join(ARTIFACTS, `${name}.png`);
  await page.screenshot({ path: file, fullPage: true }).catch(() => {});
  console.log(`  [screenshot] ${file}`);
  return file;
}

// Dumps visible interactive elements so we can learn selectors for a UI we
// can't see the source of.
export async function dumpInteractive(page, label) {
  const els = await page.evaluate(() => {
    const out = [];
    const nodes = document.querySelectorAll(
      "button, a, input, textarea, select, [role=button], [contenteditable=true]"
    );
    for (const n of nodes) {
      const r = n.getBoundingClientRect();
      if (r.width === 0 || r.height === 0) continue;
      out.push({
        tag: n.tagName.toLowerCase(),
        type: n.getAttribute("type") || "",
        text: (n.innerText || n.value || "").trim().slice(0, 60),
        placeholder: n.getAttribute("placeholder") || "",
        name: n.getAttribute("name") || "",
        testid: n.getAttribute("data-testid") || n.getAttribute("data-test") || "",
        aria: n.getAttribute("aria-label") || "",
        href: n.getAttribute("href") || "",
      });
    }
    return out;
  });
  console.log(`\n=== interactive elements: ${label} (${els.length}) ===`);
  for (const e of els) {
    const bits = [e.tag + (e.type ? `[${e.type}]` : "")];
    if (e.text) bits.push(`text=${JSON.stringify(e.text)}`);
    if (e.placeholder) bits.push(`ph=${JSON.stringify(e.placeholder)}`);
    if (e.testid) bits.push(`testid=${e.testid}`);
    if (e.aria) bits.push(`aria=${JSON.stringify(e.aria)}`);
    if (e.href) bits.push(`href=${e.href}`);
    console.log("  - " + bits.join(" "));
  }
  return els;
}

// Resolves once the SPA makes an authenticated call to the API (Bearer token),
// which is a reliable "user is logged in" signal without knowing the DOM.
export function waitForLogin(page, timeoutMs = 360000) {
  return new Promise((resolve) => {
    let done = false;
    const finish = (token) => {
      if (done) return;
      done = true;
      resolve(token);
    };
    page.on("request", (req) => {
      const auth = req.headers()["authorization"];
      if (req.url().includes("api.allset.am") && auth && /^bearer /i.test(auth)) {
        finish(auth.replace(/^bearer /i, ""));
      }
    });
    setTimeout(() => finish(null), timeoutMs);
  });
}
