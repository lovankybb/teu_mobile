<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${msg("errorTitle")!"ĐÃ XẢY RA LỖI"}
    <#elseif section = "form">
        <div style="text-align: center; padding: 20px 0;">
            <div style="width: 56px; height: 56px; border-radius: 50%; background-color: #FEF2F2; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#DC2626" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
            </div>
            <p style="font-size: 15px; color: var(--slate-700); margin-bottom: 24px; line-height: 1.6;">
                ${message.summary}
            </p>

            <#if client?? && client.baseUrl?has_content>
                <a href="${client.baseUrl}" class="teu-btn-primary" style="text-decoration: none;">
                    ${kcSanitize(msg("backToApplication"))?no_esc!"Quay lại ứng dụng"}
                </a>
            <#elseif pageRedirectUrl?has_content>
                <a href="${pageRedirectUrl}" class="teu-btn-primary" style="text-decoration: none;">
                    ${kcSanitize(msg("backToApplication"))?no_esc!"Quay lại ứng dụng"}
                </a>
            <#else>
                <a href="${url.loginUrl}" class="teu-btn-primary" style="text-decoration: none;">
                    ${msg("backToLogin")!"Quay lại trang đăng nhập"}
                </a>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>