<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true displayMessage=!messagesPerField.existsError('username'); section>
    <#if section = "header">
        ${msg("emailResetTitle")!"QUÊN MẬT KHẨU"}
    <#elseif section = "info">
        <p>${msg("emailInstruction")!"Nhập email hoặc tên tài khoản của bạn để nhận liên kết đặt lại mật khẩu."}</p>
    <#elseif section = "form">
        <form id="kc-reset-password-form" action="${url.loginAction}" method="post">
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
                    <input type="text"
                           id="username"
                           name="username"
                           class="teu-input <#if messagesPerField.existsError('username')>has-error</#if>"
                           autofocus
                           value="${(auth.attemptedUsername!'')}"
                           placeholder="user@example.com"
                           aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"/>
                </div>
            </div>

            <div style="display: flex; flex-direction: column; gap: 12px; margin-top: 24px;">
                <button class="teu-btn-primary" type="submit">
                    <span>${msg("doSubmit")!"Gửi liên kết khôi phục"}</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                        <polyline points="12 5 19 12 12 19"></polyline>
                    </svg>
                </button>

                <a href="${url.loginUrl}" class="teu-btn-social" style="text-align: center;">
                    ${msg("backToLogin")!"Quay lại đăng nhập"}
                </a>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>