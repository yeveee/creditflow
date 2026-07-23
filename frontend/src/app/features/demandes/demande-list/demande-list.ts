import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { DemandeCredit, StatutDemande } from '../../../core/models/demande.model';
import { Page } from '../../../core/models/page.model';
import { AuthService } from '../../../core/services/auth.service';
import { DemandeService } from '../../../core/services/demande.service';
import { ToastService } from '../../../core/services/toast.service';
import { riskClass } from '../../../core/util/statut.util';
import { StatutActions } from '../../../shared/statut-actions/statut-actions';
import { StatutBadge } from '../../../shared/statut-badge/statut-badge';

@Component({
  selector: 'app-demande-list',
  imports: [CurrencyPipe, DatePipe, RouterLink, StatutBadge, StatutActions],
  templateUrl: './demande-list.html',
  styleUrl: './demande-list.scss',
})
export class DemandeList implements OnInit {
  private readonly service = inject(DemandeService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  readonly page = signal<Page<DemandeCredit> | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly pageIndex = signal(0);
  readonly pageSize = 10;

  readonly updating = signal<number | null>(null);
  readonly selected = signal<DemandeCredit | null>(null);

  readonly canViewDetail = computed(() => this.auth.hasRole('ANALYSTE', 'CLIENT'));
  readonly canChangeStatut = computed(() => this.auth.hasRole('ANALYSTE', 'DIRECTEUR'));

  readonly riskClass = riskClass;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.lister(this.pageIndex(), this.pageSize).subscribe({
      next: (p) => {
        this.page.set(p);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les demandes.');
        this.loading.set(false);
      },
    });
  }

  goTo(index: number): void {
    const total = this.page()?.totalPages ?? 1;
    if (index < 0 || index >= total) {
      return;
    }
    this.pageIndex.set(index);
    this.load();
  }

  openManage(demande: DemandeCredit): void {
    this.selected.set(demande);
  }

  closeManage(): void {
    this.selected.set(null);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.selected()) {
      this.closeManage();
    }
  }

  applyStatut(nouveau: StatutDemande): void {
    const demande = this.selected();
    if (!demande) {
      return;
    }
    this.updating.set(demande.id);
    this.service.changerStatut(demande.id, nouveau).subscribe({
      next: (updated) => {
        this.updating.set(null);
        this.selected.set(null);
        this.patchRow(updated);
        this.toast.success(`Statut mis à jour : demande #${updated.id}`);
      },
      error: (err: HttpErrorResponse) => {
        this.updating.set(null);
        this.toast.error(
          err.status === 403
            ? "Vous n'avez pas les droits pour cette action."
            : 'La transition de statut a échoué.',
        );
      },
    });
  }

  private patchRow(updated: DemandeCredit): void {
    const current = this.page();
    if (!current) {
      return;
    }
    this.page.set({
      ...current,
      content: current.content.map((d) => (d.id === updated.id ? updated : d)),
    });
  }
}
