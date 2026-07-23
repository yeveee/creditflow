export type StatutDemande =
  | 'SOUMISE'
  | 'EN_INSTRUCTION'
  | 'SCORING_EN_COURS'
  | 'APPROUVEE'
  | 'REFUSEE'
  | 'EN_ATTENTE_PIECES';

export interface DemandeCredit {
  id: number;
  montant: number;
  dureeMois: number;
  nomEmprunteur: string;
  statut: StatutDemande;
  dateCreation: string;
  scoreCredit?: number | null;
  risqueCredit?: string | null;
}

export interface CreerDemandeRequest {
  montant: number;
  dureeMois: number;
  nomEmprunteur: string;
}

export interface ChangementStatut {
  statut: StatutDemande;
}
