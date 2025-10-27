/**
 * Card Component
 *
 * Container component with squircle borders and shadow.
 * Follows BrandBook 2024 design system.
 */

import React from 'react';
import { cn } from '../utils/cn';

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Card variant */
  variant?: 'default' | 'bordered' | 'elevated' | 'glass';
  /** Padding size */
  padding?: 'none' | 'sm' | 'md' | 'lg';
  /** Enable hover effect */
  hoverable?: boolean;
}

/**
 * Card container component
 */
export const Card = React.forwardRef<HTMLDivElement, CardProps>(
  ({ variant = 'default', padding = 'md', hoverable = false, className, children, ...props }, ref) => {
    const baseStyles = 'rounded-2xl transition-smooth';

    const variants = {
      default: 'bg-white shadow-sm border border-gray-100',
      bordered: 'bg-white border-2 border-gray-200',
      elevated: 'bg-white shadow-lg',
      glass: 'bg-white/80 backdrop-blur-md border border-white/20 shadow-sm',
    };

    const paddings = {
      none: '',
      sm: 'p-4',
      md: 'p-6',
      lg: 'p-8',
    };

    const hoverStyles = hoverable ? 'hover:shadow-md hover:scale-[1.02]' : '';

    return (
      <div
        ref={ref}
        className={cn(baseStyles, variants[variant], paddings[padding], hoverStyles, className)}
        {...props}
      >
        {children}
      </div>
    );
  }
);

Card.displayName = 'Card';

/**
 * Card Header component
 */
export interface CardHeaderProps extends React.HTMLAttributes<HTMLDivElement> {}

export const CardHeader = React.forwardRef<HTMLDivElement, CardHeaderProps>(
  ({ className, ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn('flex flex-col space-y-1.5', className)}
        {...props}
      />
    );
  }
);

CardHeader.displayName = 'CardHeader';

/**
 * Card Title component
 */
export interface CardTitleProps extends React.HTMLAttributes<HTMLHeadingElement> {}

export const CardTitle = React.forwardRef<HTMLHeadingElement, CardTitleProps>(
  ({ className, ...props }, ref) => {
    return (
      <h3
        ref={ref}
        className={cn('text-xl font-semibold leading-none tracking-tight text-text-primary', className)}
        {...props}
      />
    );
  }
);

CardTitle.displayName = 'CardTitle';

/**
 * Card Description component
 */
export interface CardDescriptionProps extends React.HTMLAttributes<HTMLParagraphElement> {}

export const CardDescription = React.forwardRef<HTMLParagraphElement, CardDescriptionProps>(
  ({ className, ...props }, ref) => {
    return (
      <p
        ref={ref}
        className={cn('text-sm text-text-secondary', className)}
        {...props}
      />
    );
  }
);

CardDescription.displayName = 'CardDescription';

/**
 * Card Content component
 */
export interface CardContentProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Padding size */
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

export const CardContent = React.forwardRef<HTMLDivElement, CardContentProps>(
  ({ padding, className, ...props }, ref) => {
    const paddings = {
      none: '',
      sm: 'p-4',
      md: 'p-6',
      lg: 'p-8',
    };

    const paddingClass = padding ? paddings[padding] : '';

    return <div ref={ref} className={cn('pt-6', paddingClass, className)} {...props} />;
  }
);

CardContent.displayName = 'CardContent';

/**
 * Card Footer component
 */
export interface CardFooterProps extends React.HTMLAttributes<HTMLDivElement> {}

export const CardFooter = React.forwardRef<HTMLDivElement, CardFooterProps>(
  ({ className, ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn('flex items-center pt-6', className)}
        {...props}
      />
    );
  }
);

CardFooter.displayName = 'CardFooter';
