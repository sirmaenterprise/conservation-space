package com.sirma.itt.seip.adapters.iiif;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.json.Json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.adapters.iiif.rest.ImageRestService;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;

/**
 * Service that computes the physical scale ratio of image and the canvas in which is visualized canvas
 *
 * @author radoslav
 */
public class ImagePhysicalScaleServiceImpl implements ImagePhysicalScaleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageRestService.class);

	private static final String PHYSICAL_SCALE = "physicalScale";
	private static final String PHYSICAL_UNITS = "physicalUnits";
	private static final String IMAGE_WIDTH_PROPERTY = "widthOfSubject";
	private static final String IMAGE_UNIT_PROPERTY = "dimensionUnit";

	private static final Map<String, Double> toCentimetre;

	static {
		toCentimetre = new HashMap<>();
		toCentimetre.put("mm", 0.1);
		toCentimetre.put("cm", 1.0);
		toCentimetre.put("m", 100.0);
	}

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public PhysicalScale getPhysicalScale(String imageId, Dimension<Integer> canvas) {
		Optional<Double> scale = computeScale(getPhysicalDimensions(imageId), canvas);
		if (scale.isPresent()) {
			return new PhysicalScale(Json
					.createObjectBuilder()
						.add(PHYSICAL_SCALE, scale.get().doubleValue())
						.add(PHYSICAL_UNITS, "cm")
						.build());
		}
		LOGGER.warn("No physical dimension information found for image with id={}", imageId);
		return null;
	}

	private static Optional<Double> computeScale(Optional<PhysicalDimension> physicalDimensions,
			Dimension<Integer> canvas) {
		return physicalDimensions
				.map(dimensions -> convertPhysicalDimension(dimensions))
					.filter(PhysicalDimension::isValid)
					.map(converted -> converted.getWidth() / canvas.getWidth())
					.filter(scale -> !scale.isInfinite() && !scale.isNaN());
	}

	/**
	 * Converts any physical dimension to cm
	 *
	 * @param dimension
	 * @return the converted dimension
	 */
	private static PhysicalDimension convertPhysicalDimension(PhysicalDimension dimension) {
		Double unitMultiplier = toCentimetre.get(dimension.getMeasurementUnit());
		return new PhysicalDimension(dimension.getWidth() * unitMultiplier, dimension.getHeight() * unitMultiplier,
				"cm");
	}

	private Optional<PhysicalDimension> getPhysicalDimensions(String imageId) {
		return instanceTypeResolver
				.resolveReference(imageId)
					.map(InstanceReference::toInstance)
					.map(inst -> new PhysicalDimension(inst.getAsDouble(IMAGE_WIDTH_PROPERTY),
							inst.getString(IMAGE_UNIT_PROPERTY)))
					.filter(PhysicalDimension::isValid);
	}

}
