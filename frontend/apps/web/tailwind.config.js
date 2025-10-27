import { colors } from '../../design-tokens/colors';
import { typography } from '../../design-tokens/typography';
import { spacing } from '../../design-tokens/spacing';
import { animations } from '../../design-tokens/animations';

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
    '../../shared-ui/**/*.{js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      colors,
      fontFamily: typography.fontFamily,
      fontSize: typography.fontSize,
      fontWeight: typography.fontWeight,
      letterSpacing: typography.letterSpacing,
      lineHeight: typography.lineHeight,
      spacing: spacing.spacing,
      borderRadius: spacing.borderRadius,
      boxShadow: spacing.boxShadow,
      transitionDuration: animations.transitionDuration,
      transitionTimingFunction: animations.transitionTimingFunction,
      animation: animations.animation,
      keyframes: animations.keyframes,

      // Additional BrandBook-specific utilities
      backgroundImage: {
        'gradient-primary': 'linear-gradient(135deg, #0066FF 0%, #FF2463 100%)',
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'gradient-conic': 'conic-gradient(from 180deg at 50% 50%, var(--tw-gradient-stops))',
      },

      // Squircle-inspired backdrop blur
      backdropBlur: {
        xs: '2px',
        sm: '4px',
        DEFAULT: '8px',
        md: '12px',
        lg: '16px',
        xl: '24px',
        '2xl': '40px',
        '3xl': '64px',
      },
    },
  },
  plugins: [],
};
