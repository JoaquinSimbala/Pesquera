import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CalidadService, ControlCalidad, CalidadMetricas } from '../../../core/services/calidad';

@Component({
  selector: 'app-calidad',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './calidad.html',
  styleUrl: './calidad.scss',
})
export class Calidad implements OnInit {
  private fb = inject(FormBuilder);
  private calidadService = inject(CalidadService);

  form: FormGroup = this.fb.group({
    loteReferencia: ['', [Validators.required]],
    temperatura: [null, [Validators.required, Validators.min(-10.0), Validators.max(30.0)]],
    ph: [null, [Validators.required, Validators.min(0.0), Validators.max(14.0)]],
    higienePersonal: [null, [Validators.required]],
    limpiezaEquipos: [null, [Validators.required]],
    estadoHaccp: ['', [Validators.required]],
    observaciones: ['']
  });

  metricas: CalidadMetricas | null = null;
  historial: ControlCalidad[] = [];
  isLoading = false;
  isSaving = false;
  errorMessage = '';
  successMessage = '';

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos() {
    this.isLoading = true;
    this.calidadService.getMetricas().subscribe({
      next: (m) => {
        this.metricas = m;
        this.calidadService.getHistorial().subscribe({
          next: (h) => {
            this.historial = h;
            this.isLoading = false;
          },
          error: (err) => {
            this.errorMessage = 'Error al cargar el historial.';
            this.isLoading = false;
          }
        });
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar las métricas.';
        this.isLoading = false;
      }
    });
  }

  onSubmit() {
    if (this.form.invalid) {
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Convert values if necessary
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
      next: (res) => {
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
        this.cargarDatos();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al guardar el control sanitario.';
        this.isSaving = false;
      }
    });
  }
}
