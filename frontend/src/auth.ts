import { OpaqueHttpClient } from '@codeheadsystems/hofmann-typescript';

type OnLoginSuccess = (token: string, username: string) => void;

export function initAuthView(onLoginSuccess: OnLoginSuccess): void {
  const tabLogin    = document.getElementById('tab-login')!    as HTMLButtonElement;
  const tabRegister = document.getElementById('tab-register')! as HTMLButtonElement;
  const tabRecover  = document.getElementById('tab-recover')!  as HTMLButtonElement;
  const form        = document.getElementById('auth-form')!    as HTMLFormElement;
  const recoveryForm = document.getElementById('recovery-form')! as HTMLFormElement;
  const submitBtn   = document.getElementById('auth-submit')!  as HTMLButtonElement;
  const messageEl   = document.getElementById('auth-message')!;

  let mode: 'login' | 'register' = 'login';

  function setActiveTab(tab: HTMLButtonElement) {
    tabLogin.classList.remove('active');
    tabRegister.classList.remove('active');
    tabRecover.classList.remove('active');
    tab.classList.add('active');
    clearMessage();
  }

  tabLogin.addEventListener('click', () => {
    mode = 'login';
    setActiveTab(tabLogin);
    form.classList.remove('hidden');
    recoveryForm.classList.add('hidden');
    submitBtn.textContent = 'Log In';
  });

  tabRegister.addEventListener('click', () => {
    mode = 'register';
    setActiveTab(tabRegister);
    form.classList.remove('hidden');
    recoveryForm.classList.add('hidden');
    submitBtn.textContent = 'Register';
  });

  tabRecover.addEventListener('click', () => {
    setActiveTab(tabRecover);
    form.classList.add('hidden');
    recoveryForm.classList.remove('hidden');
    resetRecoveryForm();
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearMessage();

    const username = (document.getElementById('username') as HTMLInputElement).value.trim();
    const password = (document.getElementById('password') as HTMLInputElement).value;

    submitBtn.disabled = true;
    submitBtn.textContent = mode === 'login' ? 'Logging in...' : 'Registering...';

    try {
      const client = await OpaqueHttpClient.create('/api');

      if (mode === 'register') {
        await client.register(username, password);
        showMessage('Registration successful! Please log in.', 'success');
        tabLogin.click();
      } else {
        const token = await client.authenticate(username, password);
        onLoginSuccess(token, username);
      }
    } catch (err: unknown) {
      showMessage(err instanceof Error ? err.message : String(err), 'error');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = mode === 'login' ? 'Log In' : 'Register';
    }
  });

  // ── Recovery flow ──────────────────────────────────────────────────────────

  let recoveryStep: 'start' | 'verify' = 'start';

  function resetRecoveryForm() {
    recoveryStep = 'start';
    const codeField = document.getElementById('recovery-code-field')!;
    const pwField = document.getElementById('recovery-password-field')!;
    const submitBtn = document.getElementById('recovery-submit')! as HTMLButtonElement;
    codeField.classList.add('hidden');
    pwField.classList.add('hidden');
    submitBtn.textContent = 'Send Recovery Code';
    (document.getElementById('recovery-username') as HTMLInputElement).value = '';
    (document.getElementById('recovery-code') as HTMLInputElement).value = '';
    (document.getElementById('recovery-password') as HTMLInputElement).value = '';
  }

  recoveryForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearMessage();

    const username = (document.getElementById('recovery-username') as HTMLInputElement).value.trim();
    const recoverSubmitBtn = document.getElementById('recovery-submit')! as HTMLButtonElement;
    recoverSubmitBtn.disabled = true;

    try {
      const client = await OpaqueHttpClient.create('/api');

      if (recoveryStep === 'start') {
        await client.recoveryStart(username);
        showMessage('Recovery code sent! Check the server console log for the code.', 'success');
        recoveryStep = 'verify';
        document.getElementById('recovery-code-field')!.classList.remove('hidden');
        document.getElementById('recovery-password-field')!.classList.remove('hidden');
        recoverSubmitBtn.textContent = 'Verify & Set New Password';
        (document.getElementById('recovery-username') as HTMLInputElement).readOnly = true;
      } else {
        const code = (document.getElementById('recovery-code') as HTMLInputElement).value.trim();
        const newPassword = (document.getElementById('recovery-password') as HTMLInputElement).value;
        await client.recoverAndReRegister(username, code, newPassword);
        showMessage('Account recovered! You can now log in with your new password.', 'success');
        resetRecoveryForm();
        tabLogin.click();
      }
    } catch (err: unknown) {
      showMessage(err instanceof Error ? err.message : String(err), 'error');
    } finally {
      recoverSubmitBtn.disabled = false;
    }
  });

  function showMessage(msg: string, type: 'error' | 'success'): void {
    messageEl.textContent = msg;
    messageEl.className = `message ${type}`;
  }

  function clearMessage(): void {
    messageEl.textContent = '';
    messageEl.className = '';
  }
}
