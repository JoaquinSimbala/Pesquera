import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { DialogoService } from '../../../core/services/dialogo';
import { CustomSelect, SelectOption } from '../../../core/components/custom-select/custom-select';

@Component({
  selector: 'app-carga',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CustomSelect],
  templateUrl: './carga.html',
  styleUrl: './carga.scss',
})
export class CargaComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private dialogoService = inject(DialogoService);

  especies = ['Pulpo', 'Atún', 'Caballa'];

  get especiesOptions(): SelectOption[] {
    return this.especies.map(esp => ({ value: esp, label: esp }));
  }

  calculoForm: FormGroup = this.fb.group({
    kilos: ['', [Validators.required, Validators.min(1), Validators.max(1000000)]],
    tiempoObjetivo: ['', [Validators.required, Validators.min(1), Validators.max(24)]],
    especie: ['Pulpo', [Validators.required]]
  });

  resultado: any = null;
  calculado: boolean = false;

  calcularRequerimiento() {
    if (this.calculoForm.valid) {
      this.http.post<any>('http://localhost:8080/api/gerente/calcular', this.calculoForm.value)
        .subscribe({
          next: (res) => {
            this.resultado = res;
            this.calculado = true;
          },
          error: () => {
            this.dialogoService.error('Error de Conexión', 'No se pudo conectar con el servidor para calcular el requerimiento.');
          }
        });
    } else {
      this.calculoForm.markAllAsTouched();
    }
  }

  generarAsignacion() {
    if (this.calculoForm.valid) {
      this.router.navigate(['/panel/asignacion'], {
        queryParams: {
          kilos: this.calculoForm.value.kilos,
          tiempo: this.calculoForm.value.tiempoObjetivo,
          especie: this.calculoForm.value.especie
        }
      });
    }
  }
}
