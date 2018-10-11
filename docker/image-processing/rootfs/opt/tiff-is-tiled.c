#include <tiffio.h>

int main(int argc, char **argv) {

	if (argv[1]) {
		TIFF * tif = TIFFOpen(argv[1], "r");
		if (TIFFIsTiled(tif)) {
			return 0;
		} else
			return 1;
	} else {
		printf("Tiff file must be provided\n");
		return 1;
	}
}
