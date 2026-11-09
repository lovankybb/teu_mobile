/**
 * keycloak.js
 * Service gọi trực tiếp Keycloak Token Endpoint từ frontend (Resource Owner Password Credentials Grant).
 * Không còn phải đi qua backend /api/auth/login để proxy tới Keycloak nữa.
 *
 * Các endpoint Keycloak dùng:
 *  - POST /realms/{realm}/protocol/openid-connect/token  → lấy / refresh token
 *  - POST /realms/{realm}/protocol/openid-connect/logout → đăng xuất (revoke refresh token)
 *  - GET  /realms/{realm}/protocol/openid-connect/userinfo → lấy claims từ access token
 */

const KEYCLOAK_URL    = import.meta.env.VITE_KEYCLOAK_URL    || 'http://localhost:8180';
const KEYCLOAK_REALM  = import.meta.env.VITE_KEYCLOAK_REALM  || 'teu-mobile-realm';
const KEYCLOAK_CLIENT = import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'teu-mobile-client';

/** Base URL của token endpoint */
const TOKEN_ENDPOINT  = `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`;
const LOGOUT_ENDPOINT = `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/logout`;
const USERINFO_ENDPOINT = `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/userinfo`;

/**
 * Tạo URLSearchParams body cho Keycloak (application/x-www-form-urlencoded)
 */
function buildFormBody(params) {
  return new URLSearchParams({ client_id: KEYCLOAK_CLIENT, ...params });
}

/**
 * Xử lý response từ Keycloak – parse JSON và ném lỗi có message rõ ràng nếu thất bại.
 */
async function handleResponse(response) {
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    // Keycloak thường trả về { error, error_description }
    const msg = data?.error_description || data?.error || `HTTP ${response.status}`;
    throw new Error(msg);
  }
  return data;
}

// ─────────────────────────────────────────────
// 1. ĐĂNG NHẬP – Resource Owner Password Credentials
// ─────────────────────────────────────────────
/**
 * @param {string} usernameOrEmail
 * @param {string} password
 * @returns {Promise<{access_token, refresh_token, expires_in, refresh_expires_in, token_type}>}
 */
export async function keycloakLogin(usernameOrEmail, password) {
  const response = await fetch(TOKEN_ENDPOINT, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: buildFormBody({
      grant_type: 'password',
      username:   usernameOrEmail,
      password,
      scope:      'openid profile email',
    }),
  });
  return handleResponse(response);
}

// ─────────────────────────────────────────────
// 2. LÀM MỚI TOKEN – Refresh Token Grant
// ─────────────────────────────────────────────
/**
 * @param {string} refreshToken
 * @returns {Promise<{access_token, refresh_token, expires_in, ...}>}
 */
export async function keycloakRefreshToken(refreshToken) {
  const response = await fetch(TOKEN_ENDPOINT, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: buildFormBody({
      grant_type:    'refresh_token',
      refresh_token: refreshToken,
    }),
  });
  return handleResponse(response);
}

// ─────────────────────────────────────────────
// 3. ĐĂNG XUẤT – Revoke Refresh Token
// ─────────────────────────────────────────────
/**
 * @param {string} refreshToken
 */
export async function keycloakLogout(refreshToken) {
  if (!refreshToken) return;
  await fetch(LOGOUT_ENDPOINT, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: buildFormBody({ refresh_token: refreshToken }),
  }).catch((err) => console.warn('[Keycloak] Lỗi đăng xuất:', err));
}

// ─────────────────────────────────────────────
// 4. LẤY THÔNG TIN USER TỪ USERINFO ENDPOINT
// ─────────────────────────────────────────────
/**
 * @param {string} accessToken
 * @returns {Promise<{sub, preferred_username, email, name, ...}>}
 */
export async function keycloakGetUserInfo(accessToken) {
  const response = await fetch(USERINFO_ENDPOINT, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  return handleResponse(response);
}
