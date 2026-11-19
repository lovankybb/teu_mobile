<#macro registrationLayout displayInfo=false displayMessage=true displayRequiredFields=false showAnotherWayIfPresent=true>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="${locale.currentLanguageTag}">
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="robots" content="noindex, nofollow">

    <title><#nested "header"> - TEU Mobile</title>
    <link rel="icon" href="${url.resourcesPath}/img/logo.png" type="image/svg+xml">

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
<#-- Dynamic app / home URL resolution for return navigation -->
<#assign appHomeUrl = (client.baseUrl?? && client.baseUrl?has_content)?then(client.baseUrl, (client.rootUrl?? && client.rootUrl?has_content)?then(client.rootUrl, "http://localhost:5173/"))>

<div class="teu-auth-wrapper">
    <!-- =======================================================================
         LEFT COLUMN: FORM PANEL (50% Desktop / 100% Mobile)
         ======================================================================= -->
    <div class="teu-form-panel">
        <!-- Brand Header (Compact & refined, kept per user request) -->
        <div class="teu-brand-header">
            <a href="${appHomeUrl}" class="teu-brand-logo" aria-label="TEU Mobile">
                <img src="${url.resourcesPath}/img/logo.png" alt="TEU Mobile Logo" onerror="this.onerror=null; this.src='${url.resourcesPath}/img/logo.png';" />
                <div class="teu-brand-title">
                    <span class="bold">TẾU</span>
                    <span class="light">Mobile</span>
                </div>
            </a>

            <div class="teu-header-actions">
                <#if realm.internationalizationEnabled?? && realm.internationalizationEnabled && (locale.supported?size gt 1)>
                    <div class="teu-lang-wrapper">
                        <select class="teu-lang-select" onchange="window.location.href=this.value;" aria-label="Ngôn ngữ / Language">
                            <#list locale.supported as l>
                                <option value="${l.url}" <#if l.languageTag == locale.currentLanguageTag>selected</#if>>
                                    ${l.label}
                                </option>
                            </#list>
                        </select>
                    </div>
                </#if>
                <a href="${appHomeUrl}" class="teu-back-link">
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

            <!-- Flash Alert Box (Error / Warning / Success / Info) -->
            <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                <div class="teu-alert teu-alert-${message.type}" role="alert">
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

        <!-- Footer Spacer (Ensures clean flex alignment without vertical overflow) -->
        <div class="teu-footer-spacer"></div>
    </div>

    <!-- =======================================================================
         RIGHT COLUMN: HERO VISUAL SHOWCASE PANEL (Matching Figma Design)
         ======================================================================= -->
    <div class="teu-hero-panel" aria-hidden="true">
        <div class="teu-hero-bg-artwork">
            <img src="${url.resourcesPath}/img/hero-artwork.png" alt="Teu Mobile Hero" class="teu-hero-img" />
        </div>
    </div>
</div>
</body>
</html>
</#macro>
