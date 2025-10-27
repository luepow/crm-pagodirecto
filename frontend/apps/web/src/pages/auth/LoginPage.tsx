/**
 * Login Page
 *
 * User authentication page with email and password.
 * Follows BrandBook 2024 design guidelines.
 */

import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Mail, Lock, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@shared-ui/components/Button';
import { Input } from '@shared-ui/components/Input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@shared-ui/components/Card';
import { login } from '../../lib/api/auth';
import { useAuthStore } from '../../lib/stores/authStore';
import { getErrorMessage } from '../../lib/api/client';

/**
 * Login form validation schema
 */
const loginSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(6, 'La contraseña debe tener al menos 6 caracteres'),
});

type LoginFormData = z.infer<typeof loginSchema>;

/**
 * Login page component
 */
export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { setAuth } = useAuthStore();
  const [isLoading, setIsLoading] = React.useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      setIsLoading(true);
      const response = await login(data);

      // Map backend response to expected format
      const user = {
        id: response.username,  // Use username as id temporarily
        email: response.email,
        nombre: response.username,
        apellido: '',
        rol: 'ADMIN' as const,
        unidadNegocioId: '',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      // Backend returns token, not accessToken/refreshToken
      setAuth(user, response.token, response.token);
      toast.success('Bienvenido a PagoDirecto CRM');
      navigate('/dashboard');
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary/20 via-background-lighter to-accent-yellow/10 p-4 relative overflow-hidden">
      {/* Decorative background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-primary/10 rounded-full blur-3xl"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-accent-yellow/10 rounded-full blur-3xl"></div>
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-primary/5 rounded-full blur-3xl"></div>
      </div>

      <div className="w-full max-w-md relative z-10">
        {/* Logo and Brand */}
        <div className="mb-8 text-center animate-fade-in">
          {/* Logo SVG */}
          <div className="mb-6 inline-flex">
            <svg width="80" height="80" viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="80" height="80" rx="16" fill="#FF7628"/>
              <path d="M25 30C25 27.2386 27.2386 25 30 25H40C42.7614 25 45 27.2386 45 30V35H25V30Z" fill="white" fillOpacity="0.9"/>
              <rect x="25" y="35" width="20" height="20" rx="2" fill="white"/>
              <circle cx="55" cy="45" r="10" fill="#FFD028"/>
              <path d="M50 45L53 48L60 40" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <h1 className="text-4xl font-sans font-bold text-text-primary mb-2">PagoDirecto CRM</h1>
          <p className="text-lg font-body text-text-secondary">Sistema de Gestión Empresarial</p>
        </div>

        {/* Login Card */}
        <Card className="backdrop-blur-sm bg-white/95 shadow-xl border-gray-200 animate-slide-up">
          <CardHeader className="space-y-2 pb-6">
            <CardTitle className="text-2xl font-sans text-text-primary">Iniciar Sesión</CardTitle>
            <CardDescription className="text-base font-body text-text-secondary">
              Ingresa tus credenciales para acceder al sistema
            </CardDescription>
          </CardHeader>

          <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
              <div className="space-y-4">
                <Input
                  {...register('email')}
                  type="email"
                  label="Correo Electrónico"
                  placeholder="tu@empresa.com"
                  error={errors.email?.message}
                  leftIcon={<Mail size={20} className="text-text-tertiary" />}
                  isRequired
                  disabled={isLoading}
                  className="bg-white border-border focus:border-primary focus:ring-primary/20"
                />

                <Input
                  {...register('password')}
                  type="password"
                  label="Contraseña"
                  placeholder="••••••••"
                  error={errors.password?.message}
                  leftIcon={<Lock size={20} className="text-text-tertiary" />}
                  isRequired
                  disabled={isLoading}
                  className="bg-white border-border focus:border-primary focus:ring-primary/20"
                />
              </div>

              <div className="flex items-center justify-between text-sm">
                <label className="flex items-center text-text-secondary font-body cursor-pointer hover:text-text-primary transition-colors">
                  <input
                    type="checkbox"
                    className="mr-2 h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary focus:ring-offset-0"
                  />
                  Recordarme
                </label>
                <Link
                  to="/forgot-password"
                  className="text-primary font-body hover:text-primary-600 transition-colors"
                >
                  ¿Olvidaste tu contraseña?
                </Link>
              </div>

              <Button
                type="submit"
                fullWidth
                size="lg"
                isLoading={isLoading}
                className="bg-primary text-white hover:bg-primary-600 shadow-md hover:shadow-lg transition-all font-sans text-base"
              >
                Iniciar Sesión
              </Button>

              {/* Credenciales de prueba */}
              <div className="rounded-lg bg-primary/5 border border-primary/20 p-4">
                <div className="flex gap-3 text-sm">
                  <AlertCircle size={20} className="flex-shrink-0 text-primary mt-0.5" />
                  <div className="font-body">
                    <p className="font-semibold text-text-primary mb-2">Credenciales de prueba:</p>
                    <div className="space-y-1 text-text-secondary">
                      <p>📧 Email: <span className="font-mono text-xs bg-white px-2 py-1 rounded">admin@admin.com</span></p>
                      <p>🔒 Contraseña: <span className="font-mono text-xs bg-white px-2 py-1 rounded">admin123</span></p>
                    </div>
                  </div>
                </div>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Footer */}
        <p className="mt-6 text-center text-sm font-body text-text-tertiary">
          © 2024 PagoDirecto CRM. Todos los derechos reservados.
        </p>
      </div>

      <style>{`
        @keyframes fade-in {
          from { opacity: 0; transform: translateY(-10px); }
          to { opacity: 1; transform: translateY(0); }
        }
        @keyframes slide-up {
          from { opacity: 0; transform: translateY(20px); }
          to { opacity: 1; transform: translateY(0); }
        }
        .animate-fade-in {
          animation: fade-in 0.6s ease-out;
        }
        .animate-slide-up {
          animation: slide-up 0.6s ease-out 0.2s both;
        }
      `}</style>
    </div>
  );
};
