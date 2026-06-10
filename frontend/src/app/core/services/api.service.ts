import { Injectable } from '@angular/core';
import { ClientApiService } from './client-api.service';
import { DeviceApiService } from './device-api.service';
import { NotificationApiService } from './notification-api.service';
import { RepairApiService } from './repair-api.service';
import { ReportingApiService } from './reporting-api.service';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(
    private readonly clients: ClientApiService,
    private readonly devices: DeviceApiService,
    private readonly repairs: RepairApiService,
    private readonly notifications: NotificationApiService,
    private readonly reporting: ReportingApiService
  ) {}

  getClients = this.clients.getAll.bind(this.clients);
  getClientById = this.clients.getById.bind(this.clients);
  getClientPage = this.clients.getPage.bind(this.clients);
  getClientHistory = this.clients.getHistory.bind(this.clients);
  searchClients = this.clients.search.bind(this.clients);
  createClient = this.clients.save.bind(this.clients);
  deleteClient = this.clients.delete.bind(this.clients);

  getDevices = this.devices.getAll.bind(this.devices);
  getDeviceTypes = this.devices.getTypes.bind(this.devices);
  getDeviceById = this.devices.getById.bind(this.devices);
  searchDevices = this.devices.search.bind(this.devices);
  createDevice = this.devices.create.bind(this.devices);
  updateDevice = this.devices.update.bind(this.devices);
  deleteDevice = this.devices.delete.bind(this.devices);
  addDevicePassword = this.devices.addPassword.bind(this.devices);
  updateDevicePassword = this.devices.updatePassword.bind(this.devices);
  deleteDevicePassword = this.devices.deletePassword.bind(this.devices);
  makeCurrentDevicePassword = this.devices.makeCurrentPassword.bind(this.devices);
  addDeviceObservation = this.devices.addObservation.bind(this.devices);
  updateDeviceObservation = this.devices.updateObservation.bind(this.devices);
  resolveDeviceObservation = this.devices.resolveObservation.bind(this.devices);
  deleteDeviceObservation = this.devices.deleteObservation.bind(this.devices);

  getRepairs = this.repairs.getAll.bind(this.repairs);
  getStatusBoardRepairs = this.repairs.getStatusBoard.bind(this.repairs);
  getRepairById = this.repairs.getById.bind(this.repairs);
  searchRepairs = this.repairs.search.bind(this.repairs);
  createRepair = this.repairs.create.bind(this.repairs);
  updateRepair = this.repairs.update.bind(this.repairs);
  deleteRepair = this.repairs.delete.bind(this.repairs);

  getNotifications = this.notifications.getAll.bind(this.notifications);
  getUnreadNotificationCount = this.notifications.getUnreadCount.bind(this.notifications);
  markNotificationAsRead = this.notifications.markAsRead.bind(this.notifications);

  getDashboardOverview = this.reporting.getDashboardOverview.bind(this.reporting);
  getLatestClients = this.reporting.getLatestClients.bind(this.reporting);
  getLatestDevices = this.reporting.getLatestDevices.bind(this.reporting);
  getLatestRepairs = this.reporting.getLatestRepairs.bind(this.reporting);
  getFinanceSummary = this.reporting.getFinanceSummary.bind(this.reporting);
}
