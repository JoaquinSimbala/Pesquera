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

  costos: Costo[] = [];
  resumen: ResumenCostos | null = null;
  categorias: string[] = [];

  mostrarFormulario = false;
  formCategoria = '';
  formConcepto = '';
  formMonto: number | null = null;
  formFecha = '';
  formDescripcion = '';

  cargando = false;
  mensaje = '';
  tipoMensaje: 'exito' | 'error' | '' = '';

  constructor(
    private costosService: CostosService,
    private cdr: ChangeDetectorRef
  ) {
    // Inicializamos la fecha aquí para que sea seguro
    const hoy = new Date();
    const anio = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');
    this.formFecha = `${anio}-${mes}-${dia}`;
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando = true;
    this.costosService.obtenerDatos().subscribe({
      next: (datos: DatosCostos) => {
        this.costos = datos.costos;
        this.resumen = datos.resumen;
        this.categorias = datos.categorias;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando costos:', err);
        this.cargando = false;
        this.mostrarMensaje('Error al cargar los datos. Verifica la conexión.', 'error');
      },
    });
  }

  get categoriasResumen(): { nombre: string; monto: number }[] {
    if (!this.resumen) return [];
    return Object.entries(this.resumen.porCategoria).map(([nombre, monto]) => ({
      nombre,
      monto,
    }));
  }

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
        this.resetFormulario();
        this.mostrarMensaje('Costo registrado correctamente.', 'exito');
        this.cargarDatos();
      },
      error: (err) => {
        console.error('Error registrando costo:', err);
        this.mostrarMensaje('Error al registrar el costo.', 'error');
      },
    });
  }

  resetFormulario(): void {
    this.formCategoria = '';
    this.formConcepto = '';
    this.formMonto = null;
    this.formDescripcion = '';
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
