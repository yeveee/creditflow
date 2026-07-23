import { StatutDemande } from '../models/demande.model';

export const STATUT_LABELS: Record<StatutDemande, string> = {
  SOUMISE: 'Soumise',
  EN_INSTRUCTION: 'En instruction',
  SCORING_EN_COURS: 'Scoring en cours',
  APPROUVEE: 'Approuvée',
  REFUSEE: 'Refusée',
  EN_ATTENTE_PIECES: 'En attente de pièces',
};

export const STATUT_BADGE_CLASS: Record<StatutDemande, string> = {
  SOUMISE: 'badge-soumise',
  EN_INSTRUCTION: 'badge-instruction',
  SCORING_EN_COURS: 'badge-scoring',
  APPROUVEE: 'badge-approuvee',
  REFUSEE: 'badge-refusee',
  EN_ATTENTE_PIECES: 'badge-attente',
};

/**
 * Mirror of the backend StatutTransitionValidator.
 * Only these transitions are accepted by the API.
 */
export const TRANSITIONS: Record<StatutDemande, StatutDemande[]> = {
  SOUMISE: ['EN_INSTRUCTION'],
  EN_INSTRUCTION: ['SCORING_EN_COURS'],
  SCORING_EN_COURS: ['APPROUVEE', 'REFUSEE', 'EN_ATTENTE_PIECES'],
  EN_ATTENTE_PIECES: ['EN_INSTRUCTION'],
  APPROUVEE: [],
  REFUSEE: [],
};

export function statutLabel(statut: StatutDemande): string {
  return STATUT_LABELS[statut] ?? statut;
}

export function statutBadgeClass(statut: StatutDemande): string {
  return STATUT_BADGE_CLASS[statut] ?? 'badge-default';
}

export function nextStatuts(statut: StatutDemande): StatutDemande[] {
  return TRANSITIONS[statut] ?? [];
}

export function isTerminal(statut: StatutDemande): boolean {
  return nextStatuts(statut).length === 0;
}

export function riskClass(risque?: string | null): string {
  switch ((risque ?? '').toUpperCase()) {
    case 'FAIBLE':
      return 'risk-faible';
    case 'MOYEN':
      return 'risk-moyen';
    case 'ELEVE':
    case 'ÉLEVÉ':
      return 'risk-eleve';
    default:
      return '';
  }
}
