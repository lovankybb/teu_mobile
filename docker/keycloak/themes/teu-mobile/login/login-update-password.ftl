<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('password','password-confirm'); section>
    <#if section = "header">
        ${msg("updatePasswordTitle")!"CẬP NHẬT MẬT KHẨU"}
    <#elseif section = "info">
        <p>${msg("updatePasswordSubtitle")!"Vui lòng thiết lập mật khẩu mới cho tài khoản của bạn."}</p>
    <#elseif section = "form">
        <form id="kc-passwd-update-form" action="${url.loginAction}" method="post">
            <#if messagesPerField.existsError('password','password-confirm')>
                <div class="teu-alert teu-alert-error" role="alert">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="12" y1="8" x2="12" y2="12"></line>
                        <line x1="12" y1="16" x2="12.01" y2="16"></line>
                    </svg>
                    <div>${kcSanitize(messagesPerField.getFirstError('password','password-confirm'))?no_esc}</div>
                </div>
            </#if>
            <input type="text" id="username" name="username" value="${username}" autocomplete="username" readonly="readonly" style="display:none;"/>
            <input type="password" id="password" name="password" autocomplete="current-password" style="display:none;"/>

            <div class="teu-form-group">
                <label for="password-new" class="teu-label">${msg("passwordNew")!"Mật khẩu mới"}</label>
                <div class="teu-input-wrapper">
                    <input type="password"
                           id="password-new"
                           name="password-new"
                           class="teu-input teu-input-with-toggle <#if messagesPerField.existsError('password')>has-error</#if>"
                           autofocus
                           autocomplete="new-password"
                           placeholder="••••••••••"
                           aria-invalid="<#if messagesPerField.existsError('password')>true</#if>"/>
                    <button type="button" class="teu-password-toggle" aria-label="Hiện/Ẩn mật khẩu" tabindex="-1">
                        <svg class="eye-open" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                        <svg class="eye-closed" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display: none;">
                            <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"></path>
                            <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"></path>
                            <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"></path>
                            <line x1="2" y1="2" x2="22" y2="22"></line>
                        </svg>
                    </button>
                </div>
            </div>

            <div class="teu-form-group">
                <label for="password-confirm" class="teu-label">${msg("passwordConfirm")!"Xác nhận mật khẩu mới"}</label>
                <div class="teu-input-wrapper">
                    <input type="password"
                           id="password-confirm"
                           name="password-confirm"
                           class="teu-input teu-input-with-toggle <#if messagesPerField.existsError('password-confirm')>has-error</#if>"
                           autocomplete="new-password"
                           placeholder="••••••••••"
                           aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>"/>
                    <button type="button" class="teu-password-toggle" aria-label="Hiện/Ẩn mật khẩu" tabindex="-1">
                        <svg class="eye-open" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                        <svg class="eye-closed" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display: none;">
                            <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"></path>
                            <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"></path>
                            <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"></path>
                            <line x1="2" y1="2" x2="22" y2="22"></line>
                        </svg>
                    </button>
                </div>
            </div>

            <div style="margin-top: 24px;">
                <button class="teu-btn-primary" type="submit">
                    <span>${msg("doSubmit")!"Lưu mật khẩu"}</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                        <polyline points="12 5 19 12 12 19"></polyline>
                    </svg>
                </button>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>