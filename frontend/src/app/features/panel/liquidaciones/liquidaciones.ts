import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  LiquidacionService,
  Liquidacion,
  ResumenLiquidacion,
  Trabajador,
  DatosLiquidacion,
} from '../../../core/services/liquidacion';
import { DialogoService } from '../../../core/services/dialogo';
import { CustomSelect, SelectOption } from '../../../core/components/custom-select/custom-select';

@Component({
  selector: 'app-liquidaciones',
  standalone: true,
  imports: [CommonModule, FormsModule, CustomSelect],
  templateUrl: './liquidaciones.html',
  styleUrl: './liquidaciones.scss',
})
export class Liquidaciones implements OnInit {

  liquidaciones: Liquidacion[] = [];
  resumen: ResumenLiquidacion | null = null;
  trabajadoresPorRol: { [rol: string]: Trabajador[] } = {};
  tarifas: { [rol: string]: number } = {};
  especies: any[] = [];
  roles: string[] = ['Apoyos', 'Limpieza', 'Clasificado', 'Envasado'];

  
  esLoteAsignado = false;
  especie = 'Pulpo';
  trabajadoresLote: any[] = [];
  kilosPorEtapa: { [rol: string]: number | null } = {};

  
  mostrarFormulario = false;
  rolSeleccionado = '';
  trabajadorSeleccionado: number | null = null;
  especieSeleccionada: number | null = null;
  kilosProcesados: number | null = null;
  tarifasManuales: { [rol: string]: number } = {};

  cargando = true;

  
  paginaActual = 0;
  elementosPorPagina = 10;
  totalElementos = 0;
  totalPaginas = 0;

  
  get especiesOptions(): SelectOption[] {
    return this.especies.map(esp => ({ value: esp.id, label: esp.nombre }));
  }

  get rolesOptions(): SelectOption[] {
    return this.roles.map(rol => ({ value: rol, label: rol }));
  }

  get trabajadoresOptions(): SelectOption[] {
    return this.trabajadoresDelRol.map(t => ({ value: t.id, label: `${t.nombre} — ${t.dni}` }));
  }

  constructor(
    private liquidacionService: LiquidacionService,
    private dialogoService: DialogoService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const state = history.state;
    if (state && state.trabajadores && state.especie) {
      this.esLoteAsignado = true;
      this.especie = state.especie;
      this.trabajadoresLote = state.trabajadores.map((t: any) => ({
        trabajadorId: t.trabajadorId,
        nombreCompleto: t.nombreCompleto,
        rolOperativo: t.rolOperativo,
        kilosProcesados: 0,
        montoTotal: 0
      }));
      this.kilosPorEtapa = {};
    }

    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando = true;
    this.liquidacionService.obtenerDatos(this.especie, this.paginaActual, this.elementosPorPagina).subscribe({
      next: (datos: DatosLiquidacion) => {
        this.liquidaciones = datos.liquidaciones;
        this.resumen = datos.resumen;
        this.trabajadoresPorRol = datos.trabajadoresPorRol;
        this.tarifas = datos.tarifas;
        this.especies = datos.especies || [];
        this.totalPaginas = datos.totalPages || 0;
        this.totalElementos = datos.totalElements || 0;
        
        if (this.esLoteAsignado && this.trabajadoresLote.length > 0) {
          for (const item of this.trabajadoresLote) {
            this.recalcularFila(item);
          }
        }

        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando liquidaciones:', err);
        this.cargando = false;
        this.dialogoService.error('Error de Carga', 'Error al cargar los datos de liquidación del servidor.');
      },
    });
  }

  recalcularFila(item: any): void {
    const tarifa = this.tarifas[item.rolOperativo] || 0;
    const kilos = item.kilosProcesados || 0;
    item.montoTotal = Math.round(kilos * tarifa * 100) / 100;
  }

  get rolesEnLote(): string[] {
    const rolesSet = new Set<string>();
    for (const t of this.trabajadoresLote) {
      rolesSet.add(t.rolOperativo);
    }
    const order = ['Apoyos', 'Limpieza', 'Clasificado', 'Envasado'];
    return Array.from(rolesSet).sort((a, b) => order.indexOf(a) - order.indexOf(b));
  }

  obtenerTrabajadoresPorRolEnLote(rol: string): any[] {
    return this.trabajadoresLote.filter(t => t.rolOperativo === rol);
  }

  distribuirKilosPorEtapa(rol: string): void {
    const kilosTotales = this.kilosPorEtapa[rol];
    if (kilosTotales === null || kilosTotales === undefined || kilosTotales <= 0) {
      return;
    }
    const trabajadores = this.obtenerTrabajadoresPorRolEnLote(rol);
    if (trabajadores.length === 0) return;

    const kilosIndiv = Math.round((kilosTotales / trabajadores.length) * 100) / 100;
    for (const item of trabajadores) {
      item.kilosProcesados = kilosIndiv;
      this.recalcularFila(item);
    }
    this.cdr.detectChanges();
  }

  actualizarTarifaManual(): void {
    if (!this.especieSeleccionada) return;
    const espObj = this.especies.find(e => e.id === this.especieSeleccionada);
    if (!espObj) return;

    this.liquidacionService.obtenerDatos(espObj.nombre).subscribe({
      next: (datos) => {
        this.tarifasManuales = datos.tarifas || {};
        this.cdr.detectChanges();
      }
    });
  }

  get trabajadoresDelRol(): Trabajador[] {
    return this.trabajadoresPorRol[this.rolSeleccionado] || [];
  }

  get tarifaDelRolManual(): number {
    if (this.especieSeleccionada && Object.keys(this.tarifasManuales).length > 0) {
      return this.tarifasManuales[this.rolSeleccionado] || 0;
    }
    return this.tarifas[this.rolSeleccionado] || 0;
  }

  get montoEstimado(): number {
    if (!this.kilosProcesados || !this.rolSeleccionado) return 0;
    return this.kilosProcesados * this.tarifaDelRolManual;
  }

  registrar(): void {
    if (!this.especieSeleccionada) {
      this.dialogoService.advertencia('Falta Selección', 'Debes seleccionar una especie procesada.');
      return;
    }
    if (!this.trabajadorSeleccionado || !this.kilosProcesados || this.kilosProcesados <= 0) {
      this.dialogoService.advertencia('Datos Incompletos', 'Selecciona un trabajador e ingresa los kilos procesados.');
      return;
    }
    this.liquidacionService.registrar(this.trabajadorSeleccionado, this.kilosProcesados, this.especieSeleccionada).subscribe({
      next: () => {
        this.resetFormulario();
        this.dialogoService.exito('Registro Exitoso', 'La liquidación individual ha sido registrada correctamente.');
        this.cargarDatos();
      },
      error: (err) => {
        console.error('Error registrando liquidación:', err);
        this.dialogoService.error('Error de Registro', 'No se pudo guardar la liquidación en el servidor.');
      },
    });
  }

  registrarLoteAsignado(): void {
    const invalidos = this.trabajadoresLote.some(t => !t.kilosProcesados || t.kilosProcesados <= 0);
    if (invalidos) {
      this.dialogoService.advertencia('Validación de Kilos', 'Todos los trabajadores del lote deben registrar kilos procesados válidos.');
      return;
    }

    const payload = this.trabajadoresLote.map(t => ({
      trabajadorId: t.trabajadorId,
      kilosProcesados: t.kilosProcesados
    }));

    this.liquidacionService.registrarLote(this.especie, payload).subscribe({
      next: () => {
        this.esLoteAsignado = false;
        this.trabajadoresLote = [];
        this.kilosPorEtapa = {};
        this.dialogoService.exito('Lote Procesado', 'Se han registrado todas las liquidaciones del lote y liberado al personal.');
        this.cargarDatos();
      },
      error: (err) => {
        console.error('Error al registrar lote de liquidaciones:', err);
        this.dialogoService.error('Error de Lote', 'Error al procesar el lote de liquidaciones.');
      }
    });
  }

  cancelarLote(): void {
    this.dialogoService.confirmar(
      'Cancelar Cálculo',
      '¿Está seguro de cancelar el cálculo de este lote? Se perderán todos los datos ingresados.',
      () => {
        this.esLoteAsignado = false;
        this.trabajadoresLote = [];
        this.kilosPorEtapa = {};
        this.cdr.detectChanges();
      }
    );
  }

  aprobar(id: number): void {
    this.liquidacionService.aprobar(id).subscribe({
      next: () => {
        this.dialogoService.exito('Pago Aprobado', 'La liquidación ha sido marcada como aprobada para el pago.');
        this.cargarDatos();
      },
      error: (err) => {
        console.error('Error aprobando liquidación:', err);
        this.dialogoService.error('Error de Aprobación', 'No se pudo realizar la aprobación del pago.');
      },
    });
  }

  resetFormulario(): void {
    this.rolSeleccionado = '';
    this.trabajadorSeleccionado = null;
    this.especieSeleccionada = null;
    this.kilosProcesados = null;
    this.tarifasManuales = {};
    this.mostrarFormulario = false;
  }

  siguientePagina(): void {
    if (this.paginaActual < this.totalPaginas - 1) {
      this.paginaActual++;
      this.cargarDatos();
    }
  }

  anteriorPagina(): void {
    if (this.paginaActual > 0) {
      this.paginaActual--;
      this.cargarDatos();
    }
  }
}
