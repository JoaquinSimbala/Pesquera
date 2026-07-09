import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AsignacionService, AsignacionResponse } from '../../../core/services/asignacion';
import { DialogoService } from '../../../core/services/dialogo';

@Component({
  selector: 'app-asignacion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './asignacion.html',
  styleUrl: './asignacion.scss',
})
export class Asignacion implements OnInit {

  
  vistaActiva: 'sugerencia' | 'ocupados' = 'sugerencia';

  resultado: AsignacionResponse | null = null;
  cargando = false;
  esperando = true;
  enviando = false;
  roles = ['Apoyos', 'Limpieza', 'Clasificado', 'Envasado'];

  kilos = 0;
  tiempo = 0;
  especie = 'Pulpo';

  
  asignacionesActivas: any[] = [];
  cargandoActivas = false;

  
  mostrarModalLiberar = false;
  trabajadorALiberarId: number | null = null;
  kilosALiberar: number | null = null;

  constructor(
    private asignacionService: AsignacionService,
    private route: ActivatedRoute,
    private router: Router,
    private dialogoService: DialogoService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const kilos = params['kilos'];
      const tiempo = params['tiempo'];
      const especie = params['especie'];

      if (kilos && tiempo) {
        this.esperando = false;
        this.cargando = true;
        this.kilos = Number(kilos);
        this.tiempo = Number(tiempo);
        this.especie = especie || 'Pulpo';
        this.cargarSugerencia();
      } else {
        this.vistaActiva = 'ocupados';
        this.esperando = false;
        this.cargarAsignacionesActivas();
      }
    });
  }

  cambiarVista(vista: 'sugerencia' | 'ocupados'): void {
    this.vistaActiva = vista;
    if (vista === 'ocupados') {
      this.cargarAsignacionesActivas();
    }
  }

  cargarSugerencia(): void {
    this.cargando = true;
    this.asignacionService.generarAsignacion({
      kilos: this.kilos,
      tiempoObjetivo: this.tiempo,
      especie: this.especie
    }).subscribe({
      next: (resultado) => {
        this.resultado = resultado;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err.error?.message || 'Error al calcular la sugerencia de personal.';
        this.dialogoService.error('Error de Simulación', msg);
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  confirmarAsignacion(): void {
    if (!this.resultado) return;

    const trabajadorIds: number[] = [];
    for (const rol of this.roles) {
      const lista = this.resultado.asignaciones[rol] || [];
      for (const t of lista) {
        trabajadorIds.push(t.id);
      }
    }

    if (trabajadorIds.length === 0) {
      this.dialogoService.advertencia('Falta Personal', 'No hay trabajadores sugeridos para asignar.');
      return;
    }

    this.enviando = true;
    this.asignacionService.guardarAsignacion({
      kilos: this.kilos,
      tiempoObjetivo: this.tiempo,
      especie: this.especie
    }, trabajadorIds).subscribe({
      next: () => {
        this.enviando = false;
        this.dialogoService.exito('Asignación Guardada', 'La asignación se ha guardado correctamente. Los operarios están marcados como ocupados.');
        this.cambiarVista('ocupados');
      },
      error: (err) => {
        this.enviando = false;
        const msg = err.error?.error || 'Error al confirmar y guardar la asignación.';
        this.dialogoService.error('Error al Guardar', msg);
        this.cdr.detectChanges();
      }
    });
  }

  cargarAsignacionesActivas(): void {
    this.cargandoActivas = true;
    this.asignacionService.obtenerAsignacionesActivas().subscribe({
      next: (data) => {
        this.asignacionesActivas = data;
        this.cargandoActivas = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.dialogoService.error('Error de Carga', 'Error al cargar las asignaciones activas de personal.');
        this.cargandoActivas = false;
        this.cdr.detectChanges();
      }
    });
  }

  abrirModalLiberar(trabajadorId: number): void {
    this.trabajadorALiberarId = trabajadorId;
    this.kilosALiberar = null;
    this.mostrarModalLiberar = true;
  }

  cancelarModalLiberar(): void {
    this.mostrarModalLiberar = false;
    this.trabajadorALiberarId = null;
    this.kilosALiberar = null;
  }

  confirmarLiberacion(): void {
    if (!this.trabajadorALiberarId) return;
    const kilos = this.kilosALiberar;
    if (!kilos || kilos <= 0) {
      this.dialogoService.error('Cantidad Inválida', 'Debe especificar una cantidad de kilos válida y mayor a 0.');
      return;
    }
    this.mostrarModalLiberar = false;
    this.cargandoActivas = true;
    this.asignacionService.liberarTrabajador(this.trabajadorALiberarId, kilos).subscribe({
      next: () => {
        this.trabajadorALiberarId = null;
        this.kilosALiberar = null;
        this.dialogoService.exito('Operario Liberado', 'El operario ha sido liberado de la asignación y su liquidación de pago fue generada.');
        this.cargarAsignacionesActivas();
      },
      error: (err) => {
        const msg = err.error?.error || 'No se pudo liberar al trabajador.';
        this.dialogoService.error('Error de Liberación', msg);
        this.cargandoActivas = false;
        this.cdr.detectChanges();
      }
    });
  }

  irALiquidaciones(asignacion: any): void {
    this.router.navigate(['/panel/liquidaciones'], {
      state: {
        especie: asignacion.especie,
        trabajadores: asignacion.trabajadores.map((t: any) => ({
          trabajadorId: t.id,
          nombreCompleto: t.nombreCompleto,
          rolOperativo: t.rolOperativo
        }))
      }
    });
  }
}
