/**
 * TEU Mobile - Keycloak Login Script
 * Handles password visibility toggling, accessibility, and form UX.
 */

document.addEventListener('DOMContentLoaded', function () {
  // ─── 1. Password Visibility Toggle (for all password fields) ─────────────
  const toggleBtns = document.querySelectorAll('.teu-password-toggle');
  toggleBtns.forEach(function (btn) {
    btn.addEventListener('click', function (e) {
      e.preventDefault();
      const wrapper = btn.closest('.teu-input-wrapper');
      if (!wrapper) return;
      const input = wrapper.querySelector('input');
      if (!input) return;

      const isPassword = input.getAttribute('type') === 'password';
      input.setAttribute('type', isPassword ? 'text' : 'password');

      const eyeOpen = btn.querySelector('.eye-open');
      const eyeClosed = btn.querySelector('.eye-closed');
      if (eyeOpen && eyeClosed) {
        if (isPassword) {
          eyeOpen.style.display = 'none';
          eyeClosed.style.display = 'block';
        } else {
          eyeOpen.style.display = 'block';
          eyeClosed.style.display = 'none';
        }
      }
      input.focus();
    });
  });

  // ─── 2. Auto-focus First Empty Input ────────────────────────────────────
  const usernameInput = document.getElementById('username');
  const passwordInput = document.getElementById('password');
  if (usernameInput && !usernameInput.value) {
    usernameInput.focus();
  } else if (passwordInput && !passwordInput.value) {
    passwordInput.focus();
  }

  // ─── 3. Safe Form Submit Loading Feedback ────────────────────────────────
  const loginForm = document.getElementById('kc-form-login') || document.querySelector('form');
  const submitBtn = document.getElementById('kc-login') || (loginForm ? loginForm.querySelector('button[type="submit"]') : null);

  if (loginForm && submitBtn) {
    loginForm.addEventListener('submit', function () {
      if (typeof loginForm.checkValidity === 'function' && !loginForm.checkValidity()) {
        return; // Let browser HTML5 validation trigger
      }
      // Apply subtle loading effect without disabling button immediately to prevent dropped submit values
      setTimeout(function () {
        submitBtn.style.opacity = '0.8';
        submitBtn.style.pointerEvents = 'none';
      }, 50);
    });
  }
});
