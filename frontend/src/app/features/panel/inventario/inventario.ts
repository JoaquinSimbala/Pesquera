import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { DialogoService } from '../../../core/services/dialogo';
import { CustomSelect, SelectOption } from '../../../core/components/custom-select/custom-select';

@Component({
  selector: 'app-inventario',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CustomSelect],
  templateUrl: './inventario.html',
  styleUrl: './inventario.scss'
})
export class InventarioComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private dialogoService = inject(DialogoService);

  ingresoForm: FormGroup = this.fb.group({
    codigoLote: ['', Validators.required],
    kilosIniciales: ['', [Validators.required, Validators.min(1)]]
  });

  inventarioForm: FormGroup = this.fb.group({
    loteReferencia: ['', Validators.required],
    kilosTotales: ['', [Validators.required, Validators.min(0.1)]],
    destino: ['', Validators.required]
  });

  historial = signal<any[]>([]);
  metricas = signal<any>(null);
  destinosDisponibles = signal<string[]>([]);
  lotesDisponibles = signal<{ [key: string]: number }>({});
  lotesKeys = signal<string[]>([]);

  
  paginaActual = signal<number>(0);
  elementosPorPagina = 10;
  totalElementos = signal<number>(0);
  totalPaginas = signal<number>(0);

  lotesOptions = computed<SelectOption[]>(() => {
    const keys = this.lotesKeys();
    const disponibles = this.lotesDisponibles();
    return keys.map(key => ({
      value: key,
      label: `${key} (Disp: ${disponibles[key]} kg)`
    }));
  });

  destinosOptions = computed<SelectOption[]>(() => {
    return this.destinosDisponibles().map(dest => ({
      value: dest,
      label: dest
    }));
  });

  ngOnInit() {
    this.cargarDatosDashboard();
  }

  cargarDatosDashboard() {
    this.http.get<any>(`http://localhost:8080/api/inventario/dashboard?page=${this.paginaActual()}&size=${this.elementosPorPagina}`)
      .subscribe({
        next: (res) => {
          this.historial.set(res.historial);
          this.metricas.set(res.metricas);
          this.destinosDisponibles.set(res.destinos);
          this.lotesDisponibles.set(res.lotesDisponibles);
          this.lotesKeys.set(Object.keys(res.lotesDisponibles || {}));
          this.totalPaginas.set(res.totalPages || 0);
          this.totalElementos.set(res.totalElements || 0);
        },
        error: () => this.dialogoService.error('Error de Carga', 'No se pudo cargar el inventario del servidor.')
      });
  }

  registrarIngresoLote() {
    if (this.ingresoForm.valid) {
      this.http.post('http://localhost:8080/api/inventario/ingreso-lote', this.ingresoForm.value)
        .subscribe({
          next: () => {
            this.dialogoService.exito('Lote Ingresado', 'El lote fue ingresado al almacén correctamente.');
            this.ingresoForm.reset();
            this.paginaActual.set(0);
            this.cargarDatosDashboard();
          },
          error: (err) => {
            this.dialogoService.error('Error de Ingreso', err.error?.error || 'Error al ingresar el lote.');
          }
        });
    } else {
      this.ingresoForm.markAllAsTouched();
    }
  }

  registrarDistribucion() {
    if (this.inventarioForm.valid) {
      this.http.post('http://localhost:8080/api/inventario/registrar', this.inventarioForm.value)
        .subscribe({
          next: () => {
            this.dialogoService.exito('Distribución Registrada', 'La distribución fue registrada exitosamente.');
            this.inventarioForm.reset({ loteReferencia: '', destino: '' });
            this.paginaActual.set(0);
            this.cargarDatosDashboard();
          },
          error: (err) => {
            this.dialogoService.error('Error de Distribución', err.error?.error || 'Error al registrar la distribución.');
          }
        });
    } else {
      this.inventarioForm.markAllAsTouched();
    }
  }

  siguientePagina() {
    if (this.paginaActual() < this.totalPaginas() - 1) {
      this.paginaActual.update(p => p + 1);
      this.cargarDatosDashboard();
    }
  }

  anteriorPagina() {
    if (this.paginaActual() > 0) {
      this.paginaActual.update(p => p - 1);
      this.cargarDatosDashboard();
    }
  }
}
