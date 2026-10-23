(() => {
  const root = document.getElementById('importWorkspace');
  if (!root) return;
  const cfg = root.dataset;
  const maxBytes = parseInt(cfg.maxBytes, 10);
  const button = document.getElementById('importBtn');
  const input = document.getElementById('fileInput');
  const dropZone = document.getElementById('dropZone');
  const status = document.getElementById('uploadStatus');

  function addRow(name) {
    const row = document.createElement('div');
    row.className = 'upload-item';
    const fileName = document.createElement('span');
    fileName.className = 'name';
    fileName.textContent = name;
    const state = document.createElement('span');
    state.className = 'state';
    row.append(fileName, state);
    status.appendChild(row);
    return state;
  }
  function setState(element, text, kind) {
    element.textContent = text;
    element.className = 'state' + (kind ? ' ' + kind : '');
  }
  function uploadOne(file, state) {
    return new Promise((resolve) => {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', cfg.uploadUrl);
      xhr.setRequestHeader('X-CSRF-Token', cfg.csrf);
      xhr.responseType = 'json';
      xhr.upload.onprogress = (event) => {
        if (event.lengthComputable) setState(state, cfg.msgUploading + ' ' + Math.round((event.loaded / event.total) * 100) + '%');
      };
      xhr.onload = () => {
        const data = xhr.response;
        if (xhr.status >= 200 && xhr.status < 300 && data && data.ok) { setState(state, cfg.msgDone, 'ok'); resolve(true); }
        else { setState(state, (data && data.message) || cfg.msgNetwork, 'err'); resolve(false); }
      };
      xhr.onerror = () => { setState(state, cfg.msgNetwork, 'err'); resolve(false); };
      const form = new FormData();
      form.append('file', file, file.name);
      xhr.send(form);
    });
  }
  async function importFiles(files) {
    if (!files.length || button.disabled) return;
    button.disabled = true;
    dropZone.classList.add('is-uploading');
    dropZone.setAttribute('aria-disabled', 'true');
    status.textContent = '';
    let failed = 0;
    for (const file of files) {
      const state = addRow(file.name);
      if (!/\.pdf$/i.test(file.name)) { setState(state, cfg.msgNotPdf, 'err'); failed++; continue; }
      if (file.size > maxBytes) { setState(state, cfg.msgTooLarge, 'err'); failed++; continue; }
      setState(state, cfg.msgUploading + ' 0%');
      if (!await uploadOne(file, state)) failed++;
    }
    button.disabled = false;
    dropZone.classList.remove('is-uploading');
    dropZone.setAttribute('aria-disabled', 'false');
    if (!failed) window.location.assign('home');
  }
  const openPicker = () => { if (!button.disabled) input.click(); };
  button.addEventListener('click', openPicker);
  input.addEventListener('change', () => { const files = Array.from(input.files); input.value = ''; importFiles(files); });
  ['dragenter', 'dragover'].forEach((eventName) => dropZone.addEventListener(eventName, (event) => { event.preventDefault(); if (!button.disabled) dropZone.classList.add('is-dragging'); }));
  ['dragleave', 'drop'].forEach((eventName) => dropZone.addEventListener(eventName, (event) => { event.preventDefault(); dropZone.classList.remove('is-dragging'); }));
  dropZone.addEventListener('drop', (event) => importFiles(Array.from(event.dataTransfer.files)));
  dropZone.addEventListener('keydown', (event) => { if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); openPicker(); } });
})();
