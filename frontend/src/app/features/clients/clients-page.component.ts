import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { ApiService } from '../../core/services/api.service';
import { Client } from '../../shared/models/client.model';

@Component({
  selector: 'app-clients-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, TableModule],
  template: `
    <div class="page-grid">
      <p-card header="Nuevo cliente" subheader="Alta rápida">
        <form class="p-fluid" (ngSubmit)="save()">
          <div class="field"><label>Nombre</label><input pInputText [(ngModel)]="draft.name" name="name" required /></div>
          <div class="field"><label>Apellido</label><input pInputText [(ngModel)]="draft.lastName" name="lastName" required /></div>
          <div class="field"><label>DNI</label><input pInputText [(ngModel)]="draft.dni" name="dni" required /></div>
          <div class="field"><label>Email</label><input pInputText [(ngModel)]="draft.email" name="email" required /></div>
          <div class="field"><label>Celular</label><input pInputText [(ngModel)]="draft.phone" name="phone" required /></div>
          <button pButton type="submit" label="Guardar cliente" icon="pi pi-check"></button>
        </form>
      </p-card>

      <p-card header="Clientes">
        <div class="table-toolbar">
          <span class="p-input-icon-left"><i class="pi pi-search"></i><input pInputText [(ngModel)]="searchTerm" (ngModelChange)="onSearch()" placeholder="Buscar por nombre, DNI o email" /></span>
        </div>
        <p-table [value]="clients" size="small" [paginator]="true" [rows]="10">
          <ng-template pTemplate="header"><tr><th>Nombre</th><th>DNI</th><th>Email</th><th>Teléfono</th></tr></ng-template>
          <ng-template pTemplate="body" let-c><tr><td>{{ c.name }} {{ c.lastName }}</td><td>{{ c.dni }}</td><td>{{ c.email }}</td><td>{{ c.phone }}</td></tr></ng-template>
        </p-table>
      </p-card>
    </div>
  `
})
export class ClientsPageComponent implements OnInit {
  clients: Client[] = [];
  draft: Client = { name: '', lastName: '', dni: '', email: '', phone: '' };
  searchTerm = '';

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void { this.reload(); }

  save(): void {
    this.api.createClient(this.draft).subscribe(() => {
      this.draft = { name: '', lastName: '', dni: '', email: '', phone: '' };
      this.reload();
    });
  }

  onSearch(): void {
    if (!this.searchTerm.trim()) {
      this.reload();
      return;
    }
    this.api.searchClients(this.searchTerm).subscribe((clients) => (this.clients = clients));
  }

  private reload(): void { this.api.getClients().subscribe((clients) => (this.clients = clients.slice().reverse())); }
}
