<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${msg("pageExpiredTitle")!"PHIÊN LÀM VIỆC HẾT HẠN"}
    <#elseif section = "form">
        <div style="text-align: center; padding: 20px 0;">
            <div style="width: 56px; height: 56px; border-radius: 50%; background-color: #FFFBEB; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#F59E0B" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
            </div>
            <p style="font-size: 15px; color: var(--slate-700); margin-bottom: 24px; line-height: 1.6;">
                ${msg("pageExpiredMsg1")!"Phiên đăng nhập của bạn đã hết hạn do không có tương tác trong một khoảng thời gian."}
            </p>

            <a href="${url.loginRestartFlowUrl}" class="teu-btn-primary" style="text-decoration: none;">
                <span>${msg("doRestartFlow")!"Bắt đầu lại đăng nhập"}</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                    <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
            </a>
        </div>
    </#if>
</@layout.registrationLayout>