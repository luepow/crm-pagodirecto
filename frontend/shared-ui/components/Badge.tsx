/**
 * Badge Component
 *
 * Small label component for status indicators and tags.
 * Follows BrandBook 2024 design system.
 */

import React from 'react';
import { cn } from '../utils/cn';

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  /** Badge visual variant */
  variant?: 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info';
  /** Badge size */
  size?: 'sm' | 'md' | 'lg';
  /** Show dot indicator */
  dot?: boolean;
}

/**
 * Badge component for status indicators and labels
 */
export const Badge = React.forwardRef<HTMLSpanElement, BadgeProps>(
  ({ variant = 'default', size = 'md', dot = false, className, children, ...props }, ref) => {
    const baseStyles =
      'inline-flex items-center font-medium transition-smooth whitespace-nowrap';

    const variants = {
      default: 'bg-gray-100 text-gray-700 border border-gray-200',
      primary: 'bg-primary/10 text-primary border border-primary/20',
      secondary: 'bg-secondary/10 text-secondary border border-secondary/20',
      success: 'bg-success/10 text-success-dark border border-success/20',
      warning: 'bg-warning/10 text-warning-dark border border-warning/20',
      error: 'bg-error/10 text-error-dark border border-error/20',
      info: 'bg-info/10 text-info-dark border border-info/20',
    };

    const sizes = {
      sm: 'h-5 px-2 text-xs rounded-lg gap-1',
      md: 'h-6 px-2.5 text-sm rounded-lg gap-1.5',
      lg: 'h-7 px-3 text-base rounded-xl gap-2',
    };

    const dotColors = {
      default: 'bg-gray-500',
      primary: 'bg-primary',
      secondary: 'bg-secondary',
      success: 'bg-success',
      warning: 'bg-warning',
      error: 'bg-error',
      info: 'bg-info',
    };

    return (
      <span
        ref={ref}
        className={cn(baseStyles, variants[variant], sizes[size], className)}
        {...props}
      >
        {dot && (
          <span
            className={cn('h-1.5 w-1.5 rounded-full', dotColors[variant])}
            aria-hidden="true"
          />
        )}
        {children}
      </span>
    );
  }
);

Badge.displayName = 'Badge';
