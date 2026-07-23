import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  ChangementStatut,
  CreerDemandeRequest,
  DemandeCredit,
  StatutDemande,
} from '../models/demande.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class DemandeService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/demandes';

  lister(page = 0, size = 10, sort = 'dateCreation,desc'): Observable<Page<DemandeCredit>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    return this.http.get<Page<DemandeCredit>>(this.base, { params });
  }

  obtenir(id: number): Observable<DemandeCredit> {
    return this.http.get<DemandeCredit>(`${this.base}/${id}`);
  }

  creer(payload: CreerDemandeRequest): Observable<DemandeCredit> {
    return this.http.post<DemandeCredit>(this.base, payload);
  }

  changerStatut(id: number, statut: StatutDemande): Observable<DemandeCredit> {
    const body: ChangementStatut = { statut };
    return this.http.patch<DemandeCredit>(`${this.base}/${id}/statut`, body);
  }
}
