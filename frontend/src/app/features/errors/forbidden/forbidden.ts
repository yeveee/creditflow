import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  imports: [RouterLink],
  template: `
    <div class="page">
      <div class="container">
        <div class="card">
          <div class="card-body empty-state">
            <h3>Accès refusé</h3>
            <p>Vous n'avez pas les droits nécessaires pour accéder à cette page.</p>
            <a routerLink="/" class="btn btn-primary" style="margin-top: 12px">
              Retour à l'accueil
            </a>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class Forbidden {}
