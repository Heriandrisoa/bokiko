/**
 * Book Reader Theme Engine
 * Manages preset selection, custom user color palettes, and instant live persistence.
 */
(() => {
  const THEME_KEY = 'bookreader_theme_preset';
  const CUSTOM_KEY = 'bookreader_theme_custom';

  const PRESETS = {
    'refined-library': {
      id: 'refined-library',
      canvas: '#f6f5f1',
      accent: '#709775',
      tomato: '#c68b45'
    },
    'earthy-botanical': {
      id: 'earthy-botanical',
      canvas: '#f2f4f2',
      accent: '#709775',
      tomato: '#c9725b'
    },
    'nordic-minimalist': {
      id: 'nordic-minimalist',
      canvas: '#f8f9fa',
      accent: '#709775',
      tomato: '#263d2c'
    },
    'classic-editorial': {
      id: 'classic-editorial',
      canvas: '#f9f8f5',
      accent: '#9e3c28',
      tomato: '#d94a32'
    }
  };

  function parseHex(hex) {
    if (!hex) return { r: 0, g: 0, b: 0 };
    let c = hex.replace('#', '').trim();
    if (c.length === 3) c = c.split('').map(x => x + x).join('');
    if (c.length !== 6) return { r: 0, g: 0, b: 0 };
    const num = parseInt(c, 16);
    return { r: (num >> 16) & 255, g: (num >> 8) & 255, b: num & 255 };
  }

  function adjustBrightness(hex, delta) {
    const { r, g, b } = parseHex(hex);
    const clamp = (val) => Math.max(0, Math.min(255, Math.round(val + delta)));
    const nr = clamp(r);
    const ng = clamp(g);
    const nb = clamp(b);
    return `#${((1 << 24) + (nr << 16) + (ng << 8) + nb).toString(16).slice(1)}`;
  }

  function toRgba(hex, alpha) {
    const { r, g, b } = parseHex(hex);
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  }

  function applyThemeColors(colors) {
    if (!colors || !colors.canvas || !colors.accent || !colors.tomato) return;
    const root = document.documentElement;

    root.style.setProperty('--color-canvas', colors.canvas);
    root.style.setProperty('--color-accent', colors.accent);
    root.style.setProperty('--color-accent-hover', adjustBrightness(colors.accent, -25));
    root.style.setProperty('--color-accent-active', adjustBrightness(colors.accent, -45));
    root.style.setProperty('--color-accent-subtle', toRgba(colors.accent, 0.12));
    root.style.setProperty('--color-accent-border', toRgba(colors.accent, 0.28));

    root.style.setProperty('--color-tomato', colors.tomato);
    root.style.setProperty('--color-tomato-hover', adjustBrightness(colors.tomato, -25));
    root.style.setProperty('--color-tomato-subtle', toRgba(colors.tomato, 0.12));
    root.style.setProperty('--color-tomato-border', toRgba(colors.tomato, 0.28));

    // Dynamic borders tuned to canvas tone
    root.style.setProperty('--color-border', adjustBrightness(colors.canvas, -22));
    root.style.setProperty('--color-border-hover', adjustBrightness(colors.canvas, -36));

    window.dispatchEvent(new CustomEvent('bookreader:themechanged', { detail: colors }));
  }

  function getStoredTheme() {
    let preset = 'refined-library';
    let custom = null;
    try {
      const storedPreset = localStorage.getItem(THEME_KEY);
      if (storedPreset) preset = storedPreset;
      const storedCustom = localStorage.getItem(CUSTOM_KEY);
      if (storedCustom) custom = JSON.parse(storedCustom);
    } catch (e) {
      console.warn('Storage unavailable', e);
    }
    return { preset, custom };
  }

  function init() {
    const { preset, custom } = getStoredTheme();
    if (preset === 'custom' && custom && custom.canvas && custom.accent && custom.tomato) {
      applyThemeColors(custom);
    } else if (PRESETS[preset]) {
      applyThemeColors(PRESETS[preset]);
    } else {
      applyThemeColors(PRESETS['refined-library']);
    }
  }

  window.BookReaderTheme = {
    PRESETS,
    applyThemeColors,
    setPreset: (presetKey) => {
      if (PRESETS[presetKey]) {
        try {
          localStorage.setItem(THEME_KEY, presetKey);
        } catch (e) {}
        applyThemeColors(PRESETS[presetKey]);
      }
    },
    setCustom: (colors) => {
      if (!colors.canvas || !colors.accent || !colors.tomato) return;
      try {
        localStorage.setItem(THEME_KEY, 'custom');
        localStorage.setItem(CUSTOM_KEY, JSON.stringify(colors));
      } catch (e) {}
      applyThemeColors(colors);
    },
    getStoredTheme
  };

  // Run immediately to prevent flash of wrong theme
  init();
})();

