package com.sirma.sep.ocr;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Extend this class to test in service env.
 * 
 * @author bbanchev
 */
@ContextConfiguration
@ActiveProfiles({ "service" })
@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class ServiceTest {
	// no base impl
}