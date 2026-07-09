import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { MainLayoutComponent } from './core/layout/main-layout/main-layout';
import { CargaComponent } from './features/panel/carga/carga';
import { InventarioComponent } from './features/panel/inventario/inventario';
import { Asignacion } from './features/panel/asignacion/asignacion';
import { Calidad } from './features/panel/calidad/calidad';
import { Costos } from './features/panel/costos/costos';
import { Liquidaciones } from './features/panel/liquidaciones/liquidaciones';
import { GestionComponent } from './features/panel/gestion/gestion';
import { AuditoriasComponent } from './features/panel/auditorias/auditorias';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';


export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  {
    path: 'panel',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'carga', pathMatch: 'full' },
      { path: 'asignacion', component: Asignacion, canActivate: [roleGuard], data: { roles: ['GERENTE', 'SUPERVISOR'] } },
      { path: 'carga', component: CargaComponent, canActivate: [roleGuard], data: { roles: ['GERENTE'] } },
      { path: 'calidad', component: Calidad, canActivate: [roleGuard], data: { roles: ['SUPERVISOR'] } },
      { path: 'costos', component: Costos, canActivate: [roleGuard], data: { roles: ['GERENTE'] } },
      { path: 'liquidaciones', component: Liquidaciones, canActivate: [roleGuard], data: { roles: ['GERENTE'] } },
      { path: 'inventario', component: InventarioComponent, canActivate: [roleGuard], data: { roles: ['GERENTE', 'SUPERVISOR'] } },
      { path: 'gestion', component: GestionComponent, canActivate: [roleGuard], data: { roles: ['SUPERVISOR'] } },
      { path: 'auditorias', component: AuditoriasComponent, canActivate: [roleGuard], data: { roles: ['SUPERVISOR'] } }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
