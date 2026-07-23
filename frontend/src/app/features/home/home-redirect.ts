import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home-redirect',
  template: `<div class="loading-center">
    <div class="spinner spinner-dark spinner-lg"></div>
  </div>`,
})
export class HomeRedirect implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    if (this.auth.hasRole('ANALYSTE', 'DIRECTEUR', 'CLIENT')) {
      this.router.navigate(['/demandes']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
