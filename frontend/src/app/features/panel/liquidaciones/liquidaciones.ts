import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LiquidacionService,
  Liquidacion,
  ResumenLiquidacion,
  Trabajador,
  DatosLiquidacion,
} from '../../../core/services/liquidacion';

@Component({
  selector: 'app-liquidaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './liquidaciones.html',
  styleUrl: './liquidaciones.scss',
})
export class Liquidaciones implements OnInit {

  liquidaciones: Liquidacion[] = [];
  resumen: ResumenLiquidacion | null = null;
  trabajadoresPorRol: { [rol: string]: Trabajador[] } = {};
  tarifas: { [rol: string]: number } = {};
  roles: string[] = ['Apoyos', 'Limpieza', 'Clasificado', 'Envasado'];

  mostrarFormulario = false;
  rolSeleccionado = '';
  trabajadorSeleccionado: number | null = null;
  kilosProcesados: number | null = null;

  cargando = true;
  mensaje = '';
  tipoMensaje: 'exito' | 'error' | '' = '';

  constructor(
    private liquidacionService: LiquidacionService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando = true;
    this.liquidacionService.obtenerDatos().subscribe({
      next: (datos: DatosLiquidacion) => {
        this.liquidaciones = datos.liquidaciones;
        this.resumen = datos.resumen;
        this.trabajadoresPorRol = datos.trabajadoresPorRol;
        this.tarifas = datos.tarifas;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando liquidaciones:', err);
        this.cargando = false;
        this.mostrarMensaje('Error al cargar los datos. Verifica la conexión.', 'error');
      },
    });
  }

  get trabajadoresDelRol(): Trabajador[] {
    return this.trabajadoresPorRol[this.rolSeleccionado] || [];
  }

  get tarifaDelRol(): number {
    return this.tarifas[this.rolSeleccionado] || 0;
  }

  get montoEstimado(): number {
    if (!this.kilosProcesados || !this.rolSeleccionado) return 0;
    return this.kilosProcesados * this.tarifaDelRol;
  }

  registrar(): void {
    if (!this.trabajadorSeleccionado || !this.kilosProcesados || this.kilosProcesados <= 0) {
      this.mostrarMensaje('Selecciona un trabajador e ingresa los kilos procesados.', 'error');
      return;
    }
    this.liquidacionService.registrar(this.trabajadorSeleccionado, this.kilosProcesados).subscribe({
      next: () => {
        this.resetFormulario();
        this.mostrarMensaje('Liquidación registrada correctamente.', 'exito');
        this.cargarDatos();
      },
      error: (err) => {
        console.error('Error registrando liquidación:', err);
        this.mostrarMensaje('Error al registrar la liquidación.', 'error');
      },
    });
  }

  aprobar(id: number): void {
    this.liquidacionService.aprobar(id).subscribe({
      next: () => {
        this.mostrarMensaje('Pago aprobado correctamente.', 'exito');
        this.cargarDatos();
      },
      error: (err) => {
        console.error('Error aprobando liquidación:', err);
        this.mostrarMensaje('Error al aprobar el pago.', 'error');
      },
    });
  }

  resetFormulario(): void {
    this.rolSeleccionado = '';
    this.trabajadorSeleccionado = null;
    this.kilosProcesados = null;
    this.mostrarFormulario = false;
  }

  mostrarMensaje(texto: string, tipo: 'exito' | 'error'): void {
    this.mensaje = texto;
    this.tipoMensaje = tipo;
    this.cdr.detectChanges();
    setTimeout(() => {
      this.mensaje = '';
      this.tipoMensaje = '';
      this.cdr.detectChanges();
    }, 4000);
  }
}
