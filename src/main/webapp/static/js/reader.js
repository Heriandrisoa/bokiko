import * as pdfjsLib from '../pdfjs/pdf.min.mjs';

// Everything is resolved relative to this file, so it works under any context path.
pdfjsLib.GlobalWorkerOptions.workerSrc = new URL('../pdfjs/pdf.worker.min.mjs', import.meta.url).href;

const root = document.getElementById('reader');
const cfg = root.dataset;
const stage = document.getElementById('stage');
const canvas = document.getElementById('canvas');
const g = canvas.getContext('2d');
const textLayer = document.getElementById('textLayer');
const statusEl = document.getElementById('status');
const pageInput = document.getElementById('pageInput');
const prevBtn = document.getElementById('prevBtn');
const nextBtn = document.getElementById('nextBtn');
const fullscreenBtn = document.getElementById('fullscreenBtn');
const searchInput = document.getElementById('searchInput');
const searchPrevBtn = document.getElementById('searchPrevBtn');
const searchNextBtn = document.getElementById('searchNextBtn');
const searchStatus = document.getElementById('searchStatus');
const readerMessages = document.getElementById('readerMessages').dataset;

let pdf = null;
let total = 0;
let current = 1;
let mode = 'width';        // 'width' | 'page' | 'custom'
let customScale = 1;
let lastScale = 1;
let rendering = false;
let queued = false;
let searchMatches = [];
let searchPosition = -1;
let searchToken = 0;
let pendingScrollAnchor = null;
let pendingTurnDirection = null;

/* ---------- bookmarks and notes ---------- */
const marksButton = document.createElement('button');
marksButton.type = 'button';
marksButton.className = 'btn small';
marksButton.textContent = readerMessages.marks;
marksButton.setAttribute('aria-expanded', 'false');

const AUTO_ADVANCE_KEY = 'bokiko.reader.autoAdvance';
let autoAdvance = true;
try { autoAdvance = localStorage.getItem(AUTO_ADVANCE_KEY) !== 'false'; } catch (_) { /* storage can be unavailable */ }
const autoAdvanceButton = document.createElement('button');
autoAdvanceButton.type = 'button';
autoAdvanceButton.className = 'btn small reader-auto-advance';

function updateAutoAdvanceButton() {
  const label = autoAdvance ? readerMessages.autoAdvanceOn : readerMessages.autoAdvanceOff;
  autoAdvanceButton.textContent = label;
  autoAdvanceButton.title = readerMessages.autoAdvance;
  autoAdvanceButton.setAttribute('aria-label', readerMessages.autoAdvance);
  autoAdvanceButton.setAttribute('aria-pressed', String(autoAdvance));
}

autoAdvanceButton.addEventListener('click', () => {
  autoAdvance = !autoAdvance;
  try { localStorage.setItem(AUTO_ADVANCE_KEY, String(autoAdvance)); } catch (_) { /* preference remains for this reader */ }
  updateAutoAdvanceButton();
});
updateAutoAdvanceButton();

const marksPanel = document.createElement('aside');
marksPanel.className = 'marks-panel';
marksPanel.hidden = true;
marksPanel.setAttribute('aria-label', readerMessages.marks);
marksPanel.innerHTML = '<div class="marks-panel__head"><h2></h2><button type="button" class="btn small icon-only" aria-label="Close">×</button></div>'
  + '<form class="mark-form"><label for="markNote"></label><textarea id="markNote" name="note" maxlength="500"></textarea><button type="submit" class="btn primary small"></button></form>'
  + '<div class="marks-list" aria-live="polite"></div>';
marksPanel.querySelector('h2').textContent = readerMessages.marks;
marksPanel.querySelector('label').textContent = readerMessages.note;
marksPanel.querySelector('textarea').placeholder = readerMessages.notePlaceholder;
marksPanel.querySelector('form button').textContent = readerMessages.addMark;
root.append(marksPanel);
document.querySelector('.reader-zoom').insertBefore(autoAdvanceButton, fullscreenBtn);
document.querySelector('.reader-zoom').insertBefore(marksButton, fullscreenBtn);

const marksList = marksPanel.querySelector('.marks-list');
const markForm = marksPanel.querySelector('.mark-form');
const markNote = marksPanel.querySelector('textarea');

function toggleMarks(show) {
  marksPanel.hidden = !show;
  marksButton.setAttribute('aria-expanded', String(show));
  if (show) markNote.focus();
}

function renderMarks(marks) {
  marksList.replaceChildren();
  if (!marks.length) {
    const empty = document.createElement('p');
    empty.className = 'marks-empty';
    empty.textContent = cfg.msgEmptyMarks;
    marksList.append(empty);
    return;
  }
  marks.forEach((mark) => {
    const item = document.createElement('article');
    item.className = 'mark-item';
    const jump = document.createElement('button');
    jump.type = 'button'; jump.className = 'mark-item__page'; jump.textContent = `p. ${mark.page}`;
    jump.addEventListener('click', () => { goTo(mark.page); toggleMarks(false); });
    const note = document.createElement('p'); note.textContent = mark.note || '—';
    const remove = document.createElement('button');
    remove.type = 'button'; remove.className = 'mark-item__remove'; remove.textContent = '×';
    remove.setAttribute('aria-label', cfg.msgRemoveMark);
    remove.addEventListener('click', () => removeMark(mark.id));
    item.append(jump, note, remove); marksList.append(item);
  });
}

async function loadMarks() {
  try {
    const response = await fetch(`${cfg.marksUrl}?bookId=${encodeURIComponent(cfg.bookId)}`, { headers: { Accept: 'application/json' } });
    if (response.ok) renderMarks(await response.json());
  } catch (error) { console.error('Unable to load bookmarks.', error); }
}

async function removeMark(id) {
  try {
    const response = await fetch(`${cfg.marksUrl}?bookId=${encodeURIComponent(cfg.bookId)}&id=${encodeURIComponent(id)}`, { method: 'DELETE', headers: { 'X-CSRF-Token': cfg.csrf } });
    if (response.ok) loadMarks();
  } catch (error) { console.error('Unable to remove bookmark.', error); }
}

marksButton.addEventListener('click', () => toggleMarks(marksPanel.hidden));
marksPanel.querySelector('.marks-panel__head button').addEventListener('click', () => toggleMarks(false));
markForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const body = new URLSearchParams({ bookId: cfg.bookId, page: String(current), note: markNote.value });
  try {
    const response = await fetch(cfg.marksUrl, { method: 'POST', headers: { 'X-CSRF-Token': cfg.csrf, 'Content-Type': 'application/x-www-form-urlencoded' }, body });
    if (response.ok) { markNote.value = ''; loadMarks(); }
  } catch (error) { console.error('Unable to save bookmark.', error); }
});

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
      await renderTextLayer(page, viewport);
      canvas.hidden = false;
    } while (queued);
  } catch (e) {
    console.error(e);
  } finally {
    if (pendingScrollAnchor) {
      const anchor = pendingScrollAnchor;
      pendingScrollAnchor = null;
      const target = anchor === 'bottom' ? Math.max(stage.scrollHeight - stage.clientHeight, 0) : 0;
      lastStageScrollTop = target;
      stage.scrollTop = target;
      const outgoing = pageView.querySelector('.page-transition__outgoing');
      if (outgoing) window.requestAnimationFrame(() => outgoing.classList.add('is-leaving'));
    }
    rendering = false;
  }
}

async function renderTextLayer(page, viewport) {
  textLayer.replaceChildren();
  const layer = new pdfjsLib.TextLayer({
    textContentSource: await page.getTextContent(),
    container: textLayer,
    viewport,
  });
  await layer.render();
  highlightSearchTerms();
}

function highlightSearchTerms() {
  const query = searchInput.value.trim().toLocaleLowerCase();
  if (!query) return;
  textLayer.querySelectorAll('span').forEach((span) => {
    if (span.textContent.toLocaleLowerCase().includes(query)) span.classList.add('search-hit');
  });
}

function updateControls() {
  pageInput.value = current;
  prevBtn.disabled = current <= 1;
  nextBtn.disabled = current >= total;
}

function preparePageTransition(direction) {
  if (canvas.hidden || !canvas.width || !canvas.height) return;
  pageView.querySelectorAll('.page-transition__outgoing').forEach((snapshot) => snapshot.remove());
  const snapshot = document.createElement('canvas');
  snapshot.className = 'page-transition__outgoing';
  snapshot.width = canvas.width;
  snapshot.height = canvas.height;
  snapshot.style.width = canvas.style.width;
  snapshot.style.height = canvas.style.height;
  snapshot.dataset.direction = direction;
  snapshot.getContext('2d').drawImage(canvas, 0, 0);
  snapshot.addEventListener('animationend', () => snapshot.remove(), { once: true });
  pageView.append(snapshot);
}

function goTo(n, scrollAnchor = 'top') {
  n = clamp(n);
  if (n === current) { updateControls(); return; }
  const direction = n > current ? 'forward' : 'backward';
  preparePageTransition(direction);
  current = n;
  updateControls();
  pendingScrollAnchor = scrollAnchor;
  pendingTurnDirection = direction;
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

async function searchDocument(direction = 1) {
  const query = searchInput.value.trim().toLocaleLowerCase();
  if (!query || !pdf) {
    searchMatches = [];
    searchPosition = -1;
    searchStatus.textContent = '';
    draw();
    return;
  }
  const token = ++searchToken;
  searchStatus.textContent = '…';
  const matches = [];
  for (let pageNumber = 1; pageNumber <= total; pageNumber++) {
    const page = await pdf.getPage(pageNumber);
    const content = await page.getTextContent();
    const text = content.items.map((item) => item.str).join(' ').toLocaleLowerCase();
    if (text.includes(query)) matches.push(pageNumber);
    if (token !== searchToken) return;
  }
  searchMatches = matches;
  if (!matches.length) {
    searchPosition = -1;
    searchStatus.textContent = '0';
    draw();
    return;
  }
  const currentIndex = matches.indexOf(current);
  searchPosition = currentIndex >= 0 ? currentIndex : (direction > 0 ? 0 : matches.length - 1);
  searchStatus.textContent = `${searchPosition + 1}/${matches.length}`;
  goTo(matches[searchPosition]);
}

function moveSearch(direction) {
  if (!searchMatches.length) return searchDocument(direction);
  searchPosition = (searchPosition + direction + searchMatches.length) % searchMatches.length;
  searchStatus.textContent = `${searchPosition + 1}/${searchMatches.length}`;
  goTo(searchMatches[searchPosition]);
}

searchInput.addEventListener('change', () => searchDocument(1));
searchInput.addEventListener('keydown', (event) => {
  if (event.key === 'Enter') { event.preventDefault(); searchDocument(event.shiftKey ? -1 : 1); }
  if (event.key === 'Escape') { searchInput.value = ''; searchDocument(); searchInput.blur(); }
});
searchNextBtn.addEventListener('click', () => moveSearch(1));
searchPrevBtn.addEventListener('click', () => moveSearch(-1));

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

/* Continue naturally across page edges in either scroll direction. */
let autoAdvanceLocked = false;
let lastStageScrollTop = 0;
function advanceAtEdge(direction) {
  if (!autoAdvance || autoAdvanceLocked || rendering) return;
  if (stage.scrollHeight <= stage.clientHeight + 4) return;
  const atBottom = stage.scrollTop + stage.clientHeight >= stage.scrollHeight - 3;
  const atTop = stage.scrollTop <= 3;
  if ((direction > 0 && (!atBottom || current >= total)) || (direction < 0 && (!atTop || current <= 1))) return;
  autoAdvanceLocked = true;
  goTo(current + direction, direction > 0 ? 'top' : 'bottom');
  window.setTimeout(() => { autoAdvanceLocked = false; }, 450);
}
stage.addEventListener('scroll', () => {
  const direction = Math.sign(stage.scrollTop - lastStageScrollTop);
  lastStageScrollTop = stage.scrollTop;
  if (direction) advanceAtEdge(direction);
}, { passive: true });
stage.addEventListener('wheel', (event) => {
  const direction = Math.sign(event.deltaY);
  if (direction) window.requestAnimationFrame(() => advanceAtEdge(direction));
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
    loadMarks();
    await draw();
  } catch (e) {
    console.error(e);
    statusEl.textContent = cfg.msgError;
    statusEl.hidden = false;
  }
})();
