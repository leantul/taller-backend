import { HttpContextToken, HttpInterceptorFn } from '@angular/common/http';
import { ApplicationRef, inject, NgZone } from '@angular/core';
import { catchError, finalize, throwError } from 'rxjs';
import { LoadingService } from '../services/loading.service';
import { AuthService } from './auth.service';

export const SKIP_AUTH_LOGOUT = new HttpContextToken<boolean>(() => false);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const loading = inject(LoadingService);
  const authService = inject(AuthService);
  const appRef = inject(ApplicationRef);
  const zone = inject(NgZone);
  const token = authService.getToken();

  zone.run(() => loading.show());
  const isAuthRequest = req.url.includes('/auth/login');

  const request = token && !isAuthRequest
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((error) => {
      if (error?.status === 401 && !isAuthRequest && !req.context.get(SKIP_AUTH_LOGOUT)) {
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
