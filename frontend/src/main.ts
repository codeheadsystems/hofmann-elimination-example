import { initAuthView } from './auth.js';
import { initNotesView } from './notes.js';

const authView  = document.getElementById('auth-view')!;
const notesView = document.getElementById('notes-view')!;

function showNotes(token: string, username: string): void {
  authView.classList.add('hidden');
  notesView.classList.remove('hidden');
  initNotesView(token, username, showAuth);
}

function showAuth(): void {
  notesView.classList.add('hidden');
  authView.classList.remove('hidden');
}

initAuthView(showNotes);
