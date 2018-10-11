Requirements:
libtiff4-dev package

Compilation:
cc -o tiff-is-tiled tiff-is-tiled.c -ltiff

Originally suggested - clang -o tiff-is-tiled tiff-is-tiled.c -I/usr/local/include -ltiff

Execution:
./tiff-is-tiled <path to the tiff>