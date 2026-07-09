import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { GestionService } from '../../../core/services/gestion';
import { CustomSelect, SelectOption } from '../../../core/components/custom-select/custom-select';

@Component({
  selector: 'app-gestion',
  standalone: true,
  imports: [CommonModule, FormsModule, CustomSelect],
  templateUrl: './gestion.html',
  styleUrls: ['./gestion.scss']
})
export class GestionComponent implements OnInit {
  private authService = inject(AuthService);
  private gestionService = inject(GestionService);
  private router = inject(Router);

  
  dashboardData = signal<any>(null);
  cargando = signal<boolean>(true);
  errorMsg = signal<string>('');

  
  mostrarReporteModal = signal<boolean>(false);
  reporteData = signal<any>(null);
  cargandoReporte = signal<boolean>(false);
  fechaEmision = signal<string>('');

  
  tipoReporte = signal<string>('historico');
  anioReporte = signal<number>(new Date().getFullYear());
  mesReporte = signal<number>(new Date().getMonth() + 1);
  trimestreReporte = signal<number>(Math.floor(new Date().getMonth() / 3) + 1);

  tipoOptions: SelectOption[] = [
    { value: 'historico', label: 'Histórico Completo' },
    { value: 'anio', label: 'Anual (Año Completo)' },
    { value: 'trimestre', label: 'Trimestral (Por Trimestre)' },
    { value: 'mes', label: 'Mensual (Por Mes)' }
  ];

  anioOptions: SelectOption[] = [
    { value: 2024, label: '2024' },
    { value: 2025, label: '2025' },
    { value: 2026, label: '2026' },
    { value: 2027, label: '2027' }
  ];

  trimestreOptions: SelectOption[] = [
    { value: 1, label: '1º Trimestre (Ene-Mar)' },
    { value: 2, label: '2º Trimestre (Abr-Jun)' },
    { value: 3, label: '3º Trimestre (Jul-Sep)' },
    { value: 4, label: '4º Trimestre (Oct-Dic)' }
  ];

  mesOptions: SelectOption[] = [
    { value: 1, label: 'Enero' },
    { value: 2, label: 'Febrero' },
    { value: 3, label: 'Marzo' },
    { value: 4, label: 'Abril' },
    { value: 5, label: 'Mayo' },
    { value: 6, label: 'Junio' },
    { value: 7, label: 'Julio' },
    { value: 8, label: 'Agosto' },
    { value: 9, label: 'Septiembre' },
    { value: 10, label: 'Octubre' },
    { value: 11, label: 'Noviembre' },
    { value: 12, label: 'Diciembre' }
  ];

  ngOnInit() {
    
    if (this.authService.getRole() !== 'SUPERVISOR') {
      this.router.navigate(['/panel/inventario']);
      return;
    }

    this.cargarDashboard();
  }

  cargarDashboard() {
    this.cargando.set(true);
    this.errorMsg.set('');
    this.gestionService.obtenerDashboard(
      this.tipoReporte(),
      this.anioReporte(),
      this.mesReporte(),
      this.trimestreReporte()
    ).subscribe({
      next: (data) => {
        this.dashboardData.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        console.error(err);
        this.errorMsg.set('No se pudieron cargar las métricas de gestión. Intente de nuevo.');
        this.cargando.set(false);
      }
    });
  }

  onTipoChange(value: string) {
    this.tipoReporte.set(value);
    this.cargarDashboard();
  }

  onAnioChange(value: any) {
    const parsed = typeof value === 'string' ? parseInt(value, 10) : value;
    this.anioReporte.set(parsed);
    this.cargarDashboard();
  }

  onMesChange(value: any) {
    const parsed = typeof value === 'string' ? parseInt(value, 10) : value;
    this.mesReporte.set(parsed);
    this.cargarDashboard();
  }

  onTrimestreChange(value: any) {
    const parsed = typeof value === 'string' ? parseInt(value, 10) : value;
    this.trimestreReporte.set(parsed);
    this.cargarDashboard();
  }

  abrirReporte() {
    this.mostrarReporteModal.set(true);
    this.cargandoReporte.set(true);
    this.reporteData.set(null);

    const now = new Date();
    const pad = (n: number) => n < 10 ? '0' + n : n;
    this.fechaEmision.set(`${pad(now.getDate())}/${pad(now.getMonth() + 1)}/${now.getFullYear()} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`);

    this.gestionService.obtenerReporte(
      this.tipoReporte(),
      this.anioReporte(),
      this.mesReporte(),
      this.trimestreReporte()
    ).subscribe({
      next: (res) => {
        this.reporteData.set(res);
        this.cargandoReporte.set(false);
      },
      error: (err) => {
        console.error(err);
        this.cargandoReporte.set(false);
      }
    });
  }

  cerrarReporte() {
    this.mostrarReporteModal.set(false);
    this.reporteData.set(null);
  }

  imprimirReporte() {
    const reportContent = document.getElementById('reporte-imprimible')?.innerHTML;
    if (!reportContent) return;

    const printWindow = window.open('', '_blank');
    if (!printWindow) return;

    printWindow.document.write(`
      <html>
        <head>
          <title>Reporte de Negocio y Supervisión</title>
          <style>
            body {
              font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
              color: #2f3e46;
              padding: 40px;
              line-height: 1.6;
              background: #fff;
            }
            .report-header-print {
              border-bottom: 2px solid #61C9A8;
              padding-bottom: 15px;
              margin-bottom: 25px;
              display: flex;
              justify-content: space-between;
              align-items: flex-end;
            }
            .report-title-print h1 {
              font-size: 20px;
              margin: 0 0 5px 0;
              color: #2f3e46;
              font-weight: 800;
              text-transform: uppercase;
              letter-spacing: 0.03em;
            }
            .report-title-print p {
              font-size: 13px;
              margin: 0;
              color: #3a987d;
              font-weight: 600;
            }
            .report-meta-print {
              font-size: 11px;
              color: #6c757d;
              text-align: right;
              line-height: 1.4;
            }
            .report-section-print {
              margin-bottom: 25px;
              page-break-inside: avoid;
            }
            .report-section-print h2 {
              font-size: 15px;
              border-bottom: 1px solid rgba(0,0,0,0.1);
              padding-bottom: 6px;
              margin-bottom: 12px;
              color: #2f3e46;
              text-transform: uppercase;
              letter-spacing: 0.02em;
            }
            .stat-grid-print {
              display: grid;
              grid-template-columns: repeat(3, 1fr);
              gap: 15px;
              margin-bottom: 15px;
            }
            .stat-card-print {
              background: #f8f9fa;
              border: 1px solid #e9ecef;
              border-radius: 6px;
              padding: 12px;
              text-align: center;
            }
            .stat-label-print {
              font-size: 10px;
              text-transform: uppercase;
              color: #6c757d;
              font-weight: 600;
              margin-bottom: 4px;
            }
            .stat-value-print {
              font-size: 18px;
              font-weight: 700;
              color: #2f3e46;
            }
            .stat-value-print.success { color: #52b788; }
            .stat-value-print.warning { color: #d97706; }
            .stat-value-print.danger { color: #ef476f; }
            
            table {
              width: 100%;
              border-collapse: collapse;
              margin-top: 8px;
              font-size: 12px;
            }
            th {
              background: #f1f3f5;
              text-align: left;
              padding: 8px 10px;
              font-weight: 700;
              color: #495057;
              border-bottom: 2px solid #dee2e6;
            }
            td {
              padding: 8px 10px;
              border-bottom: 1px solid #dee2e6;
              color: #2f3e46;
            }
            tr:nth-child(even) td {
              background: #f8f9fa;
            }
            .chart-bar-container {
              display: flex;
              align-items: center;
              gap: 12px;
            }
            .chart-bar-bg {
              flex: 1;
              background: #e9ecef;
              height: 10px;
              border-radius: 5px;
              overflow: hidden;
            }
            .chart-bar-fill {
              background: #61C9A8;
              height: 100%;
              border-radius: 5px;
            }
            .chart-bar-value {
              font-weight: 700;
              min-width: 65px;
              font-size: 11px;
              text-align: right;
            }
            .quality-alert-box {
              background: #fff3cd;
              border: 1px solid #ffeeba;
              color: #856404;
              padding: 12px;
              border-radius: 6px;
              margin-top: 12px;
              font-size: 12px;
            }
            @media print {
              body { padding: 0; }
            }
          </style>
        </head>
        <body>
          ${reportContent}
        </body>
      </html>
    `);
    printWindow.document.close();
    printWindow.focus();
    setTimeout(() => {
      printWindow.print();
      printWindow.close();
    }, 250);
  }

  
  obtenerCategoriasCosto(): string[] {
    const data = this.dashboardData();
    if (!data?.costos?.porCategoria) return [];
    return Object.keys(data.costos.porCategoria);
  }

  obtenerValorCategoriaCosto(cat: string): number {
    return this.dashboardData()?.costos?.porCategoria?.[cat] || 0;
  }

  obtenerPorcentajeCosto(cat: string): number {
    const total = this.dashboardData()?.costos?.totalGeneral || 1;
    const valor = this.obtenerValorCategoriaCosto(cat);
    return (valor / total) * 100;
  }

  obtenerDestinosProduccion(): string[] {
    const data = this.dashboardData();
    if (!data?.produccion?.distribucionDestinos) return [];
    return Object.keys(data.produccion.distribucionDestinos);
  }

  obtenerValorDestino(dest: string): number {
    return this.dashboardData()?.produccion?.distribucionDestinos?.[dest] || 0;
  }

  obtenerPorcentajeDestino(dest: string): number {
    const total = this.dashboardData()?.produccion?.kilosDistribuidos || 1;
    const valor = this.obtenerValorDestino(dest);
    return (valor / total) * 100;
  }

  obtenerReporteCategoriasCosto(): string[] {
    const data = this.reporteData();
    if (!data?.costos?.porCategoria) return [];
    return Object.keys(data.costos.porCategoria);
  }

  obtenerReporteValorCategoriaCosto(cat: string): number {
    return this.reporteData()?.costos?.porCategoria?.[cat] || 0;
  }

  obtenerReportePorcentajeCosto(cat: string): number {
    const total = this.reporteData()?.costos?.totalGeneral || 1;
    const valor = this.obtenerReporteValorCategoriaCosto(cat);
    return (valor / total) * 100;
  }

  obtenerReporteDestinosProduccion(): string[] {
    const data = this.reporteData();
    if (!data?.produccion?.distribucionDestinos) return [];
    return Object.keys(data.produccion.distribucionDestinos);
  }

  obtenerReporteValorDestino(dest: string): number {
    return this.reporteData()?.produccion?.distribucionDestinos?.[dest] || 0;
  }

  obtenerReportePorcentajeDestino(dest: string): number {
    const total = this.reporteData()?.produccion?.kilosDistribuidos || 1;
    const valor = this.obtenerReporteValorDestino(dest);
    return (valor / total) * 100;
  }
}
