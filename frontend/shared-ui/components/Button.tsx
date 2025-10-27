/**
 * Button Component
 *
 * Reusable button component following BrandBook 2024 design system.
 * Supports multiple variants, sizes, and states.
 */

import React from 'react';
import { Loader2 } from 'lucide-react';
import { cn } from '../utils/cn';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  /** Button visual variant */
  variant?: 'primary' | 'secondary' | 'ghost' | 'outline' | 'danger';
  /** Button size */
  size?: 'sm' | 'md' | 'lg';
  /** Loading state */
  isLoading?: boolean;
  /** Icon to display before text */
  leftIcon?: React.ReactNode;
  /** Icon to display after text */
  rightIcon?: React.ReactNode;
  /** Make button full width */
  fullWidth?: boolean;
}

/**
 * Button component with BrandBook 2024 styling
 */
export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      variant = 'primary',
      size = 'md',
      isLoading = false,
      leftIcon,
      rightIcon,
      fullWidth = false,
      disabled,
      className,
      children,
      ...props
    },
    ref
  ) => {
    const baseStyles =
      'inline-flex items-center justify-center font-medium transition-smooth focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50';

    const variants = {
      primary:
        'bg-primary text-white hover:bg-primary-600 focus-visible:ring-primary shadow-sm hover:shadow-md',
      secondary:
        'bg-secondary text-white hover:bg-secondary-600 focus-visible:ring-secondary shadow-sm hover:shadow-md',
      ghost:
        'bg-transparent hover:bg-gray-100 text-text-primary focus-visible:ring-gray-300',
      outline:
        'bg-transparent border-2 border-gray-300 hover:border-primary hover:text-primary text-text-primary focus-visible:ring-primary',
      danger:
        'bg-error text-white hover:bg-error-dark focus-visible:ring-error shadow-sm hover:shadow-md',
    };

    const sizes = {
      sm: 'h-9 px-3 text-sm rounded-xl gap-1.5',
      md: 'h-11 px-5 text-base rounded-xl gap-2',
      lg: 'h-14 px-7 text-lg rounded-2xl gap-2.5',
    };

    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={cn(
          baseStyles,
          variants[variant],
          sizes[size],
          fullWidth && 'w-full',
          className
        )}
        {...props}
      >
        {isLoading && <Loader2 className="animate-spin" size={size === 'sm' ? 16 : 20} />}
        {!isLoading && leftIcon && leftIcon}
        {children}
        {!isLoading && rightIcon && rightIcon}
      </button>
    );
  }
);

Button.displayName = 'Button';
