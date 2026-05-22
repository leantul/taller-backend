import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ThemeMode = 'dark' | 'light';

const THEME_STORAGE_KEY = 'theme-mode';

function readStoredTheme(): ThemeMode {
  try {
    const stored = localStorage.getItem(THEME_STORAGE_KEY);
    if (stored === 'light' || stored === 'dark') {
      return stored;
    }
  } catch {
    // localStorage not available (SSR, privacy mode)
  }
  return 'dark';
}

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly modeSubject = new BehaviorSubject<ThemeMode>(readStoredTheme());

  readonly mode$ = this.modeSubject.asObservable();

  initTheme(): ThemeMode {
    const mode = this.modeSubject.value;
    document.documentElement.setAttribute('data-theme', mode);
    if (mode === 'dark') {
      document.documentElement.classList.add('p-dark');
    } else {
      document.documentElement.classList.remove('p-dark');
    }
    return mode;
  }

  toggleTheme(current: ThemeMode): ThemeMode {
    const next: ThemeMode = current === 'dark' ? 'light' : 'dark';
    this.setTheme(next);
    return next;
  }

  currentTheme(): ThemeMode {
    return this.modeSubject.value;
  }

  private setTheme(mode: ThemeMode): void {
    document.documentElement.setAttribute('data-theme', mode);
    if (mode === 'dark') {
      document.documentElement.classList.add('p-dark');
    } else {
      document.documentElement.classList.remove('p-dark');
    }
    localStorage.setItem(THEME_STORAGE_KEY, mode);
    this.modeSubject.next(mode);
  }
}

