import { Component, computed, input, output } from '@angular/core';

import { StatutDemande } from '../../core/models/demande.model';
import { nextStatuts, statutLabel } from '../../core/util/statut.util';

@Component({
  selector: 'app-statut-actions',
  template: `
    @if (options().length) {
      <div class="statut-actions">
        @for (s of options(); track s) {
          <button
            type="button"
            class="btn btn-outline btn-sm"
            [disabled]="disabled()"
            (click)="change.emit(s)"
          >
            {{ label(s) }}
          </button>
        }
      </div>
    } @else {
      <span class="muted" style="font-size: 13px">Aucune transition disponible (statut final)</span>
    }
  `,
  styles: [
    `
      .statut-actions {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
      }
    `,
  ],
})
export class StatutActions {
  readonly statut = input.required<StatutDemande>();
  readonly disabled = input(false);
  readonly change = output<StatutDemande>();

  readonly options = computed(() => nextStatuts(this.statut()));

  label(s: StatutDemande): string {
    return statutLabel(s);
  }
}
