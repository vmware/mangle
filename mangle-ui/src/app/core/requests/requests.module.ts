import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { AppRoutingModule } from '../../app-routing.module';
import { ScheduledComponent } from './scheduled/scheduled.component';
import { ProcessedComponent } from './processed/processed.component';
import { RequestsComponent } from './requests.component';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [RequestsComponent, ScheduledComponent, ProcessedComponent],
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    AppRoutingModule,
    CommonModule,
    SharedModule
  ]
})
export class RequestsModule { }
