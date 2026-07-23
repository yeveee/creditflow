import { Component, computed, input } from '@angular/core';

import { StatutDemande } from '../../core/models/demande.model';
import { statutBadgeClass, statutLabel } from '../../core/util/statut.util';

@Component({
  selector: 'app-statut-badge',
  template: `<span class="badge {{ cssClass() }}">{{ label() }}</span>`,
})
export class StatutBadge {
  readonly statut = input.required<StatutDemande>();

  readonly label = computed(() => statutLabel(this.statut()));
  readonly cssClass = computed(() => statutBadgeClass(this.statut()));
}
