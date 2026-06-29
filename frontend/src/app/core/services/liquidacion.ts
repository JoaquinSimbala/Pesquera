import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
}

export interface DatosLiquidacion {
  liquidaciones: Liquidacion[];
  resumen: ResumenLiquidacion;
  trabajadoresPorRol: { [rol: string]: Trabajador[] };
  tarifas: { [rol: string]: number };
}

@Injectable({ providedIn: 'root' })
export class LiquidacionService {

  private readonly URL_BASE = 'http://localhost:8080/api/liquidaciones';

  constructor(private http: HttpClient) {}

  obtenerDatos(): Observable<DatosLiquidacion> {
    return this.http.get<DatosLiquidacion>(this.URL_BASE, { withCredentials: true });
  }

  registrar(trabajadorId: number, kilosProcesados: number): Observable<any> {
    return this.http.post(
      `${this.URL_BASE}/registrar`,
      { trabajadorId, kilosProcesados },
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

  registrarLote(trabajadores: { trabajadorId: number; kilosProcesados: number }[]): Observable<any> {
    return this.http.post(
      `${this.URL_BASE}/registrar-lote`,
      { trabajadores },
      { withCredentials: true }
    );
  }
}
