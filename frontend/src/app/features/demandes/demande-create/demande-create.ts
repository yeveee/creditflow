import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { DemandeService } from '../../../core/services/demande.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-demande-create',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './demande-create.html',
  styleUrl: './demande-create.scss',
})
export class DemandeCreate {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(DemandeService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    nomEmprunteur: ['', [Validators.required, Validators.minLength(2)]],
    montant: [15000, [Validators.required, Validators.min(1000)]],
    dureeMois: [60, [Validators.required, Validators.min(6)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    this.service.creer(this.form.getRawValue()).subscribe({
      next: (created) => {
        this.submitting.set(false);
        this.toast.success(`Demande #${created.id} soumise avec succès.`);
        this.router.navigate(['/demandes', created.id]);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.error.set(this.extractError(err));
      },
    });
  }

  private extractError(err: HttpErrorResponse): string {
    if (err.status === 400) {
      const body = err.error;
      if (typeof body === 'string') {
        return body;
      }
      if (body?.message) {
        return body.message;
      }
      return 'Données invalides. Vérifiez les champs du formulaire.';
    }
    if (err.status === 403) {
      return "Vous n'avez pas les droits pour soumettre une demande.";
    }
    return 'La soumission a échoué. Réessayez plus tard.';
  }
}
