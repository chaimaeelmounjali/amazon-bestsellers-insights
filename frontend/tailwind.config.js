/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/**/*.{js,jsx,ts,tsx}",
        "./public/index.html"
    ],
    theme: {
        extend: {
            colors: {
                amazon: {
                    light: '#232F3E',
                    DEFAULT: '#131921',
                    accent: '#FF9900',
                },
                primary: {
                    DEFAULT: '#FF9900', // Amazon Orange
                    hover: '#e88b00'
                },
                secondary: {
                    DEFAULT: '#232F3E' // Amazon Blue
                }
            },
            fontFamily: {
                sans: ['Inter', 'ui-sans-serif', 'system-ui'],
            },
        },
    },
    plugins: [],
}
