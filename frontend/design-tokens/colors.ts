/**
 * PagoDirecto BrandBook 2024 - Color Tokens
 *
 * Primary brand colors and vertical-specific accent colors.
 * Use these tokens for consistent theming across the application.
 */

export const colors = {
  // Primary Brand Colors (Logo Orange)
  primary: {
    DEFAULT: '#FF7628', // Orange
    50: '#FFF4ED',
    100: '#FFE4D4',
    200: '#FFC8A9',
    300: '#FFA978',
    400: '#FF7628', // Main
    500: '#FF7628',
    600: '#E65A1A',
    700: '#C44A14',
    800: '#9D3B10',
    900: '#752E05',
  },

  // Secondary Brand Colors (Text Brown)
  secondary: {
    DEFAULT: '#2B1406', // Dark Brown
    50: '#F8F6F5',
    100: '#F0EEEE',
    200: '#E5E2E0',
    300: '#C2B9B3',
    400: '#998A7F',
    500: '#2B1406', // Main
    600: '#231005',
    700: '#1A0C04',
    800: '#120803',
    900: '#0A0402',
  },

  // Background Colors
  background: {
    DEFAULT: '#FFFFFF',
    light: '#FAFAFA',
    lighter: '#F8EFEA',
    dark: '#2B1406',
  },

  // Text Colors
  text: {
    primary: '#2B1406',
    secondary: 'rgba(43, 20, 6, 0.6)',
    tertiary: 'rgba(43, 20, 6, 0.5)',
    inverse: '#FFFFFF',
  },

  // Accent Colors (Orange variants from Figma)
  accent: {
    orange: {
      DEFAULT: '#FF7628',
      light: '#FFE4D4',
      dark: '#752E05',
    },
    yellow: {
      DEFAULT: '#FFA928',
      light: 'rgba(255, 169, 40, 0.3)',
      dark: '#C68420',
    },
    amber: {
      DEFAULT: '#FFD028',
      light: 'rgba(255, 208, 40, 0.3)',
      dark: '#CCA620',
    },
  },

  // Functional Colors
  success: {
    DEFAULT: '#11A142',
    light: 'rgba(17, 161, 66, 0.08)',
    border: 'rgba(17, 161, 66, 0.6)',
    dark: '#0D8234',
  },
  warning: {
    DEFAULT: '#FFA928',
    light: '#FFF3E0',
    dark: '#C68420',
  },
  error: {
    DEFAULT: '#FF1212',
    light: '#FFE5E5',
    dark: '#CC0E0E',
  },
  info: {
    DEFAULT: '#FF7628',
    light: '#FFE4D4',
    dark: '#E65A1A',
  },

  // Neutral Grays (from Figma neutral-200)
  gray: {
    50: '#FAFAFA',
    100: '#F5F5F5',
    200: '#E5E5E5', // neutral-200 from Figma
    300: '#D4D4D4',
    400: '#A3A3A3',
    500: '#737373',
    600: '#525252',
    700: '#404040',
    800: '#262626',
    900: '#171717',
    950: '#0A0A0A',
  },

  // Additional UI Colors
  border: {
    DEFAULT: '#E5E5E5',
    light: '#F0EEEE',
    dark: 'rgba(43, 20, 6, 0.12)',
  },
} as const;

export type ColorToken = typeof colors;
