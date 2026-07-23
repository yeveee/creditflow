import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth-guard';
import { roleGuard } from './core/guards/role-guard';
import { Shell } from './layout/shell/shell';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Connexion — CreditFlow',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        title: 'CreditFlow — Nexus Bank',
        loadComponent: () =>
          import('./features/home/home-redirect').then((m) => m.HomeRedirect),
      },
      {
        path: 'demandes',
        title: 'Demandes de crédit — CreditFlow',
        canActivate: [roleGuard('ANALYSTE', 'DIRECTEUR', 'CLIENT')],
        loadComponent: () =>
          import('./features/demandes/demande-list/demande-list').then((m) => m.DemandeList),
      },
      {
        path: 'demandes/nouvelle',
        title: 'Nouvelle demande — CreditFlow',
        canActivate: [roleGuard('CLIENT')],
        loadComponent: () =>
          import('./features/demandes/demande-create/demande-create').then(
            (m) => m.DemandeCreate,
          ),
      },
      {
        path: 'demandes/:id',
        title: 'Détail de la demande — CreditFlow',
        canActivate: [roleGuard('ANALYSTE', 'CLIENT')],
        loadComponent: () =>
          import('./features/demandes/demande-detail/demande-detail').then(
            (m) => m.DemandeDetail,
          ),
      },
    ],
  },
  {
    path: 'forbidden',
    title: 'Accès refusé — CreditFlow',
    loadComponent: () =>
      import('./features/errors/forbidden/forbidden').then((m) => m.Forbidden),
  },
  { path: '**', redirectTo: '' },
];
