package com.sirmaenterprise.sep.bpm.camunda.transitions.model;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.delegate.DefaultDelegateInterceptor;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.scripting.ScriptFactory;
import org.camunda.bpm.engine.impl.scripting.engine.BeansResolverFactory;
import org.camunda.bpm.engine.impl.scripting.engine.ResolverFactory;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptBindingsFactory;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.camunda.bpm.engine.impl.scripting.engine.VariableScopeResolverFactory;
import org.camunda.bpm.engine.impl.scripting.env.ScriptEnvResolver;
import org.camunda.bpm.engine.impl.scripting.env.ScriptingEnvironment;
import org.camunda.bpm.engine.impl.variable.serializer.BooleanValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ByteArrayValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.DateValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.DefaultVariableSerializers;
import org.camunda.bpm.engine.impl.variable.serializer.DoubleValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.FileValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.IntegerValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.LongValueSerlializer;
import org.camunda.bpm.engine.impl.variable.serializer.NullValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ShortValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.StringValueSerializer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by hlungov on 18.5.2017 г..
 */
public class CommandExecutorMock extends CommandInterceptor {

	@Mock
	private CommandContext commandContext;

	@Mock
	private ProcessEngineConfigurationImpl processEngineConfiguration;

	public CommandExecutorMock() {
		MockitoAnnotations.initMocks(this);
		DefaultVariableSerializers variableSerializers = new DefaultVariableSerializers();
		variableSerializers.addSerializer(new NullValueSerializer());
		variableSerializers.addSerializer(new StringValueSerializer());
		variableSerializers.addSerializer(new BooleanValueSerializer());
		variableSerializers.addSerializer(new ShortValueSerializer());
		variableSerializers.addSerializer(new IntegerValueSerializer());
		variableSerializers.addSerializer(new LongValueSerlializer());
		variableSerializers.addSerializer(new DateValueSerializer());
		variableSerializers.addSerializer(new DoubleValueSerializer());
		variableSerializers.addSerializer(new ByteArrayValueSerializer());
		variableSerializers.addSerializer(new JavaObjectSerializer());
		variableSerializers.addSerializer(new FileValueSerializer());
		when(processEngineConfiguration.getVariableSerializers()).thenReturn(variableSerializers);
		ScriptFactory scriptFactory = new ScriptFactory();
		when(processEngineConfiguration.getScriptFactory()).thenReturn(scriptFactory);
		when(processEngineConfiguration.getDelegateInterceptor()).thenReturn(new DefaultDelegateInterceptor());
		List<ResolverFactory> resolverFactories = new ArrayList<>();
		resolverFactories.add(new VariableScopeResolverFactory());
		resolverFactories.add(new BeansResolverFactory());
		ScriptingEngines scriptingEngines = new ScriptingEngines(new ScriptBindingsFactory(resolverFactories));
		scriptingEngines.setEnableScriptEngineCaching(false);
		List<ScriptEnvResolver> scriptEnvResolvers = new ArrayList<>();
		ScriptingEnvironment scriptingEnvironment = new ScriptingEnvironment(scriptFactory, scriptEnvResolvers,
																			 scriptingEngines);
		when(processEngineConfiguration.getScriptingEnvironment()).thenReturn(scriptingEnvironment);
		Context.setProcessEngineConfiguration(processEngineConfiguration);
		Context.setCommandContext(commandContext);
	}

	@Override
	public <T> T execute(Command<T> command) {
		return command.execute(null);
	}
}
