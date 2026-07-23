import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly username = this.auth.username;
  readonly role = this.auth.role;
  readonly isStaff = computed(() => this.auth.hasRole('ANALYSTE', 'DIRECTEUR'));
  readonly isClient = computed(() => this.auth.hasRole('CLIENT'));

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
