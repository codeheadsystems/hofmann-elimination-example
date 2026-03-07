import { OpaqueHttpClient } from '@codeheadsystems/hofmann-typescript';

type OnLoginSuccess = (token: string, username: string) => void;

export function initAuthView(onLoginSuccess: OnLoginSuccess): void {
  const tabLogin    = document.getElementById('tab-login')!    as HTMLButtonElement;
  const tabRegister = document.getElementById('tab-register')! as HTMLButtonElement;
  const form        = document.getElementById('auth-form')!    as HTMLFormElement;
  const submitBtn   = document.getElementById('auth-submit')!  as HTMLButtonElement;
  const messageEl   = document.getElementById('auth-message')!;

  let mode: 'login' | 'register' = 'login';

  tabLogin.addEventListener('click', () => {
    mode = 'login';
    tabLogin.classList.add('active');
    tabRegister.classList.remove('active');
    submitBtn.textContent = 'Log In';
    clearMessage();
  });

  tabRegister.addEventListener('click', () => {
    mode = 'register';
    tabRegister.classList.add('active');
    tabLogin.classList.remove('active');
    submitBtn.textContent = 'Register';
    clearMessage();
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearMessage();

    const username = (document.getElementById('username') as HTMLInputElement).value.trim();
    const password = (document.getElementById('password') as HTMLInputElement).value;

    submitBtn.disabled = true;
    submitBtn.textContent = mode === 'login' ? 'Logging in...' : 'Registering...';

    try {
      // OpaqueHttpClient.create() fetches /api/opaque/config and configures
      // the cipher suite and KSF parameters automatically.
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

  function showMessage(msg: string, type: 'error' | 'success'): void {
    messageEl.textContent = msg;
    messageEl.className = `message ${type}`;
  }

  function clearMessage(): void {
    messageEl.textContent = '';
    messageEl.className = '';
  }
}
