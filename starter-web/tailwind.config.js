/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts}'],
  theme: {
    extend: {
      colors: {
        primary: { DEFAULT: '#184AEF', dark: '#0a2dbf', light: '#4a70f5' },
        secondary: { DEFAULT: '#27F3E0' },
        neutral: { 0: '#ffffff', 50: '#F5F0F3', 200: '#E3D4DD', 500: '#666666', 900: '#000000' }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace']
      },
      borderRadius: { s: '0.5em', DEFAULT: '0.5rem' },
      maxWidth: { content: '80rem' }
    }
  },
  plugins: []
}
