export function getCsrfToken() {
    const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);

    if (match) {
        return decodeURIComponent(match[1]);
    }

    return null;
}