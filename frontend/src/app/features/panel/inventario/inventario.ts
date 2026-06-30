import {Component, OnInit, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-inventario',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './inventario.html',
  styleUrl: './inventario.scss'
})
export class InventarioComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);

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

  mensajeExito = signal<string>('');
  mensajeError = signal<string>('');

  ngOnInit() {
    this.cargarDatosDashboard();
  }

  cargarDatosDashboard() {
    this.http.get<any>('http://localhost:8080/api/supervisor/inventario/dashboard')
      .subscribe({
        next: (res) => {
          this.historial.set(res.historial);
          this.metricas.set(res.metricas);
          this.destinosDisponibles.set(res.destinos);
          this.lotesDisponibles.set(res.lotesDisponibles);
        },
        error: (err) => console.error(err)
      });
  }

  registrarIngresoLote() {
    if (this.ingresoForm.valid) {
      this.mensajeError.set('');
      this.http.post('http://localhost:8080/api/supervisor/inventario/ingreso-lote', this.ingresoForm.value)
        .subscribe({
          next: () => {
            this.mensajeExito.set('Lote ingresado a almacen correctamente.');
            this.ingresoForm.reset();
            this.cargarDatosDashboard();
            setTimeout(() => this.mensajeExito.set(''), 3000);
          },
          error: (err) => {
            this.mensajeError.set(err.error?.error || 'Error al ingresar el lote.');
          }
        });
    } else {
      this.ingresoForm.markAllAsTouched();
    }
  }

  registrarDistribucion() {
    if (this.inventarioForm.valid) {
      this.mensajeError.set('');
      this.http.post('http://localhost:8080/api/supervisor/inventario/registrar', this.inventarioForm.value)
        .subscribe({
          next: () => {
            this.mensajeExito.set('Distribucion registrada exitosamente.');
            this.inventarioForm.reset({loteReferencia: '', destino: ''});
            this.cargarDatosDashboard();
            setTimeout(() => this.mensajeExito.set(''), 3000);
          },
          error: (err) => {
            this.mensajeError.set(err.error?.error || 'Error al registrar distribucion.');
          }
        });
    } else {
      this.inventarioForm.markAllAsTouched();
    }
  }
}
