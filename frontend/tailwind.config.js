/** @type {import('tailwindcss').Config} */
export default {
  darkMode: ['class'],
  content: ['./src/renderer/index.html', './src/renderer/src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        background: 'var(--background)',
        surface: 'var(--surface)',
        'surface-raised': 'var(--surface-raised)',
        border: 'var(--border)',
        'text-primary': 'var(--text-primary)',
        'text-secondary': 'var(--text-secondary)',
        'text-muted': 'var(--text-muted)',
        accent: 'var(--accent)',
        'accent-hover': 'var(--accent-hover)',
        'accent-subtle': 'var(--accent-subtle)',
        success: 'var(--success)',
        warning: 'var(--warning)',
        danger: 'var(--danger)',
        info: 'var(--info)'
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif']
      },
      fontSize: {
        xs: '12px',
        sm: '14px',
        base: '16px',
        lg: '20px',
        xl: '24px',
        '2xl': '32px',
        '3xl': '40px'
      },
      spacing: {
        1: '4px',
        2: '8px',
        3: '12px',
        4: '16px',
        6: '24px',
        8: '32px',
        12: '48px',
        16: '64px'
      },
      borderRadius: {
        DEFAULT: '8px',
        lg: '12px'
      },
      transitionDuration: {
        DEFAULT: '175ms'
      },
      transitionTimingFunction: {
        DEFAULT: 'ease-out'
      }
    }
  },
  plugins: []
}
