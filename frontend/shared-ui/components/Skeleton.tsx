/**
 * Skeleton Component
 *
 * Loading skeleton for content placeholders.
 * Follows BrandBook 2024 design system.
 */

import React from 'react';
import { cn } from '../utils/cn';

export interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Skeleton shape */
  variant?: 'text' | 'circular' | 'rectangular';
  /** Width (can be number or string) */
  width?: number | string;
  /** Height (can be number or string) */
  height?: number | string;
}

/**
 * Skeleton loading component
 */
export const Skeleton = React.forwardRef<HTMLDivElement, SkeletonProps>(
  ({ variant = 'rectangular', width, height, className, style, ...props }, ref) => {
    const baseStyles = 'animate-pulse bg-gray-200';

    const variants = {
      text: 'rounded h-4',
      circular: 'rounded-full',
      rectangular: 'rounded-xl',
    };

    const combinedStyle = {
      width: width ? (typeof width === 'number' ? `${width}px` : width) : undefined,
      height: height ? (typeof height === 'number' ? `${height}px` : height) : undefined,
      ...style,
    };

    return (
      <div
        ref={ref}
        className={cn(baseStyles, variants[variant], className)}
        style={combinedStyle}
        {...props}
      />
    );
  }
);

Skeleton.displayName = 'Skeleton';

/**
 * Card skeleton for loading states
 */
export const CardSkeleton: React.FC = () => {
  return (
    <div className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
      <div className="space-y-4">
        <Skeleton variant="text" width="60%" height={24} />
        <Skeleton variant="text" width="100%" />
        <Skeleton variant="text" width="80%" />
        <div className="flex gap-2 pt-2">
          <Skeleton variant="rectangular" width={80} height={32} />
          <Skeleton variant="rectangular" width={80} height={32} />
        </div>
      </div>
    </div>
  );
};

/**
 * Table skeleton for loading states
 */
export const TableSkeleton: React.FC<{ rows?: number }> = ({ rows = 5 }) => {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, index) => (
        <div key={index} className="flex items-center gap-4">
          <Skeleton variant="circular" width={40} height={40} />
          <div className="flex-1 space-y-2">
            <Skeleton variant="text" width="40%" />
            <Skeleton variant="text" width="60%" />
          </div>
        </div>
      ))}
    </div>
  );
};
