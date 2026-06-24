import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div class="card">
      <h2>Panel Principal</h2>
      <p>Bienvenido al sistema. Utiliza el menú superior para navegar por las diferentes opciones.</p>
    </div>
  `,
  styles: [`
    .card { padding: 2rem; }
  `]
})
export class DashboardComponent {}
