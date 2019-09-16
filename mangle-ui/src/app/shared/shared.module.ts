import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedComponent } from './shared.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { CronComponent } from './cron/cron.component';
import { EventsComponent } from './events/events.component';
import { AlertComponent } from './alerts/alert.component';

@NgModule({
  declarations: [SharedComponent, AlertComponent, EventsComponent, CronComponent],
  exports: [CronComponent, EventsComponent, AlertComponent],
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    CommonModule
  ]
})
export class SharedModule { }
