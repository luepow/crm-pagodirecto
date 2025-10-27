/**
 * PagoDirecto BrandBook 2024 - Spacing and Border Tokens
 *
 * Spacing scale and border radius values (squircle style).
 */

export const spacing = {
  spacing: {
    px: '1px',
    0: '0',
    0.5: '0.125rem', // 2px
    1: '0.25rem', // 4px
    1.5: '0.375rem', // 6px
    2: '0.5rem', // 8px
    2.5: '0.625rem', // 10px
    3: '0.75rem', // 12px
    3.5: '0.875rem', // 14px
    4: '1rem', // 16px
    5: '1.25rem', // 20px
    6: '1.5rem', // 24px
    7: '1.75rem', // 28px
    8: '2rem', // 32px
    9: '2.25rem', // 36px
    10: '2.5rem', // 40px
    11: '2.75rem', // 44px
    12: '3rem', // 48px
    14: '3.5rem', // 56px
    16: '4rem', // 64px
    20: '5rem', // 80px
    24: '6rem', // 96px
    28: '7rem', // 112px
    32: '8rem', // 128px
  },

  // Border radius (from Figma design)
  borderRadius: {
    none: '0',
    sm: '0.25rem', // 4px
    DEFAULT: '0.375rem', // 6px - Default rounded corners
    md: '0.5rem', // 8px - Cards, containers
    lg: '1rem', // 16px - Large containers (from Figma)
    xl: '1.5rem', // 24px
    '2xl': '2rem', // 32px
    '3xl': '2.5rem', // 40px
    full: '62.5rem', // 1000px - Pill shapes
  },

  // Shadow system (from Figma)
  boxShadow: {
    sm: '0 2px 4px 0 rgba(0, 0, 0, 0.04)',
    DEFAULT: '0 2px 8px 0 rgba(0, 0, 0, 0.14)',
    md: '0 4px 12px 0 rgba(0, 0, 0, 0.1)',
    lg: '0 8px 16px 0 rgba(0, 0, 0, 0.12)',
    xl: '0 12px 24px 0 rgba(0, 0, 0, 0.15)',
    '2xl': '0 24px 48px 0 rgba(0, 0, 0, 0.2)',
    inner: 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.06)',
    none: 'none',
  },
} as const;

export type SpacingToken = typeof spacing;
