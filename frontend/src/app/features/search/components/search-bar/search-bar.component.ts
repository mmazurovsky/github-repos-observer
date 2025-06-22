import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RepositoriesSearchIn } from '../../../../core/models/repositories-search-in.model';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatExpansionModule,
  ],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.css',
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
      maxPages: [5, [Validators.min(1), Validators.max(5)]],
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
