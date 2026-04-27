import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { Repair } from '../../shared/models/repair.model';

@Component({
  selector: 'app-repairs-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Reparaciones</h2>
    <form (ngSubmit)="save()">
      <input [(ngModel)]="draft.idClient" name="idClient" placeholder="ID Cliente" required />
      <input [(ngModel)]="draft.idDevice" name="idDevice" placeholder="ID Dispositivo" required />
      <input [(ngModel)]="draft.orderNumber" name="orderNumber" placeholder="Número de orden" required />
      <textarea [(ngModel)]="draft.description" name="description" placeholder="Descripción"></textarea>
      <select [(ngModel)]="draft.status" name="status">
        <option value="POR_RECIBIR">Por recibir</option>
        <option value="RECIBIDA">Recibida</option>
        <option value="PRESUPUESTADA_ESPERANDO_RESPUESTA">Presupuestada</option>
        <option value="HACIENDO">Haciendo</option>
        <option value="ESPERANDO_RETIRO">Esperando retiro</option>
        <option value="RETIRADA">Retirada</option>
      </select>
      <input [(ngModel)]="draft.price" name="price" type="number" placeholder="Precio final" required />
      <button type="submit">Agregar reparación</button>
    </form>
    <ul>
      <li *ngFor="let repair of repairs">Orden {{ repair.orderNumber }} · {{ repair.status }} · ${{ repair.price }}</li>
    </ul>
  `
})
export class RepairsPageComponent implements OnInit {
  repairs: Repair[] = [];
  draft: Repair = {
    idDevice: '',
    idClient: '',
    orderNumber: '',
    description: '',
    status: 'POR_RECIBIR',
    price: 0
  };

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.reload();
  }

  save(): void {
    this.api.createRepair(this.draft).subscribe(() => {
      this.draft = {
        idDevice: '',
        idClient: '',
        orderNumber: '',
        description: '',
        status: 'POR_RECIBIR',
        price: 0
      };
      this.reload();
    });
  }

  private reload(): void {
    this.api.getRepairs().subscribe((repairs) => (this.repairs = repairs));
  }
}
