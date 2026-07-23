import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { DemandeCredit, StatutDemande } from '../../../core/models/demande.model';
import { AuthService } from '../../../core/services/auth.service';
import { DemandeService } from '../../../core/services/demande.service';
import { ToastService } from '../../../core/services/toast.service';
import { riskClass } from '../../../core/util/statut.util';
import { StatutActions } from '../../../shared/statut-actions/statut-actions';
import { StatutBadge } from '../../../shared/statut-badge/statut-badge';

@Component({
  selector: 'app-demande-detail',
  imports: [CurrencyPipe, DatePipe, RouterLink, StatutBadge, StatutActions],
  templateUrl: './demande-detail.html',
  styleUrl: './demande-detail.scss',
})
export class DemandeDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(DemandeService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  readonly demande = signal<DemandeCredit | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly updating = signal(false);

  readonly canChangeStatut = computed(() => this.auth.hasRole('ANALYSTE'));
  readonly canReturnToList = computed(() => this.auth.hasRole('ANALYSTE', 'DIRECTEUR', 'CLIENT'));
  readonly riskClass = riskClass;

  readonly steps = ['Soumise', 'Instruction', 'Scoring', 'Décision'];
  readonly stepIndex = computed(() => {
    switch (this.demande()?.statut) {
      case 'SOUMISE':
        return 0;
      case 'EN_INSTRUCTION':
      case 'EN_ATTENTE_PIECES':
        return 1;
      case 'SCORING_EN_COURS':
        return 2;
      case 'APPROUVEE':
      case 'REFUSEE':
        return 3;
      default:
        return 0;
    }
  });
  readonly isRefused = computed(() => this.demande()?.statut === 'REFUSEE');

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const id = Number(params.get('id'));
      if (!id || Number.isNaN(id)) {
        this.error.set('Identifiant invalide.');
        this.loading.set(false);
        return;
      }
      this.load(id);
    });
  }

  private load(id: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.obtenir(id).subscribe({
      next: (d) => {
        this.demande.set(d);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(
          err.status === 404 ? 'Demande introuvable.' : 'Impossible de charger la demande.',
        );
      },
    });
  }

  applyStatut(nouveau: StatutDemande): void {
    const current = this.demande();
    if (!current) {
      return;
    }
    this.updating.set(true);
    this.service.changerStatut(current.id, nouveau).subscribe({
      next: (updated) => {
        this.updating.set(false);
        this.demande.set(updated);
        this.toast.success('Statut mis à jour.');
      },
      error: (err: HttpErrorResponse) => {
        this.updating.set(false);
        this.toast.error(
          err.status === 403 ? 'Droits insuffisants pour cette action.' : 'La transition a échoué.',
        );
      },
    });
  }
}
