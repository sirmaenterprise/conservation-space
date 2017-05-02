/**
 *
 */
package com.sirma.itt.seip.script.extensions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.Test;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptEvaluator;
import com.sirma.itt.seip.script.ScriptTest;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * @author BBonev
 */
public class ScriptEvaluatorBindingProviderTest extends ScriptTest {

	@InjectMocks
	ScriptEvaluatorBindingProvider provider;
	@Spy
	TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	ScriptEvaluator evaluator;

	@Override
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		super.provideBindings(bindingsExtensions);
		bindingsExtensions.add(provider);
	}

	@Test
	public void test_base_InCurrentTx() {

		eval(" script.createConfig().async().script('2+2').run(); ");

		verify(transactionSupport, never()).invokeAfterTransactionCompletionInTx(any(Executable.class));
		verify(evaluator).scheduleEval(eq("2+2"), anyMap(), eq(false), eq(0L), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void test_base_InCurrentTx_externalRun() {

		eval(" var config = script.createConfig().async().script('2+2'); script.execute(config); ");

		verify(transactionSupport, never()).invokeAfterTransactionCompletionInTx(any(Executable.class));
		verify(evaluator).scheduleEval(eq("2+2"), anyMap(), eq(false), eq(0L), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void test_base_AfterCurrentTx() {

		eval(" script.createConfig().async().script('2+2').afterCurrentTransaction().run(); ");

		verify(transactionSupport).invokeAfterTransactionCompletionInTx(any(Executable.class));
		verify(evaluator).scheduleEval(eq("2+2"), anyMap(), eq(false), eq(0L), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void test_base_AfterCurrentTx_persisted() {

		eval(" script.createConfig().async().script('2+2').afterCurrentTransaction().persisted().run(); ");

		verify(transactionSupport).invokeAfterTransactionCompletionInTx(any(Executable.class));
		verify(evaluator).scheduleEval(eq("2+2"), anyMap(), eq(true), eq(0L), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void test_base_AfterCurrentTx_persisted_WithDelay() {

		eval(" script.createConfig().async().script('2+2').afterCurrentTransaction().persisted().after(10).run(); ");

		verify(transactionSupport).invokeAfterTransactionCompletionInTx(any(Executable.class));
		verify(evaluator).scheduleEval(eq("2+2"), anyMap(), eq(true), eq(10L), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void test_base_AfterCurrentTx_persisted_WithDelay_unit() {

		eval(" script.createConfig().async().script('2+2').afterCurrentTransaction().persisted().after(10, java.util.concurrent.TimeUnit.SECONDS).run(); ");

		verify(transactionSupport).invokeAfterTransactionCompletionInTx(any(Executable.class));
		verify(evaluator).scheduleEval(eq("2+2"), anyMap(), eq(true), eq(10L), eq(TimeUnit.SECONDS));
	}

	@Test
	public void test_base_AfterCurrentTx_persisted_WithDelay_string() {

		eval(" script.createConfig().async().script('2+2').afterCurrentTransaction().persisted().after(10, 'SECONDS').run(); ");

		verify(transactionSupport).invokeAfterTransactionCompletionInTx(any(Executable.class));
		verify(evaluator).scheduleEval(eq("2+2"), anyMap(), eq(true), eq(10L), eq(TimeUnit.SECONDS));
	}

	@Test
	public void test_base_AfterCurrentTx_persisted_WithDelay_unit_bindig() {

		eval(" script.createConfig().async().script('2+2').afterCurrentTransaction().persisted().after(10, java.util.concurrent.TimeUnit.SECONDS).addBinding('test', 'value').run(); ");

		verify(transactionSupport).invokeAfterTransactionCompletionInTx(any(Executable.class));
		Map<String, Object> bindig = new HashMap<>();
		bindig.put("test", "value");
		verify(evaluator).scheduleEval(eq("2+2"), eq(bindig), eq(true), eq(10L), eq(TimeUnit.SECONDS));
	}

	@Test
	public void test_sync_AfterCurrentTx() {

		eval(" script.createConfig().script('2+2').afterCurrentTransaction().run(); ");

		verify(transactionSupport).invokeAfterTransactionCompletionInTx(any(Executable.class));
		verify(evaluator).eval(eq("2+2"), anyMap());
	}

	@Test
	public void test_sync_InCurrentTx() {

		eval(" script.createConfig().script('2+2').run(); ");

		verify(transactionSupport, never()).invokeAfterTransactionCompletionInTx(any(Executable.class));
		verify(evaluator).eval(eq("2+2"), anyMap());
	}
}
