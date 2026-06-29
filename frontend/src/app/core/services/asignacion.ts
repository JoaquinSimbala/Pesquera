import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TrabajadorAsignado {
  id: number;
  nombreCompleto: string;
  dni: string;
  rendimiento: number;
}

export interface AsignacionResponse {
  deficitPersonal: boolean;
  horasRecomendadas: number;
  asignaciones: { [key: string]: TrabajadorAsignado[] };
}

export interface CalculoCargaRequest {
  kilos: number;
  tiempoObjetivo: number;
}

@Injectable({
  providedIn: 'root'
})
export class AsignacionService {
  private readonly API_URL = 'http://localhost:8080/api/asignacion';

  constructor(private http: HttpClient) {}

  generarAsignacion(calculo: CalculoCargaRequest): Observable<AsignacionResponse> {
    return this.http.post<AsignacionResponse>(`${this.API_URL}/generar`, calculo, {
      withCredentials: true
    });
  }
}
