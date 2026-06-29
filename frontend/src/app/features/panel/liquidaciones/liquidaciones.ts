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

  // ─── Datos del backend ──────────────────────────────────────────────────────
  liquidaciones: Liquidacion[] = [];
  resumen: ResumenLiquidacion | null = null;
  trabajadoresPorRol: { [rol: string]: Trabajador[] } = {};
  tarifas: { [rol: string]: number } = {};
  roles: string[] = ['Apoyos', 'Limpieza', 'Clasificado', 'Envasado'];

  // ─── Estado del formulario de registro manual ───────────────────────────────
  mostrarFormulario = false;
  rolSeleccionado = '';
  trabajadorSeleccionado: number | null = null;
  kilosProcesados: number | null = null;

  // ─── Estado de la pantalla ──────────────────────────────────────────────────
  cargando = false;
  mensaje = '';
  tipoMensaje: 'exito' | 'error' | '' = '';

  constructor(
    private liquidacionService: LiquidacionService,
    private cdr: ChangeDetectorRef
  ) {}

  // Se ejecuta automáticamente al abrir la pantalla
  ngOnInit(): void {
    this.cargarDatos();
  }

  // Pide al backend todos los datos del módulo en una sola llamada
  cargarDatos(): void {
    this.cargando = true;
    this.liquidacionService.obtenerDatos().subscribe({
      next: (datos: DatosLiquidacion) => {
        this.liquidaciones = datos.liquidaciones;
        this.resumen = datos.resumen;
        this.trabajadoresPorRol = datos.trabajadoresPorRol;
        this.tarifas = datos.tarifas;
        this.cargando = false;
      },
      error: () => {
        this.mostrarMensaje('Error al cargar los datos. Verifica la conexión.', 'error');
        this.cargando = false;
      },
    });
  }

  // ─── Getters para el formulario (se recalculan automáticamente) ─────────────

  // Retorna los trabajadores disponibles según el rol elegido en el select
  get trabajadoresDelRol(): Trabajador[] {
    return this.trabajadoresPorRol[this.rolSeleccionado] || [];
  }

  // Retorna la tarifa por kilo según el rol seleccionado
  get tarifaDelRol(): number {
    return this.tarifas[this.rolSeleccionado] || 0;
  }

  // Calcula el monto estimado en tiempo real mientras el usuario escribe los kilos
  get montoEstimado(): number {
    if (!this.kilosProcesados || !this.rolSeleccionado) return 0;
    return this.kilosProcesados * this.tarifaDelRol;
  }

  // ─── Acciones ───────────────────────────────────────────────────────────────

  registrar(): void {
    if (!this.trabajadorSeleccionado || !this.kilosProcesados || this.kilosProcesados <= 0) {
      this.mostrarMensaje('Selecciona un trabajador e ingresa los kilos procesados.', 'error');
      return;
    }

    this.liquidacionService.registrar(this.trabajadorSeleccionado, this.kilosProcesados).subscribe({
      next: () => {
        this.mostrarMensaje('Liquidación registrada correctamente.', 'exito');
        this.resetFormulario();
        this.cargarDatos(); // Recarga la tabla para mostrar el nuevo registro
      },
      error: () => {
        this.mostrarMensaje('Error al registrar la liquidación.', 'error');
      },
    });
  }

  aprobar(id: number): void {
    this.liquidacionService.aprobar(id).subscribe({
      next: () => {
        this.mostrarMensaje('Pago aprobado correctamente.', 'exito');
        this.cargarDatos(); // Recarga para reflejar el cambio de estado en la fila
      },
      error: () => {
        this.mostrarMensaje('Error al aprobar el pago.', 'error');
      },
    });
  }

  // Limpia los campos del formulario y lo oculta
  resetFormulario(): void {
    this.rolSeleccionado = '';
    this.trabajadorSeleccionado = null;
    this.kilosProcesados = null;
    this.mostrarFormulario = false;
  }

  // Muestra un mensaje temporal que desaparece solo después de 4 segundos.
  // Se llama a cdr.markForCheck() porque el app usa modo zoneless y setTimeout
  // no dispara la detección de cambios automáticamente.
  mostrarMensaje(texto: string, tipo: 'exito' | 'error'): void {
    this.mensaje = texto;
    this.tipoMensaje = tipo;
    this.cdr.markForCheck();
    setTimeout(() => {
      this.mensaje = '';
      this.tipoMensaje = '';
      this.cdr.markForCheck();
    }, 4000);
  }
}
