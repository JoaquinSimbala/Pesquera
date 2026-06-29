import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  CostosService,
  Costo,
  ResumenCostos,
  DatosCostos,
  NuevoCosto,
} from '../../../core/services/costos';

@Component({
  selector: 'app-costos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './costos.html',
  styleUrl: './costos.scss',
})
export class Costos implements OnInit {

  // ─── Datos del backend ──────────────────────────────────────────────────────
  costos: Costo[] = [];
  resumen: ResumenCostos | null = null;
  categorias: string[] = [];

  // ─── Estado del formulario de registro ─────────────────────────────────────
  mostrarFormulario = false;
  formCategoria = '';
  formConcepto = '';
  formMonto: number | null = null;
  formFecha = new Date().toISOString().split('T')[0]; // Fecha de hoy como valor inicial
  formDescripcion = '';

  // ─── Estado de la pantalla ──────────────────────────────────────────────────
  cargando = false;
  mensaje = '';
  tipoMensaje: 'exito' | 'error' | '' = '';

  constructor(
    private costosService: CostosService,
    private cdr: ChangeDetectorRef
  ) {}

  // Se ejecuta automáticamente al abrir la pantalla
  ngOnInit(): void {
    this.cargarDatos();
  }

  // Pide al backend todos los datos del módulo en una sola llamada
  cargarDatos(): void {
    this.cargando = true;
    this.costosService.obtenerDatos().subscribe({
      next: (datos: DatosCostos) => {
        this.costos = datos.costos;
        this.resumen = datos.resumen;
        this.categorias = datos.categorias;
        this.cargando = false;
      },
      error: () => {
        this.mostrarMensaje('Error al cargar los datos. Verifica la conexión.', 'error');
        this.cargando = false;
      },
    });
  }

  // Convierte el objeto { Insumos: 500, Servicios: 200 } en un array para poder
  // usarlo con *ngFor en el HTML: [{ nombre: 'Insumos', monto: 500 }, ...]
  get categoriasResumen(): { nombre: string; monto: number }[] {
    if (!this.resumen) return [];
    return Object.entries(this.resumen.porCategoria).map(([nombre, monto]) => ({
      nombre,
      monto,
    }));
  }

  // ─── Acciones ───────────────────────────────────────────────────────────────

  registrar(): void {
    if (!this.formCategoria || !this.formConcepto || !this.formMonto || this.formMonto <= 0) {
      this.mostrarMensaje('Completa los campos obligatorios: categoría, concepto y monto.', 'error');
      return;
    }

    const nuevoCosto: NuevoCosto = {
      categoria: this.formCategoria,
      concepto: this.formConcepto,
      monto: this.formMonto,
      fechaCosto: this.formFecha,
      descripcion: this.formDescripcion,
    };

    this.costosService.registrar(nuevoCosto).subscribe({
      next: () => {
        this.mostrarMensaje('Costo registrado correctamente.', 'exito');
        this.resetFormulario();
        this.cargarDatos(); // Recarga la tabla para mostrar el nuevo registro
      },
      error: () => {
        this.mostrarMensaje('Error al registrar el costo.', 'error');
      },
    });
  }

  // Limpia los campos del formulario y lo oculta
  resetFormulario(): void {
    this.formCategoria = '';
    this.formConcepto = '';
    this.formMonto = null;
    this.formFecha = new Date().toISOString().split('T')[0];
    this.formDescripcion = '';
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
