import colors from 'tailwindcss/colors';

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        slate: {
          ...colors.neutral,
          800: '#121212', // Darker card background
          900: '#000000', // Pure black body background
        },
        brand: {
          50: '#fdf2f6',
          100: '#fae6ee',
          200: '#f5ccd9',
          300: '#efa2bb',
          400: '#e56f94',
          500: '#d54271',
          600: '#bf0c4f', // Primary Brand Color
          700: '#a3083e',
          800: '#880a36',
          900: '#730d31',
          950: '#410318',
        },
        accent: {
          500: '#10B981', // Emerald for success
        }
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'Inter', 'sans-serif'],
      },
      boxShadow: {
        'soft': '0 4px 20px -2px rgba(191, 12, 79, 0.1)',
        'hover': '0 10px 25px -5px rgba(191, 12, 79, 0.15), 0 8px 10px -6px rgba(191, 12, 79, 0.1)',
        'glow': '0 0 20px rgba(191, 12, 79, 0.5)',
        'btn': '0 4px 14px 0 rgba(191, 12, 79, 0.3)',
      }
    },
  },
  plugins: [],
}
