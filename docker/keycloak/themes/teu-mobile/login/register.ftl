<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('firstName','lastName','email','username','password','password-confirm'); section>
    <#if section = "header">
        ${msg("registerTitle")!"TẠO TÀI KHOẢN"}
    <#elseif section = "info">
        <p>${msg("registerSubtitle")!"Tham gia cộng đồng TEU Mobile để nhận ngàn ưu đãi."}</p>
    <#elseif section = "form">
        <form id="kc-register-form" action="${url.registrationAction}" method="post">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                <div class="teu-form-group">
                    <label for="firstName" class="teu-label">${msg("firstName")!"Họ & đệm"}</label>
                    <div class="teu-input-wrapper">
                        <input type="text"
                               id="firstName"
                               class="teu-input <#if messagesPerField.existsError('firstName')>has-error</#if>"
                               name="firstName"
                               value="${(register.formData.firstName!'')}"
                               placeholder="Nguyễn Văn" />
                    </div>
                </div>

                <div class="teu-form-group">
                    <label for="lastName" class="teu-label">${msg("lastName")!"Tên"}</label>
                    <div class="teu-input-wrapper">
                        <input type="text"
                               id="lastName"
                               class="teu-input <#if messagesPerField.existsError('lastName')>has-error</#if>"
                               name="lastName"
                               value="${(register.formData.lastName!'')}"
                               placeholder="An" />
                    </div>
                </div>
            </div>

            <div class="teu-form-group">
                <label for="email" class="teu-label">${msg("email")!"Email"}</label>
                <div class="teu-input-wrapper">
                    <input type="text"
                           id="email"
                           class="teu-input <#if messagesPerField.existsError('email')>has-error</#if>"
                           name="email"
                           value="${(register.formData.email!'')}"
                           autocomplete="email"
                           placeholder="an.nguyen@example.com" />
                </div>
            </div>

            <#if !realm.registrationEmailAsUsername>
                <div class="teu-form-group">
                    <label for="username" class="teu-label">${msg("username")!"Tên đăng nhập"}</label>
                    <div class="teu-input-wrapper">
                        <input type="text"
                               id="username"
                               class="teu-input <#if messagesPerField.existsError('username')>has-error</#if>"
                               name="username"
                               value="${(register.formData.username!'')}"
                               autocomplete="username"
                               placeholder="annguyen2026" />
                    </div>
                </div>
            </#if>

            <#if passwordRequired??>
                <div class="teu-form-group">
                    <label for="password" class="teu-label">${msg("password")!"Mật khẩu"}</label>
                    <div class="teu-input-wrapper">
                        <input type="password"
                               id="password"
                               class="teu-input <#if messagesPerField.existsError('password')>has-error</#if>"
                               name="password"
                               autocomplete="new-password"
                               placeholder="••••••••" />
                    </div>
                </div>

                <div class="teu-form-group">
                    <label for="password-confirm" class="teu-label">${msg("passwordConfirm")!"Xác nhận mật khẩu"}</label>
                    <div class="teu-input-wrapper">
                        <input type="password"
                               id="password-confirm"
                               class="teu-input <#if messagesPerField.existsError('password-confirm')>has-error</#if>"
                               name="password-confirm"
                               autocomplete="new-password"
                               placeholder="••••••••" />
                    </div>
                </div>
            </#if>

            <div style="margin-top: 24px;">
                <button class="teu-btn-primary" type="submit">
                    <span>${msg("doRegister")!"Tạo tài khoản"}</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                        <polyline points="12 5 19 12 12 19"></polyline>
                    </svg>
                </button>
            </div>

            <div class="teu-form-footer">
                ${msg("alreadyHaveAccount")!"Đã có tài khoản?"}
                <a href="${url.loginUrl}" class="teu-link-primary">
                    ${msg("backToLogin")!"Đăng nhập ngay"}
                </a>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>