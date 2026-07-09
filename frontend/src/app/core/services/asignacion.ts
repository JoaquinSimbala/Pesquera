import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';


export interface TrabajadorAsignado {
  id: number;
  nombreCompleto: string;
  dni: string;
  rendimiento: number;
  bajoEstandar?: boolean;
}

export interface AsignacionResponse {
  deficitPersonal: boolean;
  horasRecomendadas: number;
  advertenciaRendimiento?: boolean;
  asignaciones: { [key: string]: TrabajadorAsignado[] };
}

export interface CalculoCargaRequest {
  kilos: number;
  tiempoObjetivo: number;
  especie: string;
}

@Injectable({
  providedIn: 'root'
})
export class AsignacionService {
  private readonly API_URL = `${environment.apiUrl}/asignacion`;

  constructor(private http: HttpClient) {}

  generarAsignacion(calculo: CalculoCargaRequest): Observable<AsignacionResponse> {
    return this.http.post<AsignacionResponse>(`${this.API_URL}/generar`, calculo, {
      withCredentials: true
    });
  }

  guardarAsignacion(calculo: CalculoCargaRequest, trabajadorIds: number[]): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/guardar`, { calculo, trabajadorIds }, {
      withCredentials: true
    });
  }

  obtenerAsignacionesActivas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/activas`, {
      withCredentials: true
    });
  }

  liberarTrabajador(trabajadorId: number, kilos: number): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/liberar/${trabajadorId}`, { kilos }, {
      withCredentials: true
    });
  }
}
