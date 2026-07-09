import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { CalidadService, ControlCalidad, CalidadMetricas } from '../../../core/services/calidad';
import { DialogoService } from '../../../core/services/dialogo';
import { CustomSelect, SelectOption } from '../../../core/components/custom-select/custom-select';

@Component({
  selector: 'app-calidad',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CustomSelect],
  templateUrl: './calidad.html',
  styleUrl: './calidad.scss',
})
export class Calidad implements OnInit {

  haccpOptions: SelectOption[] = [
    { value: 'APROBADO', label: 'APROBADO' },
    { value: 'CON OBSERVACIONES', label: 'CON OBSERVACIONES' },
    { value: 'RECHAZADO', label: 'RECHAZADO' }
  ];

  cumplimientoOptions: SelectOption[] = [
    { value: true, label: 'Cumple' },
    { value: false, label: 'No Cumple' }
  ];

  form: FormGroup;
  metricas: CalidadMetricas | null = null;
  historial: ControlCalidad[] = [];
  isLoading = false;
  isSaving = false;

  
  paginaActual = 0;
  elementosPorPagina = 10;
  totalElements = 0;
  totalPaginas = 0;

  constructor(
    private fb: FormBuilder,
    private calidadService: CalidadService,
    private dialogoService: DialogoService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      loteReferencia: ['', [Validators.required]],
      temperatura: [null, [Validators.required, Validators.min(-10.0), Validators.max(30.0)]],
      ph: [null, [Validators.required, Validators.min(0.0), Validators.max(14.0)]],
      higienePersonal: [null, [Validators.required]],
      limpiezaEquipos: [null, [Validators.required]],
      estadoHaccp: ['', [Validators.required]],
      observaciones: ['']
    });
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.isLoading = true;
    this.calidadService.getMetricas().subscribe({
      next: (datos) => {
        this.metricas = datos;
        this.cdr.detectChanges();
      },
      error: () => {
        console.error('Error cargando métricas de calidad');
      }
    });
    this.cargarHistorial();
  }

  cargarHistorial(): void {
    this.calidadService.getHistorial(this.paginaActual, this.elementosPorPagina).subscribe({
      next: (res) => {
        this.historial = res.content || [];
        this.totalPaginas = res.totalPages || 0;
        this.totalElements = res.totalElements || 0;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.dialogoService.error('Error de Carga', 'Error al cargar el historial de calidad.');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  siguientePagina(): void {
    if (this.paginaActual < this.totalPaginas - 1) {
      this.paginaActual++;
      this.cargarHistorial();
    }
  }

  anteriorPagina(): void {
    if (this.paginaActual > 0) {
      this.paginaActual--;
      this.cargarHistorial();
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSaving = true;

    const rawVal = this.form.value;
    const cleanForm: ControlCalidad = {
      loteReferencia: rawVal.loteReferencia,
      temperatura: Number(rawVal.temperatura),
      ph: Number(rawVal.ph),
      higienePersonal: rawVal.higienePersonal === 'true' || rawVal.higienePersonal === true,
      limpiezaEquipos: rawVal.limpiezaEquipos === 'true' || rawVal.limpiezaEquipos === true,
      estadoHaccp: rawVal.estadoHaccp,
      observaciones: rawVal.observaciones
    };

    this.calidadService.registrarControl(cleanForm).subscribe({
      next: () => {
        this.dialogoService.exito('Control Registrado', 'Control de calidad registrado correctamente.');
        this.form.reset({
          loteReferencia: '',
          temperatura: null,
          ph: null,
          higienePersonal: null,
          limpiezaEquipos: null,
          estadoHaccp: '',
          observaciones: ''
        });
        this.isSaving = false;
        this.paginaActual = 0;
        this.cdr.detectChanges();
        this.cargarDatos();
      },
      error: (err) => {
        const msg = err.error?.message || 'Error al guardar el control sanitario.';
        this.dialogoService.error('Error al Guardar', msg);
        this.isSaving = false;
        this.cdr.detectChanges();
      }
    });
  }
}
