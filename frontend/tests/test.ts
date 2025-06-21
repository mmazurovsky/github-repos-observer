// This file is required by karma.conf.js and loads recursively all the .spec and framework files

import 'zone.js/testing';
import { getTestBed } from '@angular/core/testing';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting
} from '@angular/platform-browser-dynamic/testing';

// First, initialize the Angular testing environment.
getTestBed().initTestEnvironment(
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting(),
);

// Import all test files
import './app/app.component.spec';
import './app/features/search/components/search-bar.component.spec';
import './app/features/search/components/search-result-item.component.spec';
import './app/features/search/components/search-results.component.spec';
import './app/features/search/pages/search-page.component.spec'; 