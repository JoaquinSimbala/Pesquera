import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { AsignacionService, AsignacionResponse } from '../../../core/services/asignacion';

@Component({
  selector: 'app-asignacion',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './asignacion.html',
  styleUrl: './asignacion.scss',
})
export class Asignacion implements OnInit {
  private asignacionService = inject(AsignacionService);
  private route = inject(ActivatedRoute);

  isLoading = false;
  resultado: AsignacionResponse | null = null;
  errorMessage = '';
  esperandoDatos = true;

  rolesList = ['Apoyos', 'Limpieza', 'Clasificado', 'Envasado'];

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const kilos = params['kilos'];
      const tiempo = params['tiempo'];

      if (kilos && tiempo) {
        this.esperandoDatos = false;
        this.cargarAsignacion(Number(kilos), Number(tiempo));
      } else {
        this.esperandoDatos = true;
      }
    });
  }

  cargarAsignacion(kilos: number, tiempoObjetivo: number) {
    this.isLoading = true;
    this.errorMessage = '';
    
    const calculo = { kilos, tiempoObjetivo };

    this.asignacionService.generarAsignacion(calculo).subscribe({
      next: (res) => {
        this.resultado = res;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al cargar la asignación de personal';
        this.isLoading = false;
      }
    });
  }
}
