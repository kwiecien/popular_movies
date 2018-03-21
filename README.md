# Popular Movies App
1. Please provide your **TMDb API Key** in `res/values/apis.xml` and run in console `git update-index --skip-worktree <file>` in order to not track changes to this file.
2. To view SQLite database while debugging go to chrome://inspect (**Stetho** debug bridge)
3. To view the content of Cursor, call the method `DatabaseUtils.dumpCursorToString(Cursor)` from a specific class
   just for debugging Cursors - **DatabaseUtils**.