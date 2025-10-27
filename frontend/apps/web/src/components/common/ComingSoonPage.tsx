/**
 * ComingSoonPage Component
 *
 * Reusable "Coming Soon" page for modules under development.
 */

import React from 'react';
import { LucideIcon, Construction, Calendar, Code } from 'lucide-react';
import { Card, CardContent } from '@shared-ui/components/Card';

interface ComingSoonPageProps {
  /** Module title */
  title: string;
  /** Module description */
  description: string;
  /** Icon component from lucide-react */
  icon?: LucideIcon;
  /** Expected release date (optional) */
  releaseDate?: string;
  /** List of planned features (optional) */
  features?: string[];
}

export const ComingSoonPage: React.FC<ComingSoonPageProps> = ({
  title,
  description,
  icon: Icon = Construction,
  releaseDate,
  features,
}) => {
  return (
    <div className="flex min-h-[calc(100vh-200px)] items-center justify-center p-6">
      <Card className="w-full max-w-2xl">
        <CardContent padding="lg">
          <div className="text-center space-y-6">
            {/* Icon */}
            <div className="flex justify-center">
              <div className="p-6 bg-gradient-to-br from-primary/10 to-primary/5 rounded-3xl">
                <Icon className="w-20 h-20 text-primary" />
              </div>
            </div>

            {/* Title */}
            <div>
              <h1 className="text-4xl font-bold text-text-primary mb-2">{title}</h1>
              <p className="text-lg text-text-secondary">{description}</p>
            </div>

            {/* Badge */}
            <div className="inline-flex items-center gap-2 px-6 py-3 bg-yellow-50 border border-yellow-200 rounded-full">
              <Code className="w-5 h-5 text-yellow-600" />
              <span className="text-sm font-semibold text-yellow-800">
                Módulo en Desarrollo
              </span>
            </div>

            {/* Release Date */}
            {releaseDate && (
              <div className="flex items-center justify-center gap-2 text-text-secondary">
                <Calendar className="w-5 h-5" />
                <span className="text-sm">Disponible aproximadamente: <strong>{releaseDate}</strong></span>
              </div>
            )}

            {/* Features List */}
            {features && features.length > 0 && (
              <div className="mt-8 pt-6 border-t border-gray-200">
                <h3 className="text-lg font-semibold text-text-primary mb-4">
                  Funcionalidades Planificadas
                </h3>
                <ul className="space-y-2 text-left max-w-md mx-auto">
                  {features.map((feature, index) => (
                    <li key={index} className="flex items-start gap-2">
                      <div className="mt-1 w-1.5 h-1.5 rounded-full bg-primary flex-shrink-0" />
                      <span className="text-text-secondary">{feature}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Footer Message */}
            <div className="mt-8 pt-6 border-t border-gray-200">
              <p className="text-sm text-text-secondary">
                Nuestro equipo está trabajando en este módulo.
                <br />
                Te notificaremos cuando esté disponible.
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
