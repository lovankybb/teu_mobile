/**
 * keycloak.js
 * Khởi tạo Keycloak instance dùng Authorization Code + PKCE flow.
 * Frontend không bao giờ nhận username/password — Keycloak tự xử lý trang login.
 */
import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url:      import.meta.env.VITE_KEYCLOAK_URL    || 'http://localhost:8180',
  realm:    import.meta.env.VITE_KEYCLOAK_REALM  || 'teu-mobile-realm',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'teu-mobile-fe-client',
});

export default keycloak;
