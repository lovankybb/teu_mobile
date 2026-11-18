<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#if messageHeader??>
            ${messageHeader}
        <#else>
            ${msg("infoTitle")!"THÔNG BÁO"}
        </#if>
    <#elseif section = "form">
        <div style="text-align: center; padding: 20px 0;">
            <div style="width: 56px; height: 56px; border-radius: 50%; background-color: #ECFDF5; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
            </div>
            <p style="font-size: 15px; color: var(--slate-700); margin-bottom: 24px; line-height: 1.6;">
                ${message.summary}
            </p>

            <#if skipLink??>
            <#elseif pageRedirectUrl?has_content>
                <a href="${pageRedirectUrl}" class="teu-btn-primary" style="text-decoration: none;">
                    ${kcSanitize(msg("backToApplication"))?no_esc!"Tiếp tục"}
                </a>
            <#elseif actionUri?has_content>
                <a href="${actionUri}" class="teu-btn-primary" style="text-decoration: none;">
                    ${kcSanitize(msg("proceedWithAction"))?no_esc!"Tiếp tục"}
                </a>
            <#elseif client?? && client.baseUrl?has_content>
                <a href="${client.baseUrl}" class="teu-btn-primary" style="text-decoration: none;">
                    ${kcSanitize(msg("backToApplication"))?no_esc!"Quay lại ứng dụng"}
                </a>
            <#else>
                <a href="${url.loginUrl}" class="teu-btn-primary" style="text-decoration: none;">
                    ${msg("backToLogin")!"Quay lại đăng nhập"}
                </a>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>