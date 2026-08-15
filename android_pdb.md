# Product Requirements Document: Android Book Management App

## 1. Overview

Build an Android book management application in Go using the Fyne GUI framework. The app must provide feature parity with the existing JavaFX "I have read" desktop application while adapting the user experience to Android phones and tablets.

The Android app is an offline-first personal reading database. It manages authors, books, alternate titles, tags, read history, Goodreads links, local library files, and database backup/restore. The app must use the same SQLite data model as the JavaFX app so the existing `ihaveread.db` file can be migrated without data loss.

## 2. Source Application Scope

The PRD is based on the JavaFX app in this repository, especially:

- `src/main/resources/com/vlad/ihaveread/main-view.fxml`
- `src/main/java/com/vlad/ihaveread/MainController.java`
- `src/main/java/com/vlad/ihaveread/db/*.java`
- `src/main/java/com/vlad/ihaveread/*Dialog.java`
- `src/main/java/com/vlad/ihaveread/util/MegaUtil.java`
- `data/ihaveread.db` schema

The Android app must implement the JavaFX application behavior, not necessarily the same desktop layout.

## 3. Goals

- Provide a native Android app for managing the same personal book database currently managed by the JavaFX app.
- Preserve the existing SQLite schema and data semantics.
- Support search, browse, create, update, and delete workflows for authors, books, book names, tags, and read entries.
- Support external database backup upload and restore download, including MEGA-like cloud storage.
- Keep the app usable offline for all database features except remote backup operations.
- Package the app as an Android APK using Fyne tooling.

## 4. Non-Goals

- Do not build a social reading network.
- Do not require a server backend for normal use.
- Do not replace Goodreads with a full metadata import service in the first release.
- Do not require cloud login to use the local book database.
- Do not port JavaFX UI structure literally; Android UX may use navigation tabs, lists, dialogs, and detail screens.

## 5. Users and Use Cases

Primary user: A single reader maintaining a personal multilingual library and read-history database.

Core use cases:

- Find read books by year, author, title, tag, or saved/custom conditions.
- Add a newly read book in one transaction.
- Mark a book as "want to read" with an incomplete read record.
- Edit book metadata, alternate names, Goodreads IDs, local file paths, authors, tags, and read entries.
- Manage authors and alternate author names.
- Manage bilingual tags.
- Open Goodreads pages or searches from stored IDs.
- Link books to local ebook files and open them on Android.
- Upload the working SQLite database to external storage.
- Download a backup and replace the working database safely.

## 6. Platform and Technical Requirements

- Language: Go.
- UI framework: Fyne v2.
- Target platform: Android phone and tablet.
- Build output: Android APK.
- Packaging command must support Fyne's Android packaging flow, for example `fyne package -os android -app-id <unique.app.id> -icon <icon.png>`.
- Development environment must include Go, Fyne tools, Android SDK, and Android NDK.
- Local data store: SQLite database file compatible with the existing JavaFX database.
- All normal database operations must run locally and offline.
- Long-running operations, including search, file matching, backup, download, and restore, must not block the UI thread.

References checked while drafting:

- Fyne mobile packaging: https://docs.fyne.io/started/mobile/
- Fyne quick start and Android prerequisites: https://docs.fyne.io/started/quick/
- Fyne storage package: https://pkg.go.dev/fyne.io/fyne/v2/storage
- MEGA SDK repository and Android support notes: https://github.com/meganz/sdk
- MEGAcmd user guide for current desktop behavior context: https://github.com/meganz/MEGAcmd/blob/master/UserGuide.md

## 7. Data Model Requirements

The Android app must support the existing SQLite schema:

```sql
author(id, name, lang, note)
author_names(author_id, name, lang, type)
author_book(author_id, book_id)
book(id, title, publish_date, lang, series, note)
book_names(id, book_id, name, lang, goodreads_id, lib_file)
book_readed(id, book_name_id, date_read, medium, score, note)
tag(id, name_en, name_uk)
book_tag(book_id, tag_id)
custom_text(id, type, content)
```

The app must preserve these constraints and semantics:

- `author.name` is unique and displayed in normalized `"Surname, Names"` form.
- `author_names` stores alternate searchable author names, including normalized and natural forms.
- `author_book` supports multiple authors per book.
- `book_names` stores alternate titles, translations, languages, Goodreads IDs, and local library file paths.
- `book_tag` supports multiple tags per book.
- `book_readed` points to the read-language `book_names` record, not directly to `book`.
- `custom_text` with `type = 'where'` stores saved read-history query snippets.
- Existing databases must open without destructive migration in v1.
- Schema validation must check required tables and indexes, including `book_tag` and the unique book-name index on `(book_id, lang, name)`.
- If optional helper table `custom_text` is missing, the app must either create it safely or fall back to built-in saved conditions without blocking normal use.
- Delete behavior must clean up dependent rows:
  - Deleting an author removes `author_book` and `author_names` rows for that author, then the author row.
  - Deleting a book removes author links, tag links, read entries linked through book names, book names, and the book row.
  - Deleting a book name removes read entries linked to that book name.

## 8. Navigation and UX Requirements

The Android app must provide five primary areas equivalent to the JavaFX tabs:

- Read
- Authors
- Books
- Tags
- Tools

Recommended Android layout:

- Phones: bottom navigation or top tabs with one active area at a time.
- Tablets: optional master-detail layout for search results and detail forms.
- Large tables from the desktop app should become scrollable lists with sortable/detail views where needed.
- Every destructive action must require confirmation.
- Every save, delete, backup, upload, download, and restore operation must show success or error status.
- Forms must preserve current data if validation fails.
- List rows must be tappable; double-click desktop behavior must map to tap/open or long-press actions.

## 9. Functional Requirements

### 9.1 Read History

The Read area must display read-history search results with these fields:

- Read date
- Authors
- Title read
- Read language
- Publish date
- Medium
- Score
- Tags
- Note
- Goodreads link/search state
- Local file availability marker

The app must support these searches:

- By year: match `book_readed.date_read` prefix, equivalent to `YYYY%`.
- By author: search through `author_names.name LIKE %query%`.
- By title: search through `book_names.name LIKE %query%`.
- By tag: search by `tag.name_en`.
- By custom condition: feature parity with JavaFX custom where search.

Custom condition search on Android must be safer than raw SQL text entry:

- The app may keep an "advanced SQL where" mode behind a warning for trusted personal use.
- The default mobile UI must provide a query builder or saved-condition picker.
- Built-in saved conditions must include at least `date_read is null` and `score is null`.
- The condition picker must prepend current month and previous month values in `yyyy-MM` format, as the JavaFX app does.

Read search result actions:

- Open the selected result's book in the Books area.
- Open Goodreads:
  - Normal ID opens `https://www.goodreads.com/book/show/<id>`.
  - `alt:<id>` opens the alternate Goodreads book page.
  - `search:<text>` opens a Goodreads search URL.
- If no Goodreads ID exists for the selected book name and another book name has one, show the alternate ID as `alt:<id>`.
- If no Goodreads ID exists at all, generate `search:<title> <authors>`.

### 9.2 Author Management

The Authors area must support:

- Search authors by alternate author names using case-insensitive partial matching.
- Display matching authors by primary `author.name`.
- Select an author and show:
  - Name
  - Language
  - Note
  - Computed base directory
  - Alternate author names
- Create a new author.
- Edit and save author name, language, and note.
- Delete an author after confirmation.
- Show all read books for the selected author in the Read area.
- Add, edit, and delete alternate author names.
- Open or browse the author's computed library directory where Android storage permissions allow it.

New author creation requirements:

- Fields: names, surname, language, note.
- Names and surname are required.
- On create, store primary author name as `"Surname, Names"`.
- Also create two `author_names` rows:
  - Normalized name: `"Surname, Names"` with type `norm`.
  - Natural name: `"Names Surname"` with type `natural`.

Alternate author name requirements:

- Fields: name, language, type.
- Name and language are required.
- Type is optional.

Base directory computation must match JavaFX behavior:

- Ukrainian author: `/_ukr/<first-letter-lower>/<author-name>`.
- Other author: `/<first-letter-lower>/<author-name>`.
- For book-name-specific file paths, non-Ukrainian authors add `/<book-name-lang>` to the base directory.

### 9.3 Book Management

The Books area must support:

- Search books by alternate book names using partial matching.
- Display matching books by primary `book.title`.
- Select a book and show:
  - Authors
  - Title
  - Language
  - Publish date
  - Series
  - Tags
  - Note
  - Book names
  - Read entries
- Create a new book/read entry through the same transactional workflow as JavaFX.
- Edit and save book title, language, publish date, series, and note.
- Delete a book after confirmation.
- Add or remove authors.
- Add or remove tags.
- Add, edit, find file for, and delete book names.
- Add, edit, and delete read entries.
- Navigate from a listed author to the Authors area.

New book/read creation requirements:

- Fields:
  - Authors
  - Read title
  - Read language
  - Original title
  - Original language
  - Publish date
  - Series
  - Note
  - Want to read
  - Read date
  - Medium
  - Score
- Required:
  - At least one author.
  - Read title.
  - Read language.
  - If original title is entered, original language is required.
  - If not "want to read", score must be numeric.
- If original title is empty, original title and language default to read title and read language.
- The app must insert in one SQLite transaction:
  - `book`
  - read-language `book_names`
  - optional original-language `book_names`
  - `book_readed`
  - `author_book` links
- On failure, the transaction must roll back completely.
- If "want to read" is selected, create a read entry without read date and medium, with score defaulting to `0`, matching current JavaFX behavior.

Book name requirements:

- Fields: name, language, Goodreads ID, library file.
- Name and language are required.
- Goodreads ID and library file are optional.
- Book name rows must display ID, language, name, Goodreads link, and library file or computed directory.
- Deleting a book name must also delete read entries linked to that book name, matching the JavaFX DAO behavior.

Read entry requirements:

- Fields: book name, read date, medium, score, note.
- Book name and read date are required.
- Score must be numeric.
- Medium and note are optional.
- Read date format stored in DB must be `yyyy-MM-dd`.

### 9.4 Tag Management

The Tags area must support:

- Search tags by English or Ukrainian name using partial matching.
- Display matching tags.
- Select a tag and show:
  - English name
  - Ukrainian name
- Create a new tag.
- Edit and save tag names.
- Show read books for the selected tag in the Read area.
- Select multiple tags when attaching tags to a book.
- Search multiple tag fragments separated by `|`, as the JavaFX select-tag dialog does.
- Copy selected book tags to the Android clipboard as pipe-separated English names.

New tag creation requirements:

- Fields: English name, Ukrainian name.
- Both fields are required.
- Both names must remain unique according to the existing schema.

### 9.5 Local Library File Handling

The app must support local ebook library paths stored in `book_names.lib_file`.

Desktop behavior to preserve:

- If `lib_file` is empty, display/open the computed book directory.
- If `lib_file` is present, display/open the stored file path.
- For EPUB, FB2, and FB2 ZIP files, JavaFX opens the Foliate reader.
- Other files use the OS default opener.

Android adaptation:

- Use Android storage access mechanisms and Fyne URI/storage APIs where possible.
- Allow the user to configure a library root directory or document tree.
- Open ebook files using Android intents so installed reader apps can handle EPUB, FB2, FB2 ZIP, PDF, or other supported types.
- If a directory or file is missing or not permitted, show a warning and allow the user to reselect the library root or file.
- Do not depend on Linux commands such as `xdg-open` or `foliate`.

Find file requirements:

- For a selected book name with empty `lib_file`, derive the expected directory from the first author and book-name language.
- Search non-directory files in that directory.
- Compare the book name and file base names using a string-similarity algorithm.
- Also compare against a Latin transliteration of Ukrainian titles.
- Use the same threshold concept as JavaFX: accept matches above approximately `0.4`; if none pass threshold, offer the two best candidates.
- If exactly one candidate is found, store it automatically.
- If multiple candidates are found, let the user choose.
- Save the selected relative file path into `book_names.lib_file`.

### 9.6 Tools, Counts, Backup, Upload, and Restore

The Tools area must show:

- Author count.
- Book count.
- Read-entry count.
- Backup status.
- Upload status.
- Download/restore status.

Database import and working-copy requirements:

- On first launch or from Tools, the user must be able to import an existing `ihaveread.db` file.
- The app must copy the imported database into app-private working storage before opening it.
- The app must keep the working database path stable after import so all features operate on the same file.
- Replacing the working database from any source must follow the same safety rules as remote restore.

Local SQL dump requirement:

- Provide a "Backup DB" action equivalent to `sqlite3 <db> .dump > <db>_dump.sql`.
- On Android, implement this without requiring a shell `sqlite3` binary unless the build explicitly includes one.
- The dump file should be exportable through Android document sharing or a user-selected folder.

Remote backup upload requirements:

- Provide an "Upload DB" action.
- Close or checkpoint the SQLite database before copying/uploading.
- Upload a consistent snapshot of the working database.
- Default remote path should be compatible with the JavaFX app: `ihaveread/ihaveread.db`.
- Preserve JavaFX conditional behavior as an option: upload only if the local database modified timestamp is newer than the remote file.
- Also provide a forced upload action, because mobile users need explicit backup control.
- Show remote timestamp, local timestamp, file size, and final status.
- Do not store MEGA credentials in environment variables on Android.
- Store cloud credentials or tokens in Android secure storage/keystore or require login per session.

Remote backup download and replace requirements:

- Provide a "Download DB" or "Restore from Cloud" action.
- Show remote timestamp, size, and provider before restore.
- Require confirmation before replacing the working database.
- Before replacing, create a local safety copy of the current database, named with timestamp.
- Download the remote file to a temporary path first.
- Validate that the downloaded file is a readable SQLite database with required tables.
- Close the working database connection.
- Atomically replace the working database file where the filesystem allows it.
- Reopen the database and refresh counts and visible lists.
- If restore fails, keep or restore the previous working database and show an error.
- Preserve JavaFX conditional behavior as an option: download only if remote timestamp is newer than local timestamp.
- Also provide a forced restore action.

Remote storage provider requirements:

- Initial provider must support MEGA or a MEGA-like external storage service.
- The implementation must use a provider interface, for example:
  - `ListBackups(ctx)`.
  - `UploadBackup(ctx, localFile, remotePath, metadata)`.
  - `DownloadBackup(ctx, remotePath, targetFile)`.
  - `GetMetadata(ctx, remotePath)`.
  - `DeleteBackup(ctx, remotePath)` as optional post-v1.
- MEGA integration must not depend on desktop-only MEGAcmd shell commands.
- If native MEGA SDK integration in all-Go Fyne Android is not feasible, the project must isolate MEGA behind the provider interface and complete an early technical spike to choose one of:
  - MEGA SDK Android binding.
  - Compatible Go MEGA client that builds for Android.
  - User-selected external document provider as a fallback, plus MEGA in a later integration milestone.

## 10. Security and Data Safety Requirements

- All destructive operations require confirmation.
- Backup restore must always create a local pre-restore safety copy.
- Database writes must use transactions for multi-table operations.
- Database operations must use parameterized SQL except explicitly trusted advanced custom conditions.
- Cloud credentials must never be logged.
- Backup logs must not include passwords or tokens.
- The app must tolerate network interruption during upload/download.
- Backup upload must never upload a half-written SQLite database.
- Restore must validate the downloaded database before replacement.

## 11. Performance Requirements

- Search must remain responsive with at least 20,000 books, 20,000 book names, 20,000 read entries, and 5,000 authors.
- Common search result lists should render first results within 500 ms on a mid-range Android device after the database is open.
- Long operations over 500 ms must show a progress indicator.
- Remote backup operations must show progress where provider APIs expose it.
- List/detail views must use lazy rendering or paging where needed.

## 12. Error Handling Requirements

The app must show actionable error messages for:

- Missing database file.
- Invalid or incompatible database schema.
- Failed save/delete due to constraints.
- Missing required fields.
- Non-numeric score.
- Missing library directory or file.
- Storage permission denied.
- Cloud login failure.
- Upload/download failure.
- Restore validation failure.
- Restore replacement failure.

Errors must not crash the app during normal use.

## 13. Accessibility and Localization Requirements

- All controls must have readable labels.
- Touch targets must be suitable for Android.
- The app must support multilingual data entry, especially English and Ukrainian.
- The UI may initially be English, matching the JavaFX labels.
- Stored data must remain UTF-8 compatible.
- Clipboard operations must preserve Unicode text.

## 14. Architecture Requirements

Recommended package structure:

```text
cmd/ihaveread-android/
internal/app/
internal/ui/
internal/db/
internal/model/
internal/search/
internal/library/
internal/backup/
internal/backup/mega/
internal/backup/localdoc/
internal/platform/android/
```

Required layers:

- UI layer: Fyne screens, forms, dialogs, validation, navigation.
- Service layer: use cases and transactions.
- Repository layer: SQLite queries and schema validation.
- Backup provider layer: MEGA/local document/cloud implementations.
- Platform layer: Android storage, intents, secure credential storage, permissions.

The UI must call service methods rather than embedding SQL or backup logic directly.

## 15. Suggested Milestones

### Milestone 1: Android/Fyne Skeleton

- Go module and Fyne app shell.
- Android package build works.
- App opens with five primary navigation areas.
- Existing database file can be imported/opened from app-private storage.

### Milestone 2: SQLite Compatibility

- Data models and repositories implemented.
- Schema validation.
- Counts shown in Tools.
- Read-only search for Read, Authors, Books, and Tags.

### Milestone 3: CRUD Feature Parity

- Author create/edit/delete and alternate names.
- Tag create/edit and book-tag assignment.
- Book edit/delete, authors, tags, book names.
- Read-entry add/edit/delete.
- New book/read transactional flow.

### Milestone 4: Links and Local Library Files

- Goodreads link/search handling.
- Android file open intents.
- Library root configuration.
- Find-file similarity and Ukrainian transliteration matching.

### Milestone 5: Backup and Restore

- Local SQL dump/export.
- Remote backup provider interface.
- Upload working database.
- Download backup and replace working database safely.
- MEGA integration or validated MEGA-compatible implementation.

### Milestone 6: Hardening

- Error handling.
- Performance tuning.
- Android permission handling.
- Backup failure recovery tests.
- APK release build.

## 16. Acceptance Criteria

The Android app is acceptable when:

- An existing `ihaveread.db` from the JavaFX app opens successfully.
- Counts for authors, books, and read entries match the JavaFX app for the same database.
- Read search by year, author, title, tag, and selected custom condition returns matching records.
- Author CRUD and alternate-name CRUD work and persist.
- Book CRUD, author links, tag links, book names, and read entries work and persist.
- New book/read creation writes all related rows transactionally.
- Goodreads links open the correct page or search.
- Local library files can be selected, stored, detected, and opened through Android.
- Similar-file matching can find candidate files using title similarity and Ukrainian transliteration.
- Tags can be searched, created, edited, assigned, removed, and copied as pipe-separated English names.
- Backup DB creates an exportable SQL dump.
- Upload DB stores a consistent database copy in external storage.
- Download/restore can replace the working database after confirmation.
- Restore creates a local safety copy before replacement.
- Failed restore does not corrupt or delete the existing working database.
- The APK can be built using Fyne Android packaging.

## 17. Open Technical Questions

- Which SQLite driver will be used for Android Fyne builds, and does it support the required SQLite features on all target devices?
- Which MEGA integration path is practical for all-Go Fyne Android: native MEGA SDK binding, compatible Go client, or external document-provider fallback?
- Should the app expose raw SQL custom where conditions, or only a safer query-builder equivalent?
- Should the app support multiple named remote backups or only the JavaFX-compatible `ihaveread/ihaveread.db` path in v1?
- Should library root paths remain compatible with desktop relative paths, or should Android store additional URI metadata for document-tree permissions?
