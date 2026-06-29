import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// ─── Tipos de datos que vienen del backend ───────────────────────────────────

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

// Agrupa todo lo que trae el GET /api/liquidaciones en una sola llamada
export interface DatosLiquidacion {
  liquidaciones: Liquidacion[];
  resumen: ResumenLiquidacion;
  trabajadoresPorRol: { [rol: string]: Trabajador[] };
  tarifas: { [rol: string]: number };
}

// ─── Servicio ─────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class LiquidacionService {

  private readonly URL_BASE = 'http://localhost:8080/api/liquidaciones';

  constructor(private http: HttpClient) {}

  // Carga toda la información del módulo en una sola petición
  obtenerDatos(): Observable<DatosLiquidacion> {
    return this.http.get<DatosLiquidacion>(this.URL_BASE, { withCredentials: true });
  }

  // Registra una nueva liquidación manual con trabajador y kilos
  registrar(trabajadorId: number, kilosProcesados: number): Observable<any> {
    return this.http.post(
      `${this.URL_BASE}/registrar`,
      { trabajadorId, kilosProcesados },
      { withCredentials: true }
    );
  }

  // Aprueba el pago de una liquidación por su ID
  aprobar(id: number): Observable<any> {
    return this.http.post(
      `${this.URL_BASE}/${id}/aprobar`,
      {},
      { withCredentials: true }
    );
  }
}
