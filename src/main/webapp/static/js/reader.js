import * as pdfjsLib from '../pdfjs/pdf.min.mjs';

// Everything is resolved relative to this file, so it works under any context path.
pdfjsLib.GlobalWorkerOptions.workerSrc = new URL('../pdfjs/pdf.worker.min.mjs', import.meta.url).href;

const root = document.getElementById('reader');
const cfg = root.dataset;
const stage = document.getElementById('stage');
const canvas = document.getElementById('canvas');
const g = canvas.getContext('2d');
const statusEl = document.getElementById('status');
const pageInput = document.getElementById('pageInput');
const prevBtn = document.getElementById('prevBtn');
const nextBtn = document.getElementById('nextBtn');
const fullscreenBtn = document.getElementById('fullscreenBtn');

let pdf = null;
let total = 0;
let current = 1;
let mode = 'width';        // 'width' | 'page' | 'custom'
let customScale = 1;
let lastScale = 1;
let rendering = false;
let queued = false;

const clamp = (n) => Math.min(Math.max(n, 1), total || 1);

/* ---------- rendering ---------- */
async function draw() {
  if (!pdf) return;
  if (rendering) { queued = true; return; }
  rendering = true;
  try {
    do {
      queued = false;
      const page = await pdf.getPage(current);
      const base = page.getViewport({ scale: 1 });
      const availW = Math.max(stage.clientWidth - 16, 100);
      const availH = Math.max(stage.clientHeight - 16, 100);
      let scale;
      if (mode === 'width') scale = availW / base.width;
      else if (mode === 'page') scale = Math.min(availW / base.width, availH / base.height);
      else scale = customScale;
      lastScale = scale;

      const viewport = page.getViewport({ scale });
      const dpr = window.devicePixelRatio || 1;
      canvas.width = Math.floor(viewport.width * dpr);
      canvas.height = Math.floor(viewport.height * dpr);
      canvas.style.width = Math.floor(viewport.width) + 'px';
      canvas.style.height = Math.floor(viewport.height) + 'px';
      await page.render({
        canvasContext: g,
        viewport,
        transform: dpr !== 1 ? [dpr, 0, 0, dpr, 0, 0] : null,
      }).promise;
      canvas.hidden = false;
    } while (queued);
  } catch (e) {
    console.error(e);
  } finally {
    rendering = false;
  }
}

function updateControls() {
  pageInput.value = current;
  prevBtn.disabled = current <= 1;
  nextBtn.disabled = current >= total;
}

function goTo(n) {
  n = clamp(n);
  if (n === current) { updateControls(); return; }
  current = n;
  updateControls();
  stage.scrollTop = 0;
  draw();
  scheduleSave();
}

function zoom(factor) {
  customScale = Math.min(Math.max((mode === 'custom' ? customScale : lastScale) * factor, 0.25), 5);
  mode = 'custom';
  draw();
}

/* ---------- reading progress (synced through PostgreSQL) ---------- */
let dirty = false;
let saveTimer = null;

function scheduleSave() {
  dirty = true;
  clearTimeout(saveTimer);
  saveTimer = setTimeout(flush, 1000);
}

function flush() {
  if (!dirty) return;
  dirty = false;
  clearTimeout(saveTimer);
  fetch(cfg.progressUrl, {
    method: 'POST',
    headers: {
      'X-CSRF-Token': cfg.csrf,
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams({ id: cfg.bookId, page: String(current) }),
    keepalive: true,                 // still sent when the tab is closing
  }).catch(() => { dirty = true; });
}

document.addEventListener('visibilitychange', () => {
  if (document.visibilityState === 'hidden') flush();
});
window.addEventListener('pagehide', flush);

/* ---------- controls ---------- */
prevBtn.addEventListener('click', () => goTo(current - 1));
nextBtn.addEventListener('click', () => goTo(current + 1));
document.getElementById('zoomInBtn').addEventListener('click', () => zoom(1.2));
document.getElementById('zoomOutBtn').addEventListener('click', () => zoom(1 / 1.2));
document.getElementById('fitWidthBtn').addEventListener('click', () => { mode = 'width'; draw(); });
document.getElementById('fitPageBtn').addEventListener('click', () => { mode = 'page'; draw(); });

function updateFullscreenControl() {
  const active = document.fullscreenElement === root;
  const label = active ? cfg.msgExitFullscreen : cfg.msgFullscreen;
  fullscreenBtn.setAttribute('aria-pressed', String(active));
  fullscreenBtn.setAttribute('aria-label', label);
  fullscreenBtn.title = label;
}

fullscreenBtn.addEventListener('click', async () => {
  try {
    if (document.fullscreenElement === root) await document.exitFullscreen();
    else await root.requestFullscreen();
  } catch (e) {
    console.error('Fullscreen is unavailable.', e);
  }
});
document.addEventListener('fullscreenchange', () => {
  updateFullscreenControl();
  if (mode !== 'custom') draw();
});

pageInput.addEventListener('change', () => {
  const n = parseInt(pageInput.value, 10);
  if (Number.isNaN(n)) updateControls(); else goTo(n);
});

document.addEventListener('keydown', (e) => {
  if (e.target instanceof HTMLInputElement) return;
  if (e.key === 'ArrowRight' || e.key === 'PageDown') { goTo(current + 1); e.preventDefault(); }
  else if (e.key === 'ArrowLeft' || e.key === 'PageUp') { goTo(current - 1); e.preventDefault(); }
  else if (e.key === 'Home') { goTo(1); e.preventDefault(); }
  else if (e.key === 'End') { goTo(total); e.preventDefault(); }
});

// swipe left/right to turn pages (only when the page isn't wider than the screen)
let tx = 0, ty = 0;
stage.addEventListener('touchstart', (e) => {
  tx = e.changedTouches[0].clientX;
  ty = e.changedTouches[0].clientY;
}, { passive: true });
stage.addEventListener('touchend', (e) => {
  if (stage.scrollWidth > stage.clientWidth + 1) return;
  const dx = e.changedTouches[0].clientX - tx;
  const dy = e.changedTouches[0].clientY - ty;
  if (Math.abs(dx) > 60 && Math.abs(dx) > Math.abs(dy) * 1.5) goTo(current + (dx < 0 ? 1 : -1));
}, { passive: true });

let resizeTimer = null;
window.addEventListener('resize', () => {
  clearTimeout(resizeTimer);
  resizeTimer = setTimeout(() => { if (mode !== 'custom') draw(); }, 150);
});

if (!document.fullscreenEnabled) fullscreenBtn.hidden = true;

/* ---------- start ---------- */
(async () => {
  try {
    pdf = await pdfjsLib.getDocument({
      url: cfg.fileUrl,
      cMapUrl: new URL('../pdfjs/cmaps/', import.meta.url).href,
      cMapPacked: true,
      standardFontDataUrl: new URL('../pdfjs/standard_fonts/', import.meta.url).href,
      isEvalSupported: false,        // no eval() on untrusted PDFs
    }).promise;
    total = pdf.numPages;
    current = clamp(parseInt(cfg.startPage, 10) || 1);
    pageInput.max = total;
    document.getElementById('pageTotal').textContent = total;
    statusEl.hidden = true;
    updateControls();
    await draw();
  } catch (e) {
    console.error(e);
    statusEl.textContent = cfg.msgError;
    statusEl.hidden = false;
  }
})();
