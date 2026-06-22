import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { SoftwareCatalogItem, WorkshopSettings } from '../../shared/models/delivery-report.model';

@Component({
  selector: 'app-workshop-settings-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-heading">
      <div>
        <span class="eyebrow">Identidad</span>
        <h1>Datos del taller</h1>
      </div>
      <p>Contactos del taller y catálogo de software disponible para los reportes de reparación.</p>
    </section>

    <section class="settings-shell workshop-settings-shell">
      <form class="settings-form workshop-settings-form" (ngSubmit)="saveSettings()">
        <div class="report-card-head">
          <div>
            <div class="section-title">Contacto del taller</div>
            <small>El reporte usa logo-light.png como logo fijo y toma de acá los datos de contacto.</small>
          </div>
          <button class="primary-button" type="submit">Guardar datos</button>
        </div>
        <div class="report-form-grid two-cols">
          <label class="field"><span>Nombre del taller</span><input class="control" [(ngModel)]="settings.businessName" name="businessName" /></label>
          <label class="field"><span>Título del reporte</span><input class="control" [(ngModel)]="settings.reportTitle" name="reportTitle" /></label>
          <label class="field">
            <span class="inline-label"><i class="pi pi-whatsapp"></i> WhatsApp</span>
            <input class="control" [(ngModel)]="settings.whatsapp" name="whatsapp" />
          </label>
          <label class="field">
            <span class="inline-label"><i class="pi pi-instagram"></i> Instagram</span>
            <input class="control" [(ngModel)]="settings.instagram" name="instagram" />
          </label>
          <label class="field"><span>Logo</span><input class="control" [value]="settings.logoAssetPath" readonly /></label>
        </div>
      </form>

      <section class="settings-form workshop-settings-form">
        <div class="report-card-head">
          <div>
            <div class="section-title">Catálogo de software</div>
            <small>Sirve para llenar rápido los reportes y aún así podés editar cada ítem dentro del modal.</small>
          </div>
        </div>
        <div class="report-form-grid two-cols">
          <label class="field"><span>Software</span><input class="control" [(ngModel)]="draftItem.name" name="softwareName" /></label>
          <label class="field"><span>Detalle sugerido</span><input class="control" [(ngModel)]="draftItem.detail" name="softwareDetail" /></label>
        </div>
        <div class="card-inline-actions">
          <button class="secondary-button" type="button" (click)="saveCatalogItem()">Agregar al catálogo</button>
        </div>

        <div class="native-table-wrap">
          <table class="native-table compact-native-table">
            <thead><tr><th>Software</th><th>Detalle sugerido</th><th></th></tr></thead>
            <tbody>
              @for (item of catalog; track item.id || item.name) {
                <tr>
                  <td>{{ item.name }}</td>
                  <td>{{ item.detail || '-' }}</td>
                  <td class="centered-cell">
                    <button class="icon-button" type="button" (click)="deleteCatalogItem(item)"><i class="pi pi-trash"></i></button>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="3" class="empty-cell">Todavía no hay software en el catálogo.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </section>
    </section>
  `
})
export class WorkshopSettingsPageComponent implements OnInit {
  settings: WorkshopSettings = { businessName: 'Taller', whatsapp: '', instagram: '', reportTitle: 'REPORTE DE REPARACIÓN', logoAssetPath: 'report/logo-light.png' };
  catalog: SoftwareCatalogItem[] = [];
  draftItem: SoftwareCatalogItem = { name: '', detail: '' };

  constructor(private readonly api: ApiService, private readonly messages: MessageService) {}

  ngOnInit(): void {
    this.reload();
  }

  saveSettings(): void {
    this.api.updateWorkshopSettings(this.settings).subscribe({
      next: (saved) => {
        this.settings = saved;
        this.messages.add({ severity: 'success', summary: 'Datos guardados', detail: 'Los datos del taller quedaron actualizados.' });
      },
      error: () => this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron guardar los datos del taller.' })
    });
  }

  saveCatalogItem(): void {
    if (!this.draftItem.name?.trim()) {
      this.messages.add({ severity: 'warn', summary: 'Falta el nombre', detail: 'Indicá el nombre del software antes de agregarlo.' });
      return;
    }
    this.api.saveSoftwareCatalogItem(this.draftItem).subscribe({
      next: () => {
        this.draftItem = { name: '', detail: '' };
        this.reloadCatalog();
      },
      error: () => this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo guardar el software en el catálogo.' })
    });
  }

  deleteCatalogItem(item: SoftwareCatalogItem): void {
    if (!item.id) {
      return;
    }
    this.api.deleteSoftwareCatalogItem(item.id).subscribe({
      next: () => this.reloadCatalog(),
      error: () => this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo eliminar el software del catálogo.' })
    });
  }

  private reload(): void {
    this.api.getWorkshopSettings().subscribe({
      next: (settings) => {
        this.settings = settings;
        this.reloadCatalog();
      },
      error: () => this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los datos del taller.' })
    });
  }

  private reloadCatalog(): void {
    this.api.getSoftwareCatalog().subscribe({
      next: (catalog) => {
        this.catalog = catalog;
      },
      error: () => this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el catálogo de software.' })
    });
  }
}
