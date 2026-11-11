<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('password','password-confirm'); section>
    <#if section = "header">
        ${msg("updatePasswordTitle")!"CẬP NHẬT MẬT KHẨU"}
    <#elseif section = "info">
        <p>${msg("updatePasswordSubtitle")!"Vui lòng thiết lập mật khẩu mới cho tài khoản của bạn."}</p>
    <#elseif section = "form">
        <form id="kc-passwd-update-form" action="${url.loginAction}" method="post">
            <input type="text" id="username" name="username" value="${username}" autocomplete="username" readonly="readonly" style="display:none;"/>
            <input type="password" id="password" name="password" autocomplete="current-password" style="display:none;"/>

            <div class="teu-form-group">
                <label for="password-new" class="teu-label">${msg("passwordNew")!"Mật khẩu mới"}</label>
                <div class="teu-input-wrapper">
                    <input type="password"
                           id="password-new"
                           name="password-new"
                           class="teu-input <#if messagesPerField.existsError('password')>has-error</#if>"
                           autofocus
                           autocomplete="new-password"
                           placeholder="••••••••"
                           aria-invalid="<#if messagesPerField.existsError('password')>true</#if>"/>
                </div>
            </div>

            <div class="teu-form-group">
                <label for="password-confirm" class="teu-label">${msg("passwordConfirm")!"Xác nhận mật khẩu mới"}</label>
                <div class="teu-input-wrapper">
                    <input type="password"
                           id="password-confirm"
                           name="password-confirm"
                           class="teu-input <#if messagesPerField.existsError('password-confirm')>has-error</#if>"
                           autocomplete="new-password"
                           placeholder="••••••••"
                           aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>"/>
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