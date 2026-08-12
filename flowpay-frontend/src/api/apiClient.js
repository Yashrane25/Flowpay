import { getCsrfToken } from './csrf';

export async function apiFetch(path, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    if (method !== 'GET') {
        const csrfToken = getCsrfToken();
        if (csrfToken) {
            headers['X-XSRF-TOKEN'] = csrfToken;
        }
    }

    const response = await fetch(path, {
        ...options,
        method,
        headers,
        credentials: 'include', //Required, without this the browser wont send or store the httpOnly cookie at all
    });

    if (!response.ok) {
        let errorMessage = 'Request failed';
        let fieldErrors = null;

        try {
            const errorBody = await response.json();
            if (errorBody && errorBody.message) {
                errorMessage = errorBody.message;
            }
            if (errorBody && errorBody.fieldErrors) {
                fieldErrors = errorBody.fieldErrors;
            }
        } catch (parseError) {
            errorMessage = response.statusText;
        }

        const error = new Error(errorMessage);
        error.fieldErrors = fieldErrors; //attach extra detail onto the Error object
        throw error;
    }

    return response.json();
}