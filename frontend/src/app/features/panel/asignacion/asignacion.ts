import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AsignacionService, AsignacionResponse } from '../../../core/services/asignacion';
import { LiquidacionService } from '../../../core/services/liquidacion';

@Component({
  selector: 'app-asignacion',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './asignacion.html',
  styleUrl: './asignacion.scss',
})
export class Asignacion implements OnInit {

  resultado: AsignacionResponse | null = null;
  cargando = false;
  esperando = true;
  enviando = false;
  error = '';
  roles = ['Apoyos', 'Limpieza', 'Clasificado', 'Envasado'];

  private kilos = 0;
  private tiempo = 0;

  constructor(
    private asignacionService: AsignacionService,
    private liquidacionService: LiquidacionService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const kilos = params['kilos'];
      const tiempo = params['tiempo'];

      if (kilos && tiempo) {
        this.esperando = false;
        this.cargando = true;
        this.kilos = Number(kilos);
        this.tiempo = Number(tiempo);
        this.cargarAsignacion();
      }
    });
  }

  cargarAsignacion(): void {
    this.asignacionService.generarAsignacion({ kilos: this.kilos, tiempoObjetivo: this.tiempo }).subscribe({
      next: (resultado) => {
        this.resultado = resultado;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = err.error?.message || 'Error al cargar la asignación de personal';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  enviarALiquidaciones(): void {
    if (!this.resultado) return;

    const horasEfectivas = Math.max(0, this.tiempo - 1);
    const trabajadores: { trabajadorId: number; kilosProcesados: number }[] = [];

    for (const rol of this.roles) {
      const lista = this.resultado.asignaciones[rol] || [];
      for (const trabajador of lista) {
        trabajadores.push({
          trabajadorId: trabajador.id,
          kilosProcesados: Math.round(trabajador.rendimiento * horasEfectivas * 100) / 100
        });
      }
    }

    this.enviando = true;
    this.liquidacionService.registrarLote(trabajadores).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/panel/liquidaciones']);
      },
      error: () => {
        this.enviando = false;
        this.error = 'Error al enviar las liquidaciones.';
        this.cdr.detectChanges();
      }
    });
  }
}
