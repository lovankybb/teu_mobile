<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=true; section>
    <#if section = "header">
        ${msg("loginAccountTitle")!"WELCOME BACK"}
    <#elseif section = "info">
        <p>${msg("loginSubtitle")!"Chào mừng trở lại! Vui lòng đăng nhập để tiếp tục."}</p>
    <#elseif section = "form">
        <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <#if !usernameHidden??>
                <!-- Username / Email Field -->
                <div class="teu-form-group">
                    <label for="username" class="teu-label">
                        <#if !realm.loginWithEmailAllowed>
                            ${msg("username")!"Tên đăng nhập"}
                        <#elseif !realm.registrationEmailAsUsername>
                            ${msg("usernameOrEmail")!"Tên đăng nhập hoặc Email"}
                        <#else>
                            ${msg("email")!"Email"}
                        </#if>
                    </label>
                    <div class="teu-input-wrapper">
                        <input tabindex="1"
                               id="username"
                               class="teu-input <#if messagesPerField.existsError('username','password')>has-error</#if>"
                               name="username"
                               value="${(login.username!'')}"
                               type="text"
                               autofocus
                               autocomplete="username"
                               placeholder="user@example.com"
                               aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>" />
                    </div>
                </div>
            </#if>

            <!-- Password Field -->
            <div class="teu-form-group">
                <label for="password" class="teu-label">${msg("password")!"Mật khẩu"}</label>
                <div class="teu-input-wrapper">
                    <input tabindex="2"
                           id="password"
                           class="teu-input teu-input-with-toggle <#if messagesPerField.existsError('username','password')>has-error</#if>"
                           name="password"
                           type="password"
                           autocomplete="current-password"
                           placeholder="••••••••"
                           aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>" />
                    <button type="button"
                            id="teu-password-toggle-btn"
                            class="teu-password-toggle"
                            aria-label="Hiện/Ẩn mật khẩu"
                            tabindex="-1">
                        <!-- Eye Open Icon -->
                        <svg class="eye-open" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                        <!-- Eye Closed Icon -->
                        <svg class="eye-closed" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display: none;">
                            <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"></path>
                            <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"></path>
                            <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"></path>
                            <line x1="2" y1="2" x2="22" y2="22"></line>
                        </svg>
                    </button>
                </div>
            </div>

            <!-- Remember Me & Forgot Password Row -->
            <div class="teu-form-actions-row">
                <#if realm.rememberMe && !usernameHidden??>
                    <label class="teu-checkbox-label" for="rememberMe">
                        <input tabindex="3"
                               id="rememberMe"
                               name="rememberMe"
                               type="checkbox"
                               class="teu-checkbox"
                               <#if login.rememberMe??>checked</#if>>
                        ${msg("rememberMe")!"Ghi nhớ đăng nhập"}
                    </label>
                <#else>
                    <div></div>
                </#if>

                <#if realm.resetPasswordAllowed>
                    <a tabindex="5" href="${url.loginResetCredentialsUrl}" class="teu-link">
                        ${msg("doForgotPassword")!"Quên mật khẩu?"}
                    </a>
                </#if>
            </div>

            <!-- Submit Button -->
            <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
            <button tabindex="4" class="teu-btn-primary" name="login" id="kc-login" type="submit">
                <span>${msg("doLogIn")!"Đăng nhập"}</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                    <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
            </button>

            <!-- Registration Button & Link -->
            <#if realm.registrationAllowed && url.registrationUrl??>
                <#assign registerTargetUrl = url.registrationUrl>
            <#elseif client?? && client.baseUrl?has_content>
                <#assign registerTargetUrl = client.baseUrl + "/register">
            <#else>
                <#assign registerTargetUrl = "http://localhost:5173/register">
            </#if>
            <div style="margin-top: 12px;">
                <a tabindex="6" href="${registerTargetUrl}" class="teu-btn-register">
                    <span>${msg("doRegister")!"Tạo tài khoản mới"}</span>
                </a>
            </div>
        </form>
    <#elseif section = "socialProviders">
        <#if realm.password && social.providers?? && social.providers?has_content>
            <div class="teu-divider">
                <span>${msg("orDividerText")!"HOẶC"}</span>
            </div>

            <div class="teu-social-providers">
                <#list social.providers as p>
                    <a id="social-${p.alias}" class="teu-btn-social" href="${p.loginUrl}">
                        <#if p.alias == "google">
                            <svg viewBox="0 0 24 24" aria-hidden="true">
                                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" />
                                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" />
                            </svg>
                            <span>Tiếp tục với Google</span>
                        <#else>
                            <#if p.iconClasses?has_content>
                                <i class="${p.iconClasses}" aria-hidden="true"></i>
                            </#if>
                            <span>${p.displayName}</span>
                        </#if>
                    </a>
                </#list>
            </div>
        </#if>
    </#if>
</@layout.registrationLayout>