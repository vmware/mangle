import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-tags',
  templateUrl: './tags.component.html'
})
export class TagsComponent {

  constructor() { }

  @Input() tagsModal: boolean;
  @Input() tagsData: any;
  @Output() updateTagsModal = new EventEmitter<boolean>();

  public updateTags(tagsVal) {
    this.tagsData[tagsVal.tagKey] = tagsVal.tagValue;
  }

  public closeTagsModal() {
    this.tagsModal = false;
    this.updateTagsModal.emit(false);
  }

}

