import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Liquidacion {
  id: number;
  trabajadorNombre: string;
  trabajadorRol: string;
  kilosProcesados: number;
  tarifaPorKilo: number;
  montoTotal: number;
  fechaProduccion: string;
  aprobado: boolean;
  fechaRegistro: string;
}

export interface ResumenLiquidacion {
  totalRegistros: number;
  pendientesAprobacion: number;
  montoTotal: number;
  montoAprobado: number;
  montoPendiente: number;
}

export interface Trabajador {
  id: number;
  nombre: string;
  dni: string;
  rolOperativo: string;
}

export interface DatosLiquidacion {
  liquidaciones: Liquidacion[];
  resumen: ResumenLiquidacion;
  trabajadoresPorRol: { [rol: string]: Trabajador[] };
  especies: any[];
  tarifas: { [rol: string]: number };
  totalPages?: number;
  totalElements?: number;
  currentPage?: number;
}

@Injectable({ providedIn: 'root' })
export class LiquidacionService {

  private readonly URL_BASE = `${environment.apiUrl}/liquidaciones`;

  constructor(private http: HttpClient) {}

  obtenerDatos(especie: string = 'Pulpo', page = 0, size = 10): Observable<DatosLiquidacion> {
    return this.http.get<DatosLiquidacion>(`${this.URL_BASE}?especie=${especie}&page=${page}&size=${size}`, { withCredentials: true });
  }

  registrar(trabajadorId: number, kilosProcesados: number, especieId: number): Observable<any> {
    return this.http.post(
      `${this.URL_BASE}/registrar`,
      { trabajadorId, kilosProcesados, especieId },
      { withCredentials: true }
    );
  }

  aprobar(id: number): Observable<any> {
    return this.http.post(
      `${this.URL_BASE}/${id}/aprobar`,
      {},
      { withCredentials: true }
    );
  }

  registrarLote(especie: string, trabajadores: { trabajadorId: number; kilosProcesados: number }[]): Observable<any> {
    return this.http.post(
      `${this.URL_BASE}/registrar-lote`,
      { especie, trabajadores },
      { withCredentials: true }
    );
  }
}
