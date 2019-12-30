Unidown
=======
Unidown (short for "Universal PDF Download") is a simple Android app designed to automatically download a PDF from a given URL whenever it has changed (or when it's newly created). It was designed for easy and up-to-date access to lecture notes and exercise sheets from your smartphone, but can easily be adapted to different (more complex) use cases.

Examples
--------
Unidown can download lecture notes whenever the lecturer has updated them. E.g., you could tell it that there are lecture notes at `http://example.com/notes.pdf` and that it should always store the up-to-date version under a name like `Scriptum.pdf`. It will notify you when the file was changed. HTTP Password Authentication is supported.

If you want Unidown to download exercise sheets for you, you can specify a (single) variable that will be looped over inside the URL, so, e.g., it can consider the files `http://example.com/sheet${i}.pdf` where `i=0,...,15`.

Some Details
------------
Files are stored in a folder named `Unidown` in Android's "external storage". The app provides you with a chronological list of the files in that folder for easy access, but they can, of course, be accessed from any app like any downloaded file.

It should be easy to adapt the app to your needs, e.g. by implementing varying procedures for where to look for PDFs.

Download
--------
Once you've cloned this repository, the app can be compiled from Android Studio, or by running

    ./gradlew assemble

in the root of the repository from a command line, provided you have the necessary Android frameworks installed.
