# Implementation Plan - Taraz (تراز) v1.0.0

An Iranian shop accounting app built with Kotlin, Jetpack Compose, Material 3, Room, and Jalali calendar, fully in RTL Persian.

---

## Architecture & Tech Stack
- **Language**: Kotlin 2.x
- **UI**: Jetpack Compose + Material 3 with full RTL support and Vazirmatn font.
- **Database**: Room with incremental migrations, type converters for enums/dates, and sample pre-loaded data.
- **State Management**: ViewModel + StateFlow with coroutines.
- **Navigation**: Navigation Compose with safe routing and BackHandler support.
- **Formatting & Calendar**: Custom Jalali (Shamsi) calendar engine (`Asia/Tehran`) and Persian digit / currency formatting (`Fa.money`, `Fa.digits`).
- **PDF Export**: PdfDocument with A5 layout and Vazirmatn font embedding, supporting Customer version (no purchase price/profit) and Seller version.

---

## Phased Implementation Plan

### Phase 1: Foundation & Project Setup
- Update `metadata.json` with app name "تراز" and description.
- Setup `res/font/vazirmatn_*.ttf` (Regular, Medium, Bold) or fallback typography and configure Theme.kt with M3 color scheme and RTL layout direction.
- Create base package structure (`ir.taraz.app` for release, `ir.taraz.app.debug` for debug).
- Create `docs/DESIGN.md` outlining data models and accounting rules.
- Test build (`compile_applet`).

### Phase 2: Room Database & Accounting Engine
- Define Room Entities:
  - `Contact`: id, name, phone, address, type, creditLimit, note, createdAt
  - `Product`: id, name, baseUnit, unitsPerCarton, purchasePrice, salePrice, stock, minStock, barcode, isActive
  - `UnitConversion`: id, productId, unit, factorToBase
  - `Invoice`: id, number, type, contactId, contactNameSnapshot, date, subtotal, discount, taxPercent, total, paidAmount, paymentMethod, status, voidReason, isEdited, note, costTotal, profit, createdAt, updatedAt
  - `InvoiceItem`: id, invoiceId, productId, productNameSnapshot, unit, quantity, cartons, unitPrice, unitCost, discount, lineTotal
  - `Transaction`: id, type, amount, categoryId, contactId, invoiceId, date, note, isOwnerCapital (آورده/برداشت مالک)
  - `Cheque`: id, direction, amount, dueDate, bank, serial, contactId, status
  - `Category`: id, name, kind, colorHex
  - `ShopSettings`: shopName, shopPhone, shopAddress, logoPath, defaultTaxPercent, sellerName, pinHash
- Implement `AccountingEngine` with atomic transactions for Sale, Purchase, Estimate, Void, and Edit.
- Implement DAOs and Database with pre-populated sample data (products, contacts, categories).
- Write `AccountingEngineTest` and core unit tests.

### Phase 3: Bottom Navigation & Core Scaffold
- Implement 5 tabs: داشبورد (Dashboard), فاکتور (Invoice), انبار (Inventory), اشخاص (Persons), گزارشات (Reports).
- Scaffold with 72dp bottom nav, RTL layout, badge/indicator, and no text clipping.
- Safe navigation routing and BackHandler handling.

### Phase 4: Invoice Flow (Create, Edit, Void)
- Multi-mode invoice screen:روش فروش | خرید | پیش‌فاکتور (Sale, Purchase, Estimate).
- Contact selector (with quick "+ New Contact" dialog).
- Item picker with base unit, cartons conversion, quantity, price, discount, line total.
- Free-form / service item support (optional unit cost for seller).
- Summary with subtotal, discount, tax, total, paid amount, payment method (Cash, Credit, Card, Cheque).
- Save with 600ms debounce, Snackbar with PDF & Share actions.
- Invoice archive, search, filter, and detail screen with edit and void actions (reversing stock & cash atomically).

### Phase 5: Dashboard, Inventory & Persons Tabs
- **Dashboard**: 2x2 cards (Today's Sale, Today's Profit, Receivables, Payables), inventory valuation, last 5 invoices with quick edit, quick actions for cheques/categories/settings, FAB for new invoice, empty state handling.
- **Inventory**: Product list with stock status, low stock filter, edit product, safe archive (`isActive=false`), new product FAB, barcode support, total inventory valuation.
- **Persons**: Customers & Suppliers tabs, contact cards with balance (green/red), detail screen showing invoices, transactions, payment recording (receive/pay), call/sms actions.

### Phase 6: PDF Export & Sharing
- A5 PDF generation using native Android `PdfDocument` with embedded Vazirmatn font.
- Two versions:
  - **Customer Version**: Excludes unit cost, line profit, and total profit.
  - **Seller Version**: Includes unit cost, profit per line, and total profit.
- Sharing actions: Save PDF, Android Share Chooser (`content://` URI via `FileProvider`), SMS short text (`smsto:`), and plain text summary.

### Phase 7: Jalali Calendar & DatePicker
- Custom Jalali DatePicker dialog with Persian month names and year/month/day selectors.
- `Fa` helper object for Persian digits (`۰-۹`) and currency formatting (`تومان`).
- Robust date converters (`Asia/Tehran` midnight millis).
- Write `JalaliTest`.

### Phase 8: Reports Tab
- 8 comprehensive report views:
  1. Sales (daily chart + total, payments, invoice balances)
  2. Profit & Loss (gross/net profit with missing purchase cost warnings, operational expenses)
  3. Contact Balances (receivables & payables)
  4. Purchase & Inventory Valuation
  5. Top 10 Best-Selling Products
  6. Simple Balance Sheet (assets/liabilities)
  7. Tax / VAT info report
  8. Owner Capital vs Operational income/expenses, voided invoices excluded
- PDF and share support for all reports.

### Phase 9: Cheques, Settings & Backup / Restore
- **Cheques Tab**: Received & Paid tabs, due dates, bank, serial, status (Pending, Cashed, Bounced), 7-day due warning.
- **Settings Tab**: Shop profile, logo picker, seller name, default tax, PIN lock, JSON backup/restore with schema versioning & transaction safety, category management, about app.
- Write `BackupRoundTripTest` and `MigrationTest`.

### Phase 10: Polish, Final Test Suite & Release Verification
- Run all 40+ unit/Robolectric tests (`gradle :app:testDebugUnitTest`).
- Verify RTL rendering, Persian typography, touch targets (>=48dp), and screen density.
- Build release APK and verify checks.
