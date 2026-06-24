import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { MainLayoutComponent } from './core/layout/main-layout/main-layout';
import { DashboardComponent } from './features/dashboard/dashboard';
import { Asignacion } from './features/panel/asignacion/asignacion';
import { Calidad } from './features/panel/calidad/calidad';
import { Costos } from './features/panel/costos/costos';
import { Gerente } from './features/panel/gerente/gerente';
import { Liquidaciones } from './features/panel/liquidaciones/liquidaciones';
import { Supervisor } from './features/panel/supervisor/supervisor';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { 
    path: 'panel', 
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'asignacion', component: Asignacion },
      { path: 'calidad', component: Calidad },
      { path: 'costos', component: Costos },
      { path: 'liquidaciones', component: Liquidaciones },
      { path: 'gerente', component: Gerente },
      { path: 'supervisor', component: Supervisor }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
