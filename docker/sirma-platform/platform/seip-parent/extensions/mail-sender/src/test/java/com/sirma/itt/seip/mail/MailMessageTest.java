package com.sirma.itt.seip.mail;

import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.mail.attachments.MailAttachment;

/**
 * Tests for MailMessage.
 *
 * @author Boyan Tonchev
 */
@SuppressWarnings("static-method")
public class MailMessageTest {

	/**
	 * Test for method setAttachments().
	 */
	@Test(dataProvider = "setAttachmentsDP")
	public void setAttachmentsTest(MailAttachment[] mailAttachment, MailAttachment[] expectedResult) {
		MailMessage message = new MailMessage();
		message.setAttachments(mailAttachment);
		Assert.assertEquals(message.getAttachments(), expectedResult);

	}

	/**
	 * Data provider for setAttachmentsTest.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	public Object[][] setAttachmentsDP() {

		MailAttachment attachmentOne = Mockito.mock(MailAttachment.class);
		MailAttachment attachmentTwo = Mockito.mock(MailAttachment.class);

		return new Object[][] {
			{null, null},
			{new MailAttachment[] {attachmentOne, attachmentTwo}, new MailAttachment[] {attachmentOne, attachmentTwo}}
		};
	}

	/**
	 * Test for method setCcRecipients().
	 */
	@Test(dataProvider = "setCcRecipientsDP")
	public void setCcRecipientsTest(String[] ccRecipients, String[] expectedResult) {
		MailMessage message = new MailMessage();
		message.setCcRecipients(ccRecipients);
		assertStringArray(message.getCcRecipients(), expectedResult);

	}

	/**
	 * Data provider for setCcRecipientsDP.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	public Object[][] setCcRecipientsDP() {
		return new Object[][] {
			{null, new String[0]},
			{new String[] {"emailOne", "emailTwo"}, new String[] {"emailOne", "emailTwo"}},
			{new String[] {"emailOne", "emailTwo"}, new String[] {"emailTwo", "emailOne"}}
		};
	}


	/**
	 * Test for method setRecipients().
	 */
	@Test(dataProvider = "setRecipientsDP")
	public void setRecipientsTest(String[] recipients, String[] expectedResult) {
		MailMessage message = new MailMessage();
		message.setRecipients(recipients);
		assertStringArray(message.getRecipients(), expectedResult);

	}

	/**
	 * Data provider for setRecipientsTest.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	public Object[][] setRecipientsDP() {
		return new Object[][] {
			{null, null},
			{new String[] {"emailOne", "emailTwo"}, new String[] {"emailOne", "emailTwo"}},
			{new String[] {"emailOne", "emailTwo"}, new String[] {"emailTwo", "emailOne"}}
		};
	}

	/**
	 * Assert two string arrays.
	 * @param firstString
	 * @param secondString
	 */
	private void assertStringArray(String[] firstString, String[] secondString) {

		if ((firstString == null && secondString != null) || (firstString != null && secondString == null)) {
			Assert.fail();
			return;
		}
		if (firstString != null && secondString != null) {
			Assert.assertEquals(firstString.length, secondString.length);
			List<String> secondStringAsList = Arrays.asList(secondString);
			for (String value: firstString) {
				if (!secondStringAsList.contains(value)) {
					Assert.fail();
				}
			}
		}

	}
}
