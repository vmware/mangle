import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ControlContainer, NgForm } from '@angular/forms';

@Component({
  selector: 'fault-tags',
  templateUrl: './faulttags.component.html',
  viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class FaultTagsComponent {

  constructor() { }

  @Input() tagsData: any;
  @Output() tagsModalEvent = new EventEmitter<boolean>();

  public removeTag(tagKeyToRemove) {
    delete this.tagsData[tagKeyToRemove];
  }

  public openTagsModal() {
    this.tagsModalEvent.emit(true);
  }

}

