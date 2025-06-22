import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SearchResultItemComponent } from '@app/features/search/components/search-result-item/search-result-item.component';
import { MOCK_SEARCH_RESULTS } from '@app/core/models/repositories-search-out.stub';

describe('SearchResultItemComponent', () => {
  let component: SearchResultItemComponent;
  let fixture: ComponentFixture<SearchResultItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchResultItemComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SearchResultItemComponent);
    component = fixture.componentInstance;
    component.item = MOCK_SEARCH_RESULTS[0];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
