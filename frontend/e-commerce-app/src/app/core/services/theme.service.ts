import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private isDarkMode = new BehaviorSubject<boolean>(false);
  isDarkMode$ = this.isDarkMode.asObservable();

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    // Only access browser APIs if we're in a browser environment
    if (isPlatformBrowser(this.platformId)) {
      this.loadThemeFromStorage();
    }
  }

  toggleTheme(): void {
    const newValue = !this.isDarkMode.value;
    this.isDarkMode.next(newValue);

    if (isPlatformBrowser(this.platformId)) {
      this.saveThemeToStorage(newValue);
      this.applyTheme(newValue);
    }
  }

  private loadThemeFromStorage(): void {
    // Only run in browser
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    const storedTheme = localStorage.getItem('darkMode');
    if (storedTheme) {
      const isDark = storedTheme === 'true';
      this.isDarkMode.next(isDark);
      this.applyTheme(isDark);
    } else {
      // Check system preference
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      this.isDarkMode.next(prefersDark);
      this.applyTheme(prefersDark);
    }
  }

  private saveThemeToStorage(isDark: boolean): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('darkMode', isDark.toString());
    }
  }

  private applyTheme(isDark: boolean): void {
    if (isPlatformBrowser(this.platformId)) {
      if (isDark) {
        document.body.classList.add('dark-mode');
      } else {
        document.body.classList.remove('dark-mode');
      }
    }
  }
}