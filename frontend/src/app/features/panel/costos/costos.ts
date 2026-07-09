import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  CostosService, Costo, ResumenCostos, DatosCostos, NuevoCosto,
} from '../../../core/services/costos';
import { DialogoService } from '../../../core/services/dialogo';
import { CustomSelect, SelectOption } from '../../../core/components/custom-select/custom-select';

@Component({
  selector: 'app-costos',
  standalone: true,
  imports: [CommonModule, FormsModule, CustomSelect],
  templateUrl: './costos.html',
  styleUrl: './costos.scss',
})
export class Costos implements OnInit {

  costos: Costo[] = [];
  resumen: ResumenCostos | null = null;
  categorias: string[] = [];

  get categoriasOptions(): SelectOption[] {
    return this.categorias.map(cat => ({ value: cat, label: cat }));
  }

  mostrarFormulario = false;
  formCategoria = '';
  formConcepto = '';
  formMonto: number | null = null;
  formFecha = '';
  formDescripcion = '';
  cargando = false;

  
  paginaActual = 0;
  elementosPorPagina = 10;
  totalElementos = 0;
  totalPaginas = 0;

  constructor(
    private costosService: CostosService,
    private dialogoService: DialogoService,
    private cdr: ChangeDetectorRef
  ) {
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
    this.costosService.obtenerDatos(this.paginaActual, this.elementosPorPagina).subscribe({
      next: (datos: DatosCostos) => {
        this.costos = datos.costos;
        this.resumen = datos.resumen;
        this.categorias = datos.categorias;
        this.totalPaginas = datos.totalPages || 0;
        this.totalElementos = datos.totalElements || 0;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargando = false;
        this.dialogoService.error('Error de Carga', 'Error al cargar los datos. Verifica la conexión.');
      },
    });
  }

  get categoriasResumen(): { nombre: string; monto: number }[] {
    if (!this.resumen) return [];
    return Object.entries(this.resumen.porCategoria).map(([nombre, monto]) => ({ nombre, monto }));
  }

  registrar(): void {
    if (!this.formCategoria || !this.formConcepto || !this.formMonto || this.formMonto <= 0) {
      this.dialogoService.advertencia('Campos Incompletos', 'Completa los campos obligatorios: categoría, concepto y monto.');
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
        this.dialogoService.exito('Costo Registrado', 'El costo operacional fue registrado correctamente.');
        this.paginaActual = 0;
        this.cargarDatos();
      },
      error: () => {
        this.dialogoService.error('Error de Registro', 'Error al registrar el costo.');
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

  siguientePagina(): void {
    if (this.paginaActual < this.totalPaginas - 1) {
      this.paginaActual++;
      this.cargarDatos();
    }
  }

  anteriorPagina(): void {
    if (this.paginaActual > 0) {
      this.paginaActual--;
      this.cargarDatos();
    }
  }
}
