import { HttpInterceptorFn } from '@angular/common/http';
import { ApplicationRef, inject, NgZone } from '@angular/core';
import { catchError, finalize, throwError } from 'rxjs';
import { LoadingService } from '../services/loading.service';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  const loading = inject(LoadingService);
  const authService = inject(AuthService);
  const appRef = inject(ApplicationRef);
  const zone = inject(NgZone);

  zone.run(() => loading.show());
  const isAuthRequest = req.url.includes('/auth/login');

  const request = token && !isAuthRequest
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((error) => {
      if (error?.status === 401 || error?.status === 403) {
        authService.logout();
      }
      return throwError(() => error);
    }),
    finalize(() => {
      const finishRequest = () => zone.run(() => {
        loading.hide();
        appRef.tick();
      });

      if (typeof requestAnimationFrame === 'function') {
        requestAnimationFrame(finishRequest);
      } else {
        setTimeout(finishRequest, 0);
      }
    })
  );
};
