/**
 * TEU Mobile - Keycloak Login Script
 * Handles password visibility toggling and interactive UX.
 */

document.addEventListener('DOMContentLoaded', function () {
  // ─── 1. Password Visibility Toggle ──────────────────────────────────────
  const toggleBtn = document.getElementById('teu-password-toggle-btn');
  const passwordInput = document.getElementById('password');

  if (toggleBtn && passwordInput) {
    toggleBtn.addEventListener('click', function (e) {
      e.preventDefault();
      const isPassword = passwordInput.getAttribute('type') === 'password';
      passwordInput.setAttribute('type', isPassword ? 'text' : 'password');

      // Update toggle icon (Eye vs Eye-Off)
      const eyeIcon = toggleBtn.querySelector('.eye-open');
      const eyeOffIcon = toggleBtn.querySelector('.eye-closed');

      if (eyeIcon && eyeOffIcon) {
        if (isPassword) {
          eyeIcon.style.display = 'none';
          eyeOffIcon.style.display = 'block';
        } else {
          eyeIcon.style.display = 'block';
          eyeOffIcon.style.display = 'none';
        }
      }
    });
  }

  // ─── 2. Auto-focus First Empty Input ────────────────────────────────────
  const usernameInput = document.getElementById('username');
  if (usernameInput && !usernameInput.value) {
    usernameInput.focus();
  } else if (passwordInput) {
    passwordInput.focus();
  }

  // ─── 3. Double-submit Prevention ─────────────────────────────────────────
  const loginForm = document.getElementById('kc-form-login');
  const submitBtn = document.getElementById('kc-login');

  if (loginForm && submitBtn) {
    loginForm.addEventListener('submit', function () {
      submitBtn.disabled = true;
      submitBtn.style.opacity = '0.75';
      submitBtn.style.cursor = 'not-allowed';
    });
  }
});
