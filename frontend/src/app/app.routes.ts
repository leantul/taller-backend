import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login-page.component').then((module) => module.LoginPageComponent) },
  { path: '', loadComponent: () => import('./features/dashboard/dashboard-page.component').then((module) => module.DashboardPageComponent), canActivate: [authGuard] },
  { path: 'clientes', loadComponent: () => import('./features/clients/clients-page.component').then((module) => module.ClientsPageComponent), canActivate: [authGuard] },
  { path: 'dispositivos', loadComponent: () => import('./features/devices/devices-page.component').then((module) => module.DevicesPageComponent), canActivate: [authGuard] },
  { path: 'reparaciones', loadComponent: () => import('./features/repairs/repairs-page.component').then((module) => module.RepairsPageComponent), canActivate: [authGuard] },
  { path: 'seguimientos', loadComponent: () => import('./features/follow-ups/follow-ups-page.component').then((module) => module.FollowUpsPageComponent), canActivate: [authGuard] },
  { path: 'finanzas', loadComponent: () => import('./features/finance/finance-page.component').then((module) => module.FinancePageComponent), canActivate: [authGuard] },
  { path: 'notificaciones', loadComponent: () => import('./features/notifications/notifications-page.component').then((module) => module.NotificationsPageComponent), canActivate: [authGuard] },
  { path: 'status', loadComponent: () => import('./features/status/status-page.component').then((module) => module.StatusPageComponent), canActivate: [authGuard] },
  { path: 'configuracion', loadComponent: () => import('./features/workshop/workshop-settings-page.component').then((module) => module.WorkshopSettingsPageComponent), canActivate: [authGuard] },
  { path: 'cambiar-password', loadComponent: () => import('./features/auth/change-password-page.component').then((module) => module.ChangePasswordPageComponent), canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
