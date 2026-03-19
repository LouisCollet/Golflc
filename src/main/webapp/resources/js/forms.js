"use strict";

/* ============================================================
   DARK MODE TOGGLE — switches PrimeFaces theme (saga ↔ arya)
   + PrimeOne tokens (primeone-light ↔ primeone-dark)
   + data-theme attribute on <html> for custom CSS overrides
   No page reload — instant swap via CSS link href replacement
   ============================================================ */
function toggleDarkMode(button) {
    var isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    applyTheme(isDark ? 'light' : 'dark');
    localStorage.setItem('golf-theme', isDark ? 'light' : 'dark');
}

function applyTheme(mode) {
    var isDark = (mode === 'dark');
    document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');

    /* Swap PrimeFaces theme: saga ↔ arya */
    var themeLink = document.querySelector('link[href*="theme.css"]');
    if (themeLink) {
        var href = themeLink.getAttribute('href');
        if (isDark && href.indexOf('arya') === -1) {
            themeLink.setAttribute('href', href.replace('saga', 'arya'));
        } else if (!isDark && href.indexOf('saga') === -1) {
            themeLink.setAttribute('href', href.replace('arya', 'saga'));
        }
    }

    /* Swap PrimeOne design tokens: primeone-light ↔ primeone-dark */
    var primeOneLink = document.querySelector('link[href*="primeone-light"], link[href*="primeone-dark"]');
    if (primeOneLink) {
        var poHref = primeOneLink.getAttribute('href');
        if (isDark) {
            primeOneLink.setAttribute('href', poHref.replace('primeone-light', 'primeone-dark'));
        } else {
            primeOneLink.setAttribute('href', poHref.replace('primeone-dark', 'primeone-light'));
        }
    }

    /* Toggle button icon: moon (light mode) ↔ sun (dark mode) */
    var icon = document.querySelector('[id$="themeToggle"] .pi');
    if (icon) {
        icon.classList.toggle('pi-moon', !isDark);
        icon.classList.toggle('pi-sun', isDark);
    }
}

/* Restore saved theme on DOMContentLoaded */
document.addEventListener('DOMContentLoaded', function() {
    var saved = localStorage.getItem('golf-theme');
    if (saved === 'dark') {
        applyTheme('dark');
    }
});

/* ============================================================
   PLAYER SELECTOR — toggle Select/PlayerList buttons
   ============================================================ */
function togglePlayerSelectButton(input) {
    var v = input.value.replace(/\D/g, '');
    var hasInput = v.length > 0 && v !== '0';
    var container = input.closest('[id$=containerPlayer]');
    if (!container) return;
    var btn = container.querySelector('[id$=wrapperButtonSelect]');
    if (btn) {
        btn.style.display = hasInput ? '' : 'none';
    }
    var toggleBtn = document.querySelector('[id$=togglePlayerList]');
    if (toggleBtn) {
        toggleBtn.style.display = hasInput ? 'none' : '';
    }
}

/* ============================================================
   MENU — hide sidebar and go full width (used in hole.xhtml)
   ============================================================ */
function hideMenuFullWidth() {
    var menuCol = document.querySelector('.md\\:col-2');
    var contentCol = document.querySelector('.md\\:col-10');
    if (menuCol) menuCol.style.display = 'none';
    if (contentCol) {
        contentCol.classList.remove('md:col-10');
        contentCol.classList.add('md:col-12');
    }
}

/* ============================================================
   HOLE FORM — total distance from 3 inputs
   ============================================================ */
function total(frm) {
    frm.holeDistance.value = parseInt(frm.holeDistance_100.value)
            + parseInt(frm.holeDistance_10.value)
            + parseInt(frm.holeDistance_1.value);
}

/* ============================================================
   KEYBOARD — init keypress listener
   ============================================================ */
function init() {
    console.info("init: keypress listener registered");
    document.addEventListener("keypress", myFunction, false);
}
function myFunction(el) {
    console.log("keypress: name=" + el.name + " id=" + el.id);
}

/* ============================================================
   UTILITY
   ============================================================ */
function javascript_abort() {
    console.log("javascript_abort called");
    throw new Error('This is not an error. This is just to abort javascript');
}

function jqueryVersion() {
    if (window.jQuery) {
        console.log("jQuery version = " + jQuery.fn.jquery);
    }
}
