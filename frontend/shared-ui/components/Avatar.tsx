/**
 * Avatar Component
 *
 * User avatar with initials fallback.
 * Follows BrandBook 2024 design system.
 */

import React from 'react';
import { User } from 'lucide-react';
import { cn } from '../utils/cn';

export interface AvatarProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Image source URL */
  src?: string;
  /** Alt text for image */
  alt?: string;
  /** Initials to display if no image */
  initials?: string;
  /** Avatar size */
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  /** Avatar shape */
  shape?: 'circle' | 'square';
}

/**
 * Avatar component for user profiles
 */
export const Avatar = React.forwardRef<HTMLDivElement, AvatarProps>(
  (
    { src, alt = '', initials, size = 'md', shape = 'circle', className, ...props },
    ref
  ) => {
    const [imageError, setImageError] = React.useState(false);

    const sizes = {
      xs: 'h-6 w-6 text-xs',
      sm: 'h-8 w-8 text-sm',
      md: 'h-10 w-10 text-base',
      lg: 'h-12 w-12 text-lg',
      xl: 'h-16 w-16 text-2xl',
    };

    const shapes = {
      circle: 'rounded-full',
      square: 'rounded-xl',
    };

    const iconSizes = {
      xs: 14,
      sm: 16,
      md: 20,
      lg: 24,
      xl: 32,
    };

    const baseStyles =
      'inline-flex items-center justify-center overflow-hidden bg-gradient-to-br from-primary to-verticals-viajes text-white font-medium';

    const showImage = src && !imageError;
    const showInitials = !showImage && initials;
    const showIcon = !showImage && !initials;

    return (
      <div
        ref={ref}
        className={cn(baseStyles, sizes[size], shapes[shape], className)}
        {...props}
      >
        {showImage && (
          <img
            src={src}
            alt={alt}
            className="h-full w-full object-cover"
            onError={() => setImageError(true)}
          />
        )}
        {showInitials && <span className="uppercase">{initials}</span>}
        {showIcon && <User size={iconSizes[size]} />}
      </div>
    );
  }
);

Avatar.displayName = 'Avatar';
