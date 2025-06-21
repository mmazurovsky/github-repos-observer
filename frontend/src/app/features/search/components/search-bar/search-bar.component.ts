import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RepositoriesSearchIn } from '../../../../core/models/repositories-search-in.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.css'
})
export class SearchBarComponent {
  @Output() search = new EventEmitter<RepositoriesSearchIn>();
  searchForm: FormGroup;
  showOptionalFields = false;

  constructor(private fb: FormBuilder) {
    this.searchForm = this.fb.group({
      keywords: ['', [Validators.required, Validators.maxLength(50)]],
      earliestCreatedDate: [''],
      language: ['', [Validators.pattern(/^[a-zA-Z\s]*$/)]],
      maxPages: [10, [Validators.min(1), Validators.max(10)]]
    });
  }

  onSearch(): void {
    if (this.searchForm.valid) {
      this.search.emit(this.searchForm.value);
    }
  }

  toggleOptionalFields(): void {
    this.showOptionalFields = !this.showOptionalFields;
  }
}
