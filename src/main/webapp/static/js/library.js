(() => {
  const root = document.getElementById('library');
  if (!root) return;
  const cfg = root.dataset;
  const maxBytes = parseInt(cfg.maxBytes, 10);
  const btn = document.getElementById('importBtn');
  const input = document.getElementById('fileInput');
  const box = document.getElementById('uploadStatus');
  const dropZone = document.getElementById('dropZone');

  // delete confirmation
  document.querySelectorAll('.delete-form').forEach((form) => {
    form.addEventListener('submit', (e) => {
      if (!window.confirm(form.dataset.confirm)) e.preventDefault();
    });
  });

  function addRow(name) {
    const row = document.createElement('div');
    row.className = 'upload-item';
    const n = document.createElement('span');
    n.className = 'name';
    n.textContent = name;                       // textContent: file names are never parsed as HTML
    const s = document.createElement('span');
    s.className = 'state';
    row.append(n, s);
    box.appendChild(row);
    return s;
  }

  function setState(el, text, kind) {
    el.textContent = text;
    el.className = 'state' + (kind ? ' ' + kind : '');
  }

  function uploadOne(file, stateEl) {
    return new Promise((resolve) => {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', cfg.uploadUrl);
      xhr.setRequestHeader('X-CSRF-Token', cfg.csrf);
      xhr.responseType = 'json';
      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          setState(stateEl, cfg.msgUploading + ' ' + Math.round((e.loaded / e.total) * 100) + '%');
        }
      };
      xhr.onload = () => {
        const data = xhr.response;
        if (xhr.status >= 200 && xhr.status < 300 && data && data.ok) {
          setState(stateEl, cfg.msgDone, 'ok');
          resolve(true);
        } else {
          setState(stateEl, (data && data.message) || cfg.msgNetwork, 'err');
          resolve(false);
        }
      };
      xhr.onerror = () => { setState(stateEl, cfg.msgNetwork, 'err'); resolve(false); };
      const fd = new FormData();
      fd.append('file', file, file.name);
      xhr.send(fd);
    });
  }

  const openPicker = () => input.click();
  btn.addEventListener('click', openPicker);
  document.querySelectorAll('[data-import-trigger]').forEach((trigger) => {
    trigger.addEventListener('click', () => {
      if (!btn.disabled) openPicker();
    });
  });

  async function importFiles(files) {
    if (!files.length) return;

    btn.disabled = true;
    dropZone.classList.add('is-uploading');
    dropZone.setAttribute('aria-disabled', 'true');
    box.textContent = '';
    let ok = 0, failed = 0;

    for (const file of files) {                 // one at a time: simple progress, bounded memory
      const state = addRow(file.name);
      if (!/\.pdf$/i.test(file.name)) { setState(state, cfg.msgNotPdf, 'err'); failed++; continue; }
      if (file.size > maxBytes) { setState(state, cfg.msgTooLarge, 'err'); failed++; continue; }
      setState(state, cfg.msgUploading + ' 0%');
      (await uploadOne(file, state)) ? ok++ : failed++;
    }

    btn.disabled = false;
    dropZone.classList.remove('is-uploading');
    dropZone.setAttribute('aria-disabled', 'false');
    if (failed === 0) {
      window.location.reload();
    } else if (ok > 0) {
      const b = document.createElement('button');
      b.type = 'button';
      b.className = 'btn small';
      b.textContent = cfg.msgRefresh;
      b.addEventListener('click', () => window.location.reload());
      box.appendChild(b);
    }
  }

  input.addEventListener('change', () => {
    const files = Array.from(input.files);
    input.value = '';
    importFiles(files);
  });
  ['dragenter', 'dragover'].forEach((eventName) => {
    dropZone.addEventListener(eventName, (event) => {
      event.preventDefault();
      if (!btn.disabled) dropZone.classList.add('is-dragging');
    });
  });
  ['dragleave', 'drop'].forEach((eventName) => {
    dropZone.addEventListener(eventName, (event) => {
      event.preventDefault();
      dropZone.classList.remove('is-dragging');
    });
  });
  dropZone.addEventListener('drop', (event) => {
    if (!btn.disabled) importFiles(Array.from(event.dataTransfer.files));
  });
  dropZone.addEventListener('keydown', (event) => {
    if ((event.key === 'Enter' || event.key === ' ') && !btn.disabled) {
      event.preventDefault();
      openPicker();
    }
  });
})();
