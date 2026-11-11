<#macro registrationLayout displayInfo=false displayMessage=true displayRequiredFields=false showAnotherWayIfPresent=true>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="${locale.currentLanguageTag}">
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="robots" content="noindex, nofollow">

    <title><#nested "header"> - TEU Mobile</title>
    <link rel="icon" href="${url.resourcesPath}/img/logo.svg" type="image/svg+xml">

    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
</head>
<body>
<div class="teu-auth-wrapper">
    <!-- =======================================================================
         LEFT COLUMN: FORM PANEL (48% Desktop / 100% Mobile)
         ======================================================================= -->
    <div class="teu-form-panel">
        <!-- Brand Header -->
        <div class="teu-brand-header">
            <a href="/" class="teu-brand-logo" aria-label="TEU Mobile">
                <img src="${url.resourcesPath}/img/logo.svg" alt="TEU Mobile Logo" onerror="this.onerror=null; this.src='${url.resourcesPath}/img/logo.png';" />
                <div class="teu-brand-title">
                    <span class="bold">TẾU</span>
                    <span class="light">Mobile</span>
                </div>
            </a>

            <div style="display: flex; align-items: center; gap: 12px;">
                <#if realm.internationalizationEnabled?? && realm.internationalizationEnabled && (locale.supported?size gt 1)>
                    <div class="teu-lang-wrapper">
                        <select class="teu-lang-select" onchange="window.location.href=this.value;">
                            <#list locale.supported as l>
                                <option value="${l.url}" <#if l.languageTag == locale.currentLanguageTag>selected</#if>>
                                    ${l.label}
                                </option>
                            </#list>
                        </select>
                    </div>
                </#if>
                <a href="/" class="teu-back-link">
                    ${msg("backToHome")!"Về trang chủ"}
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="9 18 15 12 9 6"></polyline>
                    </svg>
                </a>
            </div>
        </div>

        <!-- Center Form Container -->
        <div class="teu-form-container">
            <!-- Title & Subtitle -->
            <div class="teu-heading">
                <h1><#nested "header"></h1>
                <#if displayInfo>
                    <#nested "info">
                </#if>
            </div>

            <!-- Security Trust Badge -->
            <div class="teu-security-badge">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                    <path d="m9 12 2 2 4-4"></path>
                </svg>
                <p>${kcSanitize(msg("securityBadgeNotice"))?no_esc}</p>
            </div>

            <!-- Flash Alert Box (Error / Warning / Success / Info) -->
            <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                <div class="teu-alert teu-alert-${message.type}">
                    <#if message.type = 'error'>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="12" cy="12" r="10"></circle>
                            <line x1="12" y1="8" x2="12" y2="12"></line>
                            <line x1="12" y1="16" x2="12.01" y2="16"></line>
                        </svg>
                    <#elseif message.type = 'warning'>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                            <line x1="12" y1="9" x2="12" y2="13"></line>
                            <line x1="12" y1="17" x2="12.01" y2="17"></line>
                        </svg>
                    <#elseif message.type = 'success'>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                            <polyline points="22 4 12 14.01 9 11.01"></polyline>
                        </svg>
                    <#else>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="12" cy="12" r="10"></circle>
                            <line x1="12" y1="16" x2="12" y2="12"></line>
                            <line x1="12" y1="8" x2="12.01" y2="8"></line>
                        </svg>
                    </#if>
                    <div>${kcSanitize(message.summary)?no_esc}</div>
                </div>
            </#if>

            <!-- Main Form Injection -->
            <#nested "form">

            <!-- Social Providers Injection -->
            <#nested "socialProviders">
        </div>

        <!-- Footer Trust Badges -->
        <div class="teu-trust-footer">
            <div class="teu-trust-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                    <path d="m9 12 2 2 4-4"></path>
                </svg>
                <span>Bảo mật chuẩn Keycloak OIDC</span>
            </div>
            <div class="teu-trust-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="#F59E0B" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
                </svg>
                <span>Đồng bộ tức thì</span>
            </div>
            <div class="teu-trust-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="#EA384D" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
                    <line x1="12" y1="18" x2="12.01" y2="18"></line>
                </svg>
                <span>Hàng chính hãng 100%</span>
            </div>
        </div>
    </div>

    <!-- =======================================================================
         RIGHT COLUMN: HERO VISUAL SHOWCASE PANEL (52% Desktop / Hidden on Mobile)
         ======================================================================= -->
    <div class="teu-hero-panel" aria-hidden="true">
        <div class="teu-coral-glow"></div>

        <div class="teu-showcase-wrapper">
            <div class="teu-showcase-card">
                <!-- Tech Badge -->
                <div class="teu-tech-badge">
                    <span class="dot"></span>
                    Next-Gen Experience
                </div>

                <!-- Phone Mockup Illustration -->
                <div class="teu-phone-wrapper">
                    <img src="${url.resourcesPath}/img/phone-illustration.svg" alt="Phone Showcase" />
                </div>

                <!-- Headline -->
                <h2 class="teu-hero-headline">
                    MUA SẮM <span class="accent">ĐỈNH CAO</span>
                </h2>
                <p class="teu-hero-desc">
                    Điện thoại &amp; phụ kiện công nghệ chính hãng. Trải nghiệm mua sắm mượt mà, bảo mật tuyệt đối.
                </p>

                <!-- Stats Grid -->
                <div class="teu-stats-grid">
                    <div>
                        <div class="teu-stat-value">100%</div>
                        <div class="teu-stat-label">Chính hãng</div>
                    </div>
                    <div>
                        <div class="teu-stat-value accent">24/7</div>
                        <div class="teu-stat-label">Hỗ trợ</div>
                    </div>
                    <div>
                        <div class="teu-stat-value">0%</div>
                        <div class="teu-stat-label">Trả góp</div>
                    </div>
                </div>
            </div>

            <!-- Floating Cyberpunk Badges -->
            <div class="teu-floating-tag top-right">Hot Deal 🔥</div>
            <div class="teu-floating-tag bottom-left">Freeship toàn quốc</div>
        </div>
    </div>
</div>
</body>
</html>
</#macro>