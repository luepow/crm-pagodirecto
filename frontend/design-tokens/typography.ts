/**
 * PagoDirecto BrandBook 2024 - Typography Tokens
 *
 * Font families, sizes, weights, and line heights.
 * Uses "Outfit Sans" as primary font with system fallbacks.
 */

export const typography = {
  fontFamily: {
    // Inter for headings, numbers, and important labels
    sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
    // Plus Jakarta Sans for body text and secondary labels
    body: ['Plus Jakarta Sans', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
    mono: ['JetBrains Mono', 'Menlo', 'Monaco', 'Courier New', 'monospace'],
  },

  fontSize: {
    xs: ['0.75rem', { lineHeight: 'normal', letterSpacing: '-0.01em' }], // 12px - Small labels
    sm: ['0.875rem', { lineHeight: 'normal', letterSpacing: '-0.01em' }], // 14px - Body text, menu items
    base: ['1rem', { lineHeight: 'normal', letterSpacing: '-0.01em' }], // 16px - Large body text
    lg: ['1.125rem', { lineHeight: 'normal', letterSpacing: '-0.01em' }], // 18px - Page titles
    xl: ['1.25rem', { lineHeight: 'normal' }], // 20px
    '2xl': ['1.5rem', { lineHeight: 'normal', letterSpacing: '-0.01em' }], // 24px - Large numbers
    '3xl': ['2rem', { lineHeight: 'normal', letterSpacing: '-0.01em' }], // 32px - Display numbers
    '4xl': ['2.25rem', { lineHeight: 'normal' }], // 36px
    '5xl': ['3rem', { lineHeight: '1' }], // 48px
    '6xl': ['3.75rem', { lineHeight: '1' }], // 60px
  },

  fontWeight: {
    light: '300',
    normal: '400',
    medium: '500',
    semibold: '600',
    bold: '700',
  },

  letterSpacing: {
    tight: '-0.02em',
    normal: '-0.01em', // Slight negative tracking as per Figma
    wide: '0em',
  },

  lineHeight: {
    none: '1',
    tight: '1.125', // 18px
    snug: '1.375',
    normal: 'normal', // Browser default
    relaxed: '1.625',
    loose: '2',
  },
} as const;

export type TypographyToken = typeof typography;
