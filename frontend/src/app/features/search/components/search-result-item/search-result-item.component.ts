import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { RepositoriesSearchOut } from '../../../../core/models/repositories-search-out.model';

@Component({
  selector: 'app-search-result-item',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
  ],
  templateUrl: './search-result-item.component.html',
  styleUrl: './search-result-item.component.css',
})
export class SearchResultItemComponent {
  @Input({ required: true }) item!: RepositoriesSearchOut;
}
