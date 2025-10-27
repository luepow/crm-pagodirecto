/**
 * PagoDirecto BrandBook 2024 - Animation Tokens
 *
 * Transition durations, easing functions, and keyframe animations.
 * Supports micro-animations and smooth transitions.
 */

export const animations = {
  transitionDuration: {
    fast: '150ms',
    base: '200ms',
    slow: '300ms',
    slower: '500ms',
  },

  transitionTimingFunction: {
    DEFAULT: 'cubic-bezier(0.4, 0, 0.2, 1)',
    linear: 'linear',
    in: 'cubic-bezier(0.4, 0, 1, 1)',
    out: 'cubic-bezier(0, 0, 0.2, 1)',
    'in-out': 'cubic-bezier(0.4, 0, 0.2, 1)',
    bounce: 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
  },

  animation: {
    none: 'none',
    spin: 'spin 1s linear infinite',
    ping: 'ping 1s cubic-bezier(0, 0, 0.2, 1) infinite',
    pulse: 'pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
    bounce: 'bounce 1s infinite',
    'fade-in': 'fadeIn 200ms ease-in',
    'fade-out': 'fadeOut 200ms ease-out',
    'slide-in-up': 'slideInUp 300ms ease-out',
    'slide-in-down': 'slideInDown 300ms ease-out',
    'slide-in-left': 'slideInLeft 300ms ease-out',
    'slide-in-right': 'slideInRight 300ms ease-out',
    'scale-in': 'scaleIn 200ms ease-out',
    'scale-out': 'scaleOut 200ms ease-in',
  },

  keyframes: {
    spin: {
      from: { transform: 'rotate(0deg)' },
      to: { transform: 'rotate(360deg)' },
    },
    ping: {
      '75%, 100%': { transform: 'scale(2)', opacity: '0' },
    },
    pulse: {
      '0%, 100%': { opacity: '1' },
      '50%': { opacity: '.5' },
    },
    bounce: {
      '0%, 100%': {
        transform: 'translateY(-25%)',
        animationTimingFunction: 'cubic-bezier(0.8, 0, 1, 1)',
      },
      '50%': {
        transform: 'translateY(0)',
        animationTimingFunction: 'cubic-bezier(0, 0, 0.2, 1)',
      },
    },
    fadeIn: {
      from: { opacity: '0' },
      to: { opacity: '1' },
    },
    fadeOut: {
      from: { opacity: '1' },
      to: { opacity: '0' },
    },
    slideInUp: {
      from: { transform: 'translateY(100%)', opacity: '0' },
      to: { transform: 'translateY(0)', opacity: '1' },
    },
    slideInDown: {
      from: { transform: 'translateY(-100%)', opacity: '0' },
      to: { transform: 'translateY(0)', opacity: '1' },
    },
    slideInLeft: {
      from: { transform: 'translateX(-100%)', opacity: '0' },
      to: { transform: 'translateX(0)', opacity: '1' },
    },
    slideInRight: {
      from: { transform: 'translateX(100%)', opacity: '0' },
      to: { transform: 'translateX(0)', opacity: '1' },
    },
    scaleIn: {
      from: { transform: 'scale(0.9)', opacity: '0' },
      to: { transform: 'scale(1)', opacity: '1' },
    },
    scaleOut: {
      from: { transform: 'scale(1)', opacity: '1' },
      to: { transform: 'scale(0.9)', opacity: '0' },
    },
  },
} as const;

export type AnimationToken = typeof animations;
