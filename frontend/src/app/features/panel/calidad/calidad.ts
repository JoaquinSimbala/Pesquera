import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { CalidadService, ControlCalidad, CalidadMetricas } from '../../../core/services/calidad';

@Component({
  selector: 'app-calidad',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './calidad.html',
  styleUrl: './calidad.scss',
})
export class Calidad implements OnInit {

  form: FormGroup;
  metricas: CalidadMetricas | null = null;
  historial: ControlCalidad[] = [];
  isLoading = false;
  isSaving = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private calidadService: CalidadService,
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
    forkJoin({
      metricas: this.calidadService.getMetricas(),
      historial: this.calidadService.getHistorial()
    }).subscribe({
      next: (datos) => {
        this.metricas = datos.metricas;
        this.historial = datos.historial;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Error al cargar los datos de calidad.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSaving = true;
    this.errorMessage = '';
    this.successMessage = '';

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
        this.successMessage = 'Control de calidad registrado correctamente.';
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
        this.cdr.detectChanges();
        this.cargarDatos();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al guardar el control sanitario.';
        this.isSaving = false;
        this.cdr.detectChanges();
      }
    });
  }
}
