import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';

@Component({
  selector: 'app-carga',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './carga.html',
  styleUrl: './carga.scss',
})
export class CargaComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);

  calculoForm: FormGroup = this.fb.group({
    kilos: ['', [Validators.required, Validators.min(1), Validators.max(1000000)]],
    tiempoObjetivo: ['', [Validators.required, Validators.min(1), Validators.max(24)]]
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
          error: (err) => {
            console.error(err);
            alert('Error al conectar con el servidor.');
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
          tiempo: this.calculoForm.value.tiempoObjetivo
        }
      });
    }
  }
}
