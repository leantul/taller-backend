import { Injectable } from '@angular/core';

export type ThemeMode = 'dark' | 'light';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly key = 'theme-mode';

  initTheme(): ThemeMode {
    const stored = localStorage.getItem(this.key) as ThemeMode | null;
    const mode = stored ?? 'dark';
    this.setTheme(mode);
    return mode;
  }

  toggleTheme(current: ThemeMode): ThemeMode {
    const next: ThemeMode = current === 'dark' ? 'light' : 'dark';
    this.setTheme(next);
    return next;
  }

  private setTheme(mode: ThemeMode): void {
    document.documentElement.setAttribute('data-theme', mode);
    localStorage.setItem(this.key, mode);
  }
}
