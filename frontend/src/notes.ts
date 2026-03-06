interface NoteResponse {
  id:        string;
  title:     string;
  content:   string;
  createdAt: string;
}

export function initNotesView(token: string, username: string, onLogout: () => void): void {
  const loggedInUser = document.getElementById('logged-in-user')!;
  const logoutBtn    = document.getElementById('logout-btn')!    as HTMLButtonElement;
  const createForm   = document.getElementById('create-form')!   as HTMLFormElement;
  const notesList    = document.getElementById('notes-list')!;
  const emptyState   = document.getElementById('empty-state')!;
  const createError  = document.getElementById('create-error')!;

  loggedInUser.textContent = username;

  logoutBtn.addEventListener('click', () => {
    notesList.innerHTML = '';
    onLogout();
  });

  createForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    createError.classList.add('hidden');

    const titleInput   = document.getElementById('note-title')   as HTMLInputElement;
    const contentInput = document.getElementById('note-content') as HTMLTextAreaElement;
    const title   = titleInput.value.trim();
    const content = contentInput.value.trim();

    const submitBtn = createForm.querySelector('button[type="submit"]') as HTMLButtonElement;
    submitBtn.disabled = true;

    try {
      const note = await apiPost<NoteResponse>('/api/notes', { title, content });
      titleInput.value   = '';
      contentInput.value = '';
      prependNote(note);
      emptyState.classList.add('hidden');
    } catch (err) {
      createError.textContent = err instanceof Error ? err.message : String(err);
      createError.classList.remove('hidden');
    } finally {
      submitBtn.disabled = false;
    }
  });

  // ── API helpers ──────────────────────────────────────────────────────────

  async function apiGet<T>(path: string): Promise<T> {
    const r = await fetch(path, {
      headers: { 'Authorization': `Bearer ${token}` },
    });
    if (!r.ok) throw new Error(`${r.status} ${r.statusText}`);
    return r.json() as Promise<T>;
  }

  async function apiPost<T>(path: string, body: unknown): Promise<T> {
    const r = await fetch(path, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(body),
    });
    if (!r.ok) throw new Error(`${r.status} ${r.statusText}`);
    return r.json() as Promise<T>;
  }

  async function apiDelete(path: string): Promise<void> {
    const r = await fetch(path, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` },
    });
    if (!r.ok) throw new Error(`${r.status} ${r.statusText}`);
  }

  // ── Rendering ────────────────────────────────────────────────────────────

  function prependNote(note: NoteResponse): void {
    notesList.prepend(renderCard(note));
  }

  function renderCard(note: NoteResponse): HTMLElement {
    const date = new Date(note.createdAt).toLocaleDateString(undefined, {
      year: 'numeric', month: 'short', day: 'numeric',
    });

    const card = document.createElement('div');
    card.className = 'note-card';
    card.dataset['id'] = note.id;
    card.innerHTML = `
      <div class="note-header">
        <span class="note-title">${esc(note.title)}</span>
        <button class="delete-btn" title="Delete note">✕</button>
      </div>
      <div class="note-body">${esc(note.content)}</div>
      <div class="note-date">${date}</div>
    `;

    card.querySelector<HTMLButtonElement>('.delete-btn')!.addEventListener('click', async () => {
      card.classList.add('deleting');
      try {
        await apiDelete(`/api/notes/${note.id}`);
        card.remove();
        if (notesList.children.length === 0) {
          emptyState.classList.remove('hidden');
        }
      } catch (err) {
        card.classList.remove('deleting');
        alert(err instanceof Error ? err.message : String(err));
      }
    });

    return card;
  }

  function esc(s: string): string {
    return s
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  // ── Initial load ─────────────────────────────────────────────────────────

  apiGet<NoteResponse[]>('/api/notes').then(notes => {
    notesList.innerHTML = '';
    if (notes.length === 0) {
      emptyState.classList.remove('hidden');
      return;
    }
    emptyState.classList.add('hidden');
    notes.forEach(note => notesList.appendChild(renderCard(note)));
  }).catch(err => {
    console.error('Failed to load notes:', err);
  });
}
