<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titleKey" value="settings.title"/><c:set var="mainClass" value="wide"/>
<%@ include file="_header.jspf" %>

<section id="settingsPage" class="settings-view" aria-labelledby="settings-title">
  <!-- Page Header -->
  <div class="page-heading">
    <div class="page-heading__content">
      <span class="eyebrow"><fmt:message key="app.kicker"/></span>
      <h1 id="settings-title"><fmt:message key="settings.title"/></h1>
      <p class="settings-subtitle"><fmt:message key="settings.desc"/></p>
    </div>
  </div>

  <div class="settings-section">
    <div class="settings-section__head">
      <h2><fmt:message key="settings.theme.title"/></h2>
      <p><fmt:message key="settings.theme.subtitle"/></p>
    </div>

    <!-- Presets Grid -->
    <div class="theme-presets-block">
      <h3 class="settings-block-title"><fmt:message key="settings.theme.presets"/></h3>
      <div class="presets-grid" id="presetsGrid">
        <!-- Preset 1: Refined Library -->
        <article class="preset-card" data-preset="refined-library" role="button" tabindex="0">
          <div class="preset-card__preview">
            <span class="color-dot dot-canvas" style="background: #f6f5f1;" title="Fond"></span>
            <span class="color-dot dot-accent" style="background: #709775;" title="Principal"></span>
            <span class="color-dot dot-highlight" style="background: #c68b45;" title="Accent"></span>
          </div>
          <div class="preset-card__body">
            <div class="preset-card__title-row">
              <h4><fmt:message key="settings.theme.preset.refined"/></h4>
              <span class="active-badge"><fmt:message key="settings.theme.active"/></span>
            </div>
            <p><fmt:message key="settings.theme.preset.refined.desc"/></p>
          </div>
        </article>

        <!-- Preset 2: Earthy Botanical -->
        <article class="preset-card" data-preset="earthy-botanical" role="button" tabindex="0">
          <div class="preset-card__preview">
            <span class="color-dot dot-canvas" style="background: #f2f4f2;" title="Fond"></span>
            <span class="color-dot dot-accent" style="background: #709775;" title="Principal"></span>
            <span class="color-dot dot-highlight" style="background: #c9725b;" title="Accent"></span>
          </div>
          <div class="preset-card__body">
            <div class="preset-card__title-row">
              <h4><fmt:message key="settings.theme.preset.botanical"/></h4>
              <span class="active-badge"><fmt:message key="settings.theme.active"/></span>
            </div>
            <p><fmt:message key="settings.theme.preset.botanical.desc"/></p>
          </div>
        </article>

        <!-- Preset 3: Nordic Minimalist -->
        <article class="preset-card" data-preset="nordic-minimalist" role="button" tabindex="0">
          <div class="preset-card__preview">
            <span class="color-dot dot-canvas" style="background: #f8f9fa;" title="Fond"></span>
            <span class="color-dot dot-accent" style="background: #709775;" title="Principal"></span>
            <span class="color-dot dot-highlight" style="background: #263d2c;" title="Accent"></span>
          </div>
          <div class="preset-card__body">
            <div class="preset-card__title-row">
              <h4><fmt:message key="settings.theme.preset.nordic"/></h4>
              <span class="active-badge"><fmt:message key="settings.theme.active"/></span>
            </div>
            <p><fmt:message key="settings.theme.preset.nordic.desc"/></p>
          </div>
        </article>

        <!-- Preset 4: Classic Editorial -->
        <article class="preset-card" data-preset="classic-editorial" role="button" tabindex="0">
          <div class="preset-card__preview">
            <span class="color-dot dot-canvas" style="background: #f9f8f5;" title="Fond"></span>
            <span class="color-dot dot-accent" style="background: #9e3c28;" title="Principal"></span>
            <span class="color-dot dot-highlight" style="background: #d94a32;" title="Accent"></span>
          </div>
          <div class="preset-card__body">
            <div class="preset-card__title-row">
              <h4><fmt:message key="settings.theme.preset.classic"/></h4>
              <span class="active-badge"><fmt:message key="settings.theme.active"/></span>
            </div>
            <p><fmt:message key="settings.theme.preset.classic.desc"/></p>
          </div>
        </article>
      </div>
    </div>

    <!-- Custom Theme Creator -->
    <div class="custom-theme-block card">
      <div class="custom-theme-block__head">
        <div>
          <h3><fmt:message key="settings.theme.custom.title"/></h3>
          <p><fmt:message key="settings.theme.custom.desc"/></p>
        </div>
      </div>

      <div class="custom-theme-builder">
        <!-- Color Inputs -->
        <div class="custom-theme-inputs">
          <div class="color-field">
            <label for="colorCanvas"><fmt:message key="settings.theme.canvas"/></label>
            <div class="color-input-wrap">
              <input type="color" id="pickerCanvas" value="#f6f5f1">
              <input type="text" id="colorCanvas" class="hex-text-input" value="#f6f5f1" maxlength="7">
            </div>
          </div>

          <div class="color-field">
            <label for="colorAccent"><fmt:message key="settings.theme.accent"/></label>
            <div class="color-input-wrap">
              <input type="color" id="pickerAccent" value="#709775">
              <input type="text" id="colorAccent" class="hex-text-input" value="#709775" maxlength="7">
            </div>
          </div>

          <div class="color-field">
            <label for="colorHighlight"><fmt:message key="settings.theme.highlight"/></label>
            <div class="color-input-wrap">
              <input type="color" id="pickerHighlight" value="#c68b45">
              <input type="text" id="colorHighlight" class="hex-text-input" value="#c68b45" maxlength="7">
            </div>
          </div>

          <div class="custom-theme-actions">
            <button type="button" id="applyCustomThemeBtn" class="btn btn-primary">
              <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              <span><fmt:message key="settings.theme.apply"/></span>
            </button>
            <button type="button" id="resetThemeBtn" class="btn btn-ghost">
              <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
                <path d="M3 3v5h5"></path>
              </svg>
              <span><fmt:message key="settings.theme.reset"/></span>
            </button>
          </div>
        </div>

        <!-- Live Preview Stage -->
        <div class="custom-preview-panel">
          <span class="preview-panel-title"><fmt:message key="settings.theme.preview"/></span>
          <div class="preview-stage" id="livePreviewStage">
            <div class="preview-mini-header">
              <div class="preview-mini-brand">
                <span class="preview-mini-mark"></span>
                <span>Mon lecteur</span>
              </div>
              <span class="preview-mini-tag">Actif</span>
            </div>

            <div class="preview-mini-body">
              <div class="preview-mini-card">
                <div class="preview-mini-cover">
                  <div class="preview-spine"></div>
                  <div class="preview-progress-line"></div>
                </div>
                <div class="preview-mini-meta">
                  <span class="preview-title">Le Livre d'Exemple</span>
                  <span class="preview-sub">Page 42 sur 180</span>
                </div>
              </div>

              <div class="preview-mini-actions">
                <button type="button" class="preview-btn-sample">Bouton Principal</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>

<script>
(() => {
  const engine = window.BookReaderTheme;
  if (!engine) return;

  const presetsGrid = document.getElementById('presetsGrid');
  const pickerCanvas = document.getElementById('pickerCanvas');
  const textCanvas = document.getElementById('colorCanvas');
  const pickerAccent = document.getElementById('pickerAccent');
  const textAccent = document.getElementById('colorAccent');
  const pickerHighlight = document.getElementById('pickerHighlight');
  const textHighlight = document.getElementById('colorHighlight');
  const applyCustomBtn = document.getElementById('applyCustomThemeBtn');
  const resetBtn = document.getElementById('resetThemeBtn');
  const previewStage = document.getElementById('livePreviewStage');

  function updateActiveUI() {
    const { preset, custom } = engine.getStoredTheme();
    document.querySelectorAll('.preset-card').forEach(card => {
      if (card.dataset.preset === preset) {
        card.classList.add('is-active');
      } else {
        card.classList.remove('is-active');
      }
    });

    let currentColors;
    if (preset === 'custom' && custom) {
      currentColors = custom;
    } else if (engine.PRESETS[preset]) {
      currentColors = engine.PRESETS[preset];
    } else {
      currentColors = engine.PRESETS['refined-library'];
    }

    setInputs(currentColors);
    updatePreview(currentColors);
  }

  function setInputs(colors) {
    if (colors.canvas) {
      pickerCanvas.value = colors.canvas;
      textCanvas.value = colors.canvas;
    }
    if (colors.accent) {
      pickerAccent.value = colors.accent;
      textAccent.value = colors.accent;
    }
    if (colors.tomato) {
      pickerHighlight.value = colors.tomato;
      textHighlight.value = colors.tomato;
    }
  }

  function updatePreview(colors) {
    if (!previewStage) return;
    previewStage.style.setProperty('--pv-canvas', colors.canvas);
    previewStage.style.setProperty('--pv-accent', colors.accent);
    previewStage.style.setProperty('--pv-highlight', colors.tomato);
  }

  function syncFromPickers() {
    const colors = {
      canvas: pickerCanvas.value,
      accent: pickerAccent.value,
      tomato: pickerHighlight.value
    };
    textCanvas.value = colors.canvas;
    textAccent.value = colors.accent;
    textHighlight.value = colors.tomato;
    updatePreview(colors);
    // Live update full app while tweaking
    engine.applyThemeColors(colors);
  }

  function syncFromText(input, picker) {
    let val = input.value.trim();
    if (/^#[0-9a-fA-F]{6}$/.test(val)) {
      picker.value = val;
      syncFromPickers();
    }
  }

  // Bind Preset clicks
  presetsGrid.addEventListener('click', (e) => {
    const card = e.target.closest('.preset-card');
    if (!card) return;
    const key = card.dataset.preset;
    engine.setPreset(key);
    updateActiveUI();
  });

  // Color Pickers
  [pickerCanvas, pickerAccent, pickerHighlight].forEach(p => {
    p.addEventListener('input', syncFromPickers);
  });

  textCanvas.addEventListener('input', () => syncFromText(textCanvas, pickerCanvas));
  textAccent.addEventListener('input', () => syncFromText(textAccent, pickerAccent));
  textHighlight.addEventListener('input', () => syncFromText(textHighlight, pickerHighlight));

  // Save Custom Theme
  applyCustomBtn.addEventListener('click', () => {
    const colors = {
      canvas: pickerCanvas.value,
      accent: pickerAccent.value,
      tomato: pickerHighlight.value
    };
    engine.setCustom(colors);
    document.querySelectorAll('.preset-card').forEach(c => c.classList.remove('is-active'));
  });

  // Reset to default
  resetBtn.addEventListener('click', () => {
    engine.setPreset('refined-library');
    updateActiveUI();
  });

  updateActiveUI();
})();
</script>

<%@ include file="_footer.jspf" %>

