/**
 * auth.js — Login and Registration logic for TrackMyWallet
 *
 * Uses the Fetch API to call the Spring Boot REST endpoints:
 *   POST /api/auth/login
 *   POST /api/auth/register
 *   POST /api/auth/logout
 */

// ============================================================
//  On page load: redirect to dashboard if already logged in
// ============================================================
window.addEventListener('DOMContentLoaded', async () => {
  try {
    const res = await fetch('/api/auth/me', { credentials: 'include' });
    if (res.ok) {
      const data = await res.json();
      if (data.success) {
        window.location.href = '/dashboard.html';  // Already logged in → go to dashboard
      }
    }
  } catch (_) {
    // Not logged in — stay on this page
  }
});

// ============================================================
//  Tab Switcher
// ============================================================
function switchTab(tab) {
  const loginForm    = document.getElementById('loginForm');
  const registerForm = document.getElementById('registerForm');
  const tabLogin     = document.getElementById('tabLogin');
  const tabRegister  = document.getElementById('tabRegister');

  hideAlert();

  if (tab === 'login') {
    loginForm.classList.remove('hidden');
    registerForm.classList.add('hidden');
    tabLogin.classList.add('active');
    tabRegister.classList.remove('active');
  } else {
    loginForm.classList.add('hidden');
    registerForm.classList.remove('hidden');
    tabLogin.classList.remove('active');
    tabRegister.classList.add('active');
  }
}

// ============================================================
//  LOGIN
// ============================================================
async function handleLogin(event) {
  event.preventDefault();
  const btn      = document.getElementById('loginBtn');
  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value;

  if (!username || !password) {
    showAlert('Please fill in all fields.', 'error');
    return;
  }

  setLoading(btn, true);
  hideAlert();

  try {
    const response = await fetch('/api/auth/login', {
      method:      'POST',
      headers:     { 'Content-Type': 'application/json' },
      credentials: 'include',                          // Send session cookie
      body:        JSON.stringify({ username, password })
    });

    const data = await response.json();

    if (data.success) {
      showAlert('✅ ' + data.message, 'success');
      setTimeout(() => { window.location.href = '/dashboard.html'; }, 800);
    } else {
      showAlert('❌ ' + data.message, 'error');
      setLoading(btn, false);
    }
  } catch (err) {
    showAlert('❌ Could not connect to the server. Please try again.', 'error');
    setLoading(btn, false);
  }
}

// ============================================================
//  REGISTER
// ============================================================
async function handleRegister(event) {
  event.preventDefault();
  const btn      = document.getElementById('registerBtn');
  const fullName = document.getElementById('regFullName').value.trim();
  const username = document.getElementById('regUsername').value.trim();
  const email    = document.getElementById('regEmail').value.trim();
  const password = document.getElementById('regPassword').value;

  if (!fullName || !username || !email || !password) {
    showAlert('Please fill in all fields.', 'error');
    return;
  }
  if (password.length < 6) {
    showAlert('Password must be at least 6 characters.', 'error');
    return;
  }

  setLoading(btn, true);
  hideAlert();

  try {
    const response = await fetch('/api/auth/register', {
      method:      'POST',
      headers:     { 'Content-Type': 'application/json' },
      credentials: 'include',
      body:        JSON.stringify({ fullName, username, email, password })
    });

    const data = await response.json();

    if (data.success) {
      showAlert('🎉 ' + data.message, 'success');
      setTimeout(() => { window.location.href = '/dashboard.html'; }, 1000);
    } else {
      showAlert('❌ ' + data.message, 'error');
      setLoading(btn, false);
    }
  } catch (err) {
    showAlert('❌ Could not connect to the server. Please try again.', 'error');
    setLoading(btn, false);
  }
}

// ============================================================
//  HELPERS
// ============================================================

/** Show or hide the loading spinner on a button. */
function setLoading(btn, isLoading) {
  const text    = btn.querySelector('.btn-text');
  const spinner = btn.querySelector('.btn-spinner');
  btn.disabled  = isLoading;
  text.style.display    = isLoading ? 'none'   : 'inline';
  spinner.style.display = isLoading ? 'inline' : 'none';
}

/** Show an alert message. type = 'success' | 'error' */
function showAlert(message, type) {
  const box = document.getElementById('alertBox');
  box.textContent   = message;
  box.className     = 'alert ' + type;
  box.style.display = 'block';
}

/** Hide the alert banner. */
function hideAlert() {
  const box = document.getElementById('alertBox');
  box.style.display = 'none';
  box.className     = 'alert';
}

/** Toggle password field visibility. */
function togglePassword(fieldId, btn) {
  const input = document.getElementById(fieldId);
  if (input.type === 'password') {
    input.type  = 'text';
    btn.textContent = '🙈';
  } else {
    input.type  = 'password';
    btn.textContent = '👁️';
  }
}
