import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly showPassword = signal(false);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  readonly demoAccounts = [
    { username: 'client1', role: 'CLIENT' },
    { username: 'analyste1', role: 'ANALYSTE' },
    { username: 'directeur1', role: 'DIRECTEUR' },
  ];

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        const redirect = this.route.snapshot.queryParamMap.get('redirect') ?? '/';
        this.router.navigateByUrl(redirect);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(
          err.status === 401
            ? 'Identifiants invalides. Veuillez réessayer.'
            : "Connexion impossible. Vérifiez que l'API est démarrée (port 8080).",
        );
      },
    });
  }

  useDemo(username: string): void {
    this.form.setValue({ username, password: 'password123' });
    this.error.set(null);
  }

  togglePassword(): void {
    this.showPassword.update((v) => !v);
  }
}
