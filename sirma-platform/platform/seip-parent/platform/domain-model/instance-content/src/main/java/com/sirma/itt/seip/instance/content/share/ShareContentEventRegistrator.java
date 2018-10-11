package com.sirma.itt.seip.instance.content.share;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.serialization.kryo.KryoInitializer;

import java.util.Arrays;
import java.util.List;

/**
 * Kryo registrator for the {@link ShareInstanceContentEvent}.
 * <p/>
 * When schedule task is created that is triggered from an event the event has to be persisted as well. That's why
 * we register {@link ShareInstanceContentEvent} in kryo so it can be serialized before the persist.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 12/09/2017
 */
@Extension(target = KryoInitializer.TARGET_NAME, order = 10003)
public class ShareContentEventRegistrator implements KryoInitializer {

	@Override
	public List<Pair<Class<?>, Integer>> getClassesToRegister() {
		return Arrays.asList(new Pair<>(ShareInstanceContentEvent.class, 103), new Pair<>(ContentShareData.class, 104));
	}
}
