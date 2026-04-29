import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { TagModule } from 'primeng/tag';
import { DatePickerModule } from 'primeng/datepicker';
import { ApiService } from '../../core/services/api.service';
import { Repair } from '../../shared/models/repair.model';

@Component({
  selector: 'app-repairs-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, TableModule, SelectModule, InputNumberModule, TagModule, DatePickerModule],
  template: `
    <div class="page-grid">
      <p-card header="Nueva reparación">
        <form class="p-fluid" (ngSubmit)="save()">
          <div class="field"><label>ID Cliente</label><input pInputText [(ngModel)]="draft.idClient" name="idClient" required /></div>
          <div class="field"><label>ID Dispositivo</label><input pInputText [(ngModel)]="draft.idDevice" name="idDevice" required /></div>
          <div class="field"><label>Número de orden</label><input pInputText [(ngModel)]="draft.orderNumber" name="orderNumber" required /></div>
          <div class="field"><label>Descripción</label><input pInputText [(ngModel)]="draft.description" name="description" /></div>
          <div class="field"><label>Estado</label><p-select [options]="statusOptions" optionLabel="label" optionValue="value" [(ngModel)]="draft.status" name="status"></p-select></div>
          <div class="field"><label>Presupuesto enviado</label><p-inputNumber [(ngModel)]="draft.quotedAmount" name="quotedAmount" mode="currency" currency="USD" [min]="0"></p-inputNumber></div>
          <div class="field"><label>Monto final</label><p-inputNumber [(ngModel)]="draft.price" name="price" mode="currency" currency="USD" [min]="0"></p-inputNumber></div>
          <button pButton type="submit" label="Guardar reparación" icon="pi pi-check"></button>
        </form>
      </p-card>

      <p-card header="Historial de reparaciones">
        <div class="table-toolbar multi">
          <span class="p-input-icon-left"><i class="pi pi-search"></i><input pInputText [(ngModel)]="searchTerm" (ngModelChange)="applyFilters()" placeholder="Buscar por cliente, orden, dispositivo" /></span>
          <p-datepicker [(ngModel)]="fromDate" (ngModelChange)="applyFilters()" dateFormat="yy-mm-dd" placeholder="Desde" appendTo="body"></p-datepicker>
          <p-datepicker [(ngModel)]="toDate" (ngModelChange)="applyFilters()" dateFormat="yy-mm-dd" placeholder="Hasta" appendTo="body"></p-datepicker>
        </div>

        <p-table [value]="filteredRepairs" size="small" [paginator]="true" [rows]="10">
          <ng-template pTemplate="header"><tr><th>Orden</th><th>Cliente</th><th>Estado</th><th>Presupuesto</th><th>Monto</th><th>Acción</th></tr></ng-template>
          <ng-template pTemplate="body" let-r>
            <tr>
              <td>{{ r.orderNumber }}</td><td>{{ r.idClient }}</td><td><p-tag [value]="r.status"></p-tag></td>
              <td><p-inputNumber [(ngModel)]="r.quotedAmount" mode="currency" currency="USD" [min]="0"></p-inputNumber></td>
              <td>{{ r.price | currency:'USD' }}</td>
              <td><button pButton type="button" icon="pi pi-save" label="Actualizar" (click)="updateBudget(r)"></button></td>
            </tr>
          </ng-template>
        </p-table>
      </p-card>
    </div>
  `
})
export class RepairsPageComponent implements OnInit {
  repairs: Repair[] = [];
  filteredRepairs: Repair[] = [];
  draft: Repair = {
    idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0
  };
  searchTerm = '';
  fromDate: Date | null = null;
  toDate: Date | null = null;

  statusOptions = [
    { label: 'Por recibir', value: 'POR_RECIBIR' }, { label: 'Recibida', value: 'RECIBIDA' },
    { label: 'Presupuestada', value: 'PRESUPUESTADA_ESPERANDO_RESPUESTA' }, { label: 'Haciendo', value: 'HACIENDO' },
    { label: 'Esperando retiro', value: 'ESPERANDO_RETIRO' }, { label: 'Retirada', value: 'RETIRADA' }
  ];

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void { this.reload(); }

  save(): void {
    this.api.createRepair(this.draft).subscribe(() => {
      this.draft = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0 };
      this.reload();
    });
  }

  updateBudget(repair: Repair): void {
    this.api.updateRepair(repair).subscribe(() => this.reload());
  }

  applyFilters(): void {
    const term = this.searchTerm.trim().toLowerCase();
    this.filteredRepairs = this.repairs.filter((r) => {
      const matchesTerm = !term || [r.idClient, r.idDevice, r.orderNumber, r.description].filter(Boolean).join(' ').toLowerCase().includes(term);
      const receive = r.receiveDateTime ? new Date(r.receiveDateTime) : null;
      const matchesFrom = !this.fromDate || (receive && receive >= this.fromDate);
      const matchesTo = !this.toDate || (receive && receive <= this.toDate);
      return Boolean(matchesTerm && matchesFrom && matchesTo);
    });
  }

  private reload(): void {
    this.api.getRepairs().subscribe((repairs) => {
      this.repairs = repairs.slice().reverse();
      this.applyFilters();
    });
  }
}
