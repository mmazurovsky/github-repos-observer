import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app/app.component';
import { routes } from './app/app-routing.module';
import { provideHttpClient } from '@angular/common/http';
import { SearchApiService } from './app/core/services/search-api.service';
import { SearchStateService } from './app/core/services/search-state.service';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    SearchApiService,
    SearchStateService
  ]
}).catch((err) => console.error(err));
