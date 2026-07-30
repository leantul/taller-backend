import { HttpInterceptorFn } from '@angular/common/http';
import { inject, NgZone } from '@angular/core';
import { catchError, finalize, throwError } from 'rxjs';
import { MessageService } from 'primeng/api';
import { LoadingService } from '../services/loading.service';
import { ErrorDialogService } from '../services/error-dialog.service';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const loading = inject(LoadingService);
  const authService = inject(AuthService);
  const messageService = inject(MessageService);
  const errorDialogService = inject(ErrorDialogService);
  const zone = inject(NgZone);
  const token = authService.getToken();
  const isBackgroundRequest = req.url.includes('/notifications/unread-count');

  if (!isBackgroundRequest) {
    zone.run(() => loading.show());
  }
  const isAuthRequest = req.url.includes('/auth/login');

  const request = token && !isAuthRequest
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((error) => {
      if (error?.status === 401 && !isAuthRequest) {
        authService.logout();
      } else if (!isAuthRequest) {
        zone.run(() => {
          const detail = resolveErrorDetail(error);
          messageService.add({
            severity: 'error',
            summary: `Error ${error?.status || ''}`.trim(),
            detail
          });
          errorDialogService.show(`Error ${error?.status || ''}`.trim(), detail);
        });
      }
      return throwError(() => error);
    }),
    finalize(() => {
      if (isBackgroundRequest) {
        return;
      }
      const finishRequest = () => zone.run(() => loading.hide());

      if (typeof requestAnimationFrame === 'function') {
        requestAnimationFrame(finishRequest);
      } else {
        setTimeout(finishRequest, 0);
      }
    })
  );
};

function resolveErrorDetail(error: any): string {
  if (typeof error?.error === 'string' && error.error.trim()) {
    return error.error;
  }
  if (error?.error?.error) {
    return String(error.error.error);
  }
  if (error?.error?.message) {
    return String(error.error.message);
  }
  if (error?.message) {
    return String(error.message);
  }
  return 'Ocurrió un error inesperado al procesar la solicitud.';
}
