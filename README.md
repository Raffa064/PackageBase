# About

This project is like a "database", specifically to store android packages.

My objective is to use it on a android launcher, to load it application list much faster as possible.

# Architeture
- 100 bytes header 
    - **3 bytes**   : Store "pkb" as file type indentifier
    - **4 bytes**   : Version number
    - **8 bytes**   : Id counter/register
    - **4 bytes**   : Number of wiped entries
    - **4 bytes**   : Minimum wiped entry index
    - *Other bytes are saved for future versions*
- 719 bytes **for each** Entry
	- **1 byte**    : Wiped flag
	- **8 bytes**   : Entry identifier (id)
	- **200 bytes** : Entry name (up to 100 chars)
	- **510 bytes** : Entry package name (up to 255 chars)

----

## Wiped entries
Each entry is stored in the memory, and if you delete an entry from the entry list, it will trigger a cascade operation to move down all the entries before that.

To prevent it, we only use an *wiped flag* on the deleted entries, and it can be replaced with new entrie when necessary.
![Animation](./wiped-replaced.gif)
