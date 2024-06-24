/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      backgroundImage: theme => ({
        'custom-background': 'linear-gradient(309.55deg, #D783FF 27.61%, #F2DCFC 108.56%)'
      }),
      colors: {
        purpleBackground: '#F3F2F8',
      },
    },
  },
  plugins: [],
}