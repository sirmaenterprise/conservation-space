FROM ubuntu:16.04

COPY rootfs/ /

RUN \
	# Update packages and install tools
	apt-get update -y && apt-get install -y  \
	build-essential \
	git \
	python2.7 \
	pkg-config \
	checkinstall \
	wget && \
\
	# Install and setup image processing
	# rawtherapee
	apt-get install software-properties-common -y&& \
	add-apt-repository ppa:dhor/myway && \
	apt-get update && \
	apt-get install rawtherapee --no-install-recommends -y && \
\
	#vips
	apt-get install libvips-tools --no-install-recommends -y && \
	# openjpeg (j2k_to_image, image_to_j2k)
	apt-get install openjpeg-tools --no-install-recommends -y && \
\
	# ghostscript ghostscript-font are required for pdf conversion
    # ufraw-batch is required for .nef (and probably other raw) files
    apt-get install ghostscript gsfonts ufraw-batch -y --no-install-recommends && \
\
	# imagemagick
	cd /opt && \
	apt-get build-dep graphicsmagick-imagemagick-compat -y && \
	IMAGE_MAGIC=ImageMagick-7.0.7-39 && \
	wget http://www.imagemagick.org/download/releases/${IMAGE_MAGIC}.tar.xz && \
	tar xvf ${IMAGE_MAGIC}.tar.xz && cd ${IMAGE_MAGIC} && \
	./configure --prefix=/opt/imagemagick-7.0 && make && make install &&  checkinstall -y && make check && \
	ln -s /opt/imagemagick-7.0/bin/convert /usr/bin/ && \
	ln -s /opt/imagemagick-7.0/bin/identify /usr/bin/ && \
	cd ../ && \
	rm -f ${IMAGE_MAGIC}.tar.xz && rm -rf ${IMAGE_MAGIC} &&	\
\
	# Compile & make runnable image processing source
	cc -o /opt/tiff-is-tiled /opt/tiff-is-tiled.c -ltiff && \
	chmod +x /opt/image-processing.py /opt/tiff-is-tiled && \
\
	# Remove intermediate packages and clean-up apt-get cache
	apt-get autoremove -y --purge software-properties-common build-essential checkinstall git wget pkg-config && \
	apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*


# -u is required for python to redirect properly the output to Docker's stdout
CMD ["python", "-u", "/opt/image-processing.py"]
