import { Component, Input } from '@angular/core';
import { RepositoriesSearchOut } from '../../../../core/models/repositories-search-out.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-search-result-item',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './search-result-item.component.html',
  styleUrl: './search-result-item.component.css',
})
export class SearchResultItemComponent {
  @Input({ required: true }) item!: RepositoriesSearchOut;
}
