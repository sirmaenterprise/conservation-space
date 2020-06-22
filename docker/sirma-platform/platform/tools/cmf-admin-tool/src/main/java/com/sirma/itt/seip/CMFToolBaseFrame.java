/*
 *
 */
package com.sirma.itt.seip;

import static com.sirma.itt.seip.PropertyConfigsWrapper.CONFIG_INPUT_EMF_HOST;
import static com.sirma.itt.seip.PropertyConfigsWrapper.CONFIG_INPUT_LAST_CHECKS;
import static com.sirma.itt.seip.PropertyConfigsWrapper.CONFIG_INPUT_LAST_SITEID;
import static com.sirma.itt.seip.PropertyConfigsWrapper.CONFIG_INPUT_SOLR_HOST;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import com.sirma.itt.seip.alfresco4.remote.AbstractRESTClient;
import com.sirma.itt.seip.controlers.InitControler;
import com.sirma.itt.seip.controlers.InitControler.DefinitionType;
import com.sirma.itt.seip.controlers.InitControler.ModuleType;
import com.sirma.itt.seip.controlers.SolrController;
import com.sirma.itt.seip.controlers.TaskThread;

/**
 * The Class CMFToolBaseFrame is main gui element.
 */
public class CMFToolBaseFrame extends JFrame implements PropertyChangeListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7540861603241022983L;

	/** The LOGGER. */
	protected static final Logger LOGGER = Logger.getLogger(CMFToolBaseFrame.class);

	/**
	 * Inits the controlers.
	 */
	protected void initControlers() {
		createRestClient();
	}

	/** The http client. */
	private AbstractRESTClient httpClient;

	/** The info. */
	private JLabel info;

	/** The frame. */
	private CMFToolBaseFrame frame;

	/** The progress. */
	private JProgressBar progress;

	/** The init button. */
	private JButton initButton;

	/** The clear all. */
	private JButton clearAll;

	/** The clear node. */
	private JButton clearNode;

	/** The clear child node. */
	private JButton clearChildNode;

	/** The post request. */
	private JButton postRequest;

	/** The clear task n processed. */
	private JButton clearTaskNProcessed;

	/** The reload definitions. */
	private JButton reloadDefinitions;

	/** The clear int cache. */
	private JButton clearIntCache;

	/** The clear def cache. */
	private JButton clearDefCache;

	/** The clear label cache. */
	private JButton clearLabelCache;

	/** The reload users. */
	private JButton reloadUsers;

	/** The reload templates. */
	private JButton reloadTemplates;

	/** The reload semantic def. */
	private JButton reloadSemanticDef;

	/** The reload codelists. */
	private JButton reloadCodelists;

	/**
	 * The Class UIThreadImpl.
	 */
	private abstract class UIThreadImpl extends TaskThread {

		/*
		 * (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Void doInBackground() throws Exception {
			try {

				InitControler init = new InitControler();
				init.setHttpClient(httpClient);
				doActualWork(init);
			} catch (Exception e) {
				log(e);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.sirma.itt.cmf.controlers.ProgressMonitor#finish()
		 */
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void finish() {
			frame.done();
		}

		/**
		 * Do actual work.
		 *
		 * @param init
		 *            the init
		 * @return the void
		 * @throws Exception
		 *             the exception
		 */
		protected abstract Void doActualWork(InitControler init) throws Exception;
	};

	/**
	 * Create the frame.
	 */
	public CMFToolBaseFrame() {

	}

	/**
	 * Toogle buttons activity.
	 *
	 * @param button
	 *            the button. if passed all buttons are disabled
	 */
	private void toogleButtons(JButton button) {

		if (button != null) {
			initButton.setEnabled(false);
			clearNode.setEnabled(false);
			clearAll.setEnabled(false);
			clearChildNode.setEnabled(false);
			postRequest.setEnabled(false);
			clearTaskNProcessed.setEnabled(false);
		} else {
			initButton.setEnabled(true);
			clearNode.setEnabled(true);
			clearAll.setEnabled(true);
			clearChildNode.setEnabled(true);
			postRequest.setEnabled(true);
			clearTaskNProcessed.setEnabled(true);
		}

	}

	/**
	 * Internal init.Should be invoked on new instances.
	 */
	public void init() {
		frame = this;
		initUI();
		initControlers();
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				PropertyConfigsWrapper.getInstance().store();
			}
		});

		setVisible(true);
	}

	/**
	 * Inits the.
	 */
	public void initUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		final PropertyConfigsWrapper configsWrapper = PropertyConfigsWrapper.getInstance();
		setTitle("SEP Bundle Initialization & Tools. Version:" + configsWrapper.getVersion());
		JPanel contentPane = new JPanel();
		getContentPane().add(contentPane, BorderLayout.CENTER);
		info = new JLabel();
		info.setBounds(0, 0, 225, 20);
		JPanel panelinfo = new JPanel();
		progress = new JProgressBar();
		progress.setBounds(255, 0, 200, 20);
		panelinfo.setLayout(null);
		panelinfo.setBackground(Color.DARK_GRAY);
		panelinfo.add(info);
		panelinfo.add(progress);
		panelinfo.setPreferredSize(new Dimension(100, 20));
		getContentPane().add(panelinfo, BorderLayout.PAGE_END);
		contentPane.setLayout(null);

		JPanel initPanel = new JPanel();
		final boolean pmEnabled = Boolean.TRUE.equals(Boolean.valueOf(configsWrapper.getProperty("pm.enabled")));
		final boolean domEnabled = Boolean.TRUE.equals(Boolean.valueOf(configsWrapper.getProperty("dom.enabled")));
		String initLabel = "";
		initLabel = "CMF";
		if (domEnabled) {
			initLabel += ", DOM";
		}
		if (pmEnabled) {
			initLabel += ", PM";
		}

		initPanel.setBorder(new TitledBorder(null, "Initialization " + initLabel, TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		JPanel toolsPanel = new JPanel();
		toolsPanel.setBorder(new TitledBorder(null, "DMS Tools", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JPanel emfTools = new JPanel();
		emfTools.setBorder(new TitledBorder(null, "EMF Tools", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JPanel semanticTools = new JPanel();
		semanticTools.setBorder(
				new TitledBorder(null, "Semantic Tools", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		initButton = new JButton("Init & Upload");
		initButton.setBounds(330, 25, 110, 35);

		JPanel checkPanel = new JPanel();
		checkPanel.setBounds(10, 25, 346, 48);
		Set<DefinitionType> values = InitControler.DefinitionType
				.getForModule(pmEnabled ? ModuleType.PM : ModuleType.CMF);
		values.addAll(InitControler.DefinitionType.getForModule(domEnabled ? ModuleType.DOM : ModuleType.CMF));
		final List<Checkbox> initTypes = new ArrayList<>();
		// convert to list
		List<String> lastSelected = Arrays.asList(configsWrapper.getProperty(CONFIG_INPUT_LAST_CHECKS).split(", "));
		for (DefinitionType definitionType : values) {
			Checkbox comp = new Checkbox(definitionType.toString());
			initTypes.add(comp);
			if (lastSelected.contains(comp.getLabel())) {
				comp.setState(true);
			}
			checkPanel.add(comp);
		}
		initButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TaskThread taskThread = new UIThreadImpl() {

					@Override
					protected Void doActualWork(InitControler init) throws Exception {
						String site = JOptionPane.showInputDialog("Which is the initilized site?",
								configsWrapper.getProperty(CONFIG_INPUT_LAST_SITEID));
						if (site == null || site.trim().isEmpty()) {
							log("Site information is empty. Would not continue!");
							return null;
						}
						configsWrapper.put(CONFIG_INPUT_LAST_SITEID, site);
						List<DefinitionType> listOfSelectedTypes = new ArrayList<>();
						for (Checkbox checkbox : initTypes) {
							if (checkbox.getState()) {
								listOfSelectedTypes.add(DefinitionType.valueOf(checkbox.getLabel()));
							}
						}
						progress.setVisible(true);
						progress.setIndeterminate(false);
						String checks = listOfSelectedTypes.toString();
						configsWrapper.put(CONFIG_INPUT_LAST_CHECKS, checks.substring(1, checks.length() - 1));
						init.init(site, listOfSelectedTypes, this);
						return null;
					}
				};
				taskThread.addPropertyChangeListener(frame);
				toogleButtons(initButton);
				taskThread.execute();
			}

		});

		initPanel.setBounds(0, 0, 454, 85);
		contentPane.add(initPanel);
		initPanel.setLayout(null);
		initPanel.add(initButton);
		initPanel.add(checkPanel);

		checkPanel.setLayout(new GridLayout(4, 2));

		toolsPanel.setBounds(0, 85, 454, 125);
		contentPane.add(toolsPanel);

		emfTools.setBounds(0, 210, 454, 125);
		contentPane.add(emfTools);
		semanticTools.setBounds(0, 335, 454, 60);
		contentPane.add(semanticTools);
		semanticTools.setBackground(Color.WHITE);

		initPanel.setBackground(Color.WHITE);
		toolsPanel.setBackground(Color.WHITE);
		emfTools.setBackground(Color.WHITE);
		checkPanel.setBackground(Color.WHITE);
		contentPane.setBackground(Color.WHITE);
		clearAll = new JButton("Clear Instances");
		clearAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				TaskThread taskThread = new UIThreadImpl() {

					@SuppressWarnings("unchecked")
					@Override
					protected Void doActualWork(final InitControler init) throws Exception {
						int showConfirmDialog = JOptionPane.showConfirmDialog(clearAll,
								"This will delete all selected instances in the selected site");
						if (showConfirmDialog == JOptionPane.YES_OPTION) {
							List<String> containersList = new ArrayList<>(3);
							containersList.add("cmf");
							if (pmEnabled) {
								containersList.add("pm");
							}
							if (domEnabled) {
								containersList.add("dom");
							}

							final Object[] site = CustomFrames.showOptionDialogWithInput(clearAll,
									"Site name to clear:", configsWrapper.getProperty(CONFIG_INPUT_LAST_SITEID),
									JOptionPane.QUESTION_MESSAGE, null,
									containersList.toArray(new String[containersList.size()]),
									configsWrapper.getProperty(CONFIG_INPUT_LAST_SITEID));
							Object siteId = site[0];
							if (siteId != null) {
								configsWrapper.put(CONFIG_INPUT_LAST_SITEID, siteId.toString());
								int process = JOptionPane.showConfirmDialog(clearAll,
										"Are sure you want to clear instances in site: " + siteId + " ?");
								if (process == JOptionPane.OK_OPTION) {
									progress.setVisible(true);
									progress.setIndeterminate(true);
									init.clearSite(siteId.toString(), (List<String>) site[1]);
								}
							}
						}
						return null;
					}
				};
				taskThread.addPropertyChangeListener(frame);
				toogleButtons(clearAll);
				taskThread.execute();

			}
		});
		clearTaskNProcessed = new JButton("Clear Tasks & Processes");
		clearTaskNProcessed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				TaskThread taskThread = new UIThreadImpl() {

					@Override
					protected Void doActualWork(final InitControler init) throws Exception {
						int showConfirmDialog = JOptionPane.showConfirmDialog(clearAll,
								"This will delete all task & process instances in the selected sever");
						if (showConfirmDialog == JOptionPane.YES_OPTION) {
							JOptionPane.showMessageDialog(initButton, "Not implemented yet!");
						}
						return null;
					}
				};
				taskThread.addPropertyChangeListener(frame);
				toogleButtons(clearTaskNProcessed);
				taskThread.execute();

			}
		});

		clearNode = new JButton("Delete specific node");
		clearNode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				TaskThread taskThread = new UIThreadImpl() {

					@Override
					protected Void doActualWork(final InitControler init) throws Exception {
						int showConfirmDialog = JOptionPane.showConfirmDialog(clearNode,
								"This will delete the specified node");
						if (showConfirmDialog == JOptionPane.YES_OPTION) {
							String node = JOptionPane.showInputDialog("Input the node to delete");
							if (node != null) {
								int process = JOptionPane.showConfirmDialog(clearNode,
										"Are sure you want to delete node: " + node + " ?");
								if (process == JOptionPane.OK_OPTION) {
									progress.setVisible(true);
									progress.setIndeterminate(true);
									init.clearSpecific(node);
								}
							}
						}
						return null;
					}
				};
				taskThread.addPropertyChangeListener(frame);
				toogleButtons(clearNode);
				taskThread.execute();

			}
		});

		clearChildNode = new JButton("Delete children nodes");
		clearChildNode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				TaskThread taskThread = new UIThreadImpl() {

					@Override
					protected Void doActualWork(final InitControler init) throws Exception {
						int showConfirmDialog = JOptionPane.showConfirmDialog(clearChildNode,
								"This will delete all children under the specified node");
						if (showConfirmDialog == JOptionPane.YES_OPTION) {
							String node = JOptionPane.showInputDialog("Input the node to clear under");
							if (node != null) {
								int process = JOptionPane.showConfirmDialog(clearChildNode,
										"Are sure you want to delete children nodes for: " + node + " ?");
								if (process == JOptionPane.OK_OPTION) {
									progress.setVisible(true);
									progress.setIndeterminate(true);
									init.clearChildSpecific(node);
								}
							}
						}
						return null;
					}
				};
				taskThread.addPropertyChangeListener(frame);
				toogleButtons(clearChildNode);
				taskThread.execute();

			}
		});

		postRequest = new JButton("POST Request");
		postRequest.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				TaskThread taskThread = new UIThreadImpl() {

					@Override
					protected Void doActualWork(final InitControler init) throws Exception {
						@SuppressWarnings("unchecked")
						List<String> multipleInput = CustomFrames.showMultipleInput(postRequest,
								"Input the url and data", new String[] { "URL", "Data" },
								new Class[] { JTextField.class, JTextArea.class });
						if (multipleInput != null && multipleInput.size() == 2) {
							init.postRequest(multipleInput.get(0), multipleInput.get(1));
						}
						return null;
					}
				};
				taskThread.addPropertyChangeListener(frame);
				toogleButtons(postRequest);
				taskThread.execute();

			}
		});
		toolsPanel.setLayout(new GridLayout(3, 2, 5, 2));
		toolsPanel.add(clearAll);
		toolsPanel.add(clearTaskNProcessed);
		toolsPanel.add(clearNode);
		toolsPanel.add(clearChildNode);
		toolsPanel.add(postRequest);

		reloadDefinitions = new JButton("Reload definitions");
		reloadDefinitions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				executeServiceCall(configsWrapper, "/emf/service/administration/reloadDefinitions");
			}
		});
		reloadCodelists = new JButton("Reload codelists");
		reloadCodelists.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				executeServiceCall(configsWrapper, "/emf/service/administration/resetCodelists");
			}

		});
		reloadTemplates = new JButton("Reload templates");
		reloadTemplates.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				executeServiceCall(configsWrapper, "/emf/service/administration/reloadTemplates");
			}

		});
		reloadSemanticDef = new JButton("Reload semanic");
		reloadSemanticDef.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				executeServiceCall(configsWrapper, "/emf/service/administration/reloadSemanticDefinitions");
			}

		});
		reloadUsers = new JButton("Reload users");
		reloadUsers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				executeServiceCall(configsWrapper, "/emf/service/resources/refresh?force=true");
			}

		});
		clearIntCache = new JButton("Clear Int cache");
		clearIntCache.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				executeServiceCall(configsWrapper, "/emf/service/administration/clearInternalCache");
			}

		});
		clearDefCache = new JButton("Clear Def cache");
		clearDefCache.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				executeServiceCall(configsWrapper, "/emf/service/administration/clearDefinitionsCache");
			}

		});
		clearLabelCache = new JButton("Clear label cache");
		clearLabelCache.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				executeServiceCall(configsWrapper, "/emf/service/administration/clear-label-cache");
			}

		});

		emfTools.setLayout(new GridLayout(3, 3, 5, 2));
		// TODO: add buttons
		emfTools.add(reloadDefinitions);
		emfTools.add(reloadCodelists);
		emfTools.add(reloadTemplates);
		emfTools.add(reloadSemanticDef);
		emfTools.add(reloadUsers);
		emfTools.add(clearIntCache);
		emfTools.add(clearDefCache);
		emfTools.add(clearLabelCache);

		semanticTools.setLayout(new GridLayout(1, 2, 5, 2));
		final JButton solrSchemaUpdate = new JButton("Update Solr Schema");
		solrSchemaUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					solrSchemaUpdate.setEnabled(false);
					executeSolrCall(configsWrapper);
				} finally {
					solrSchemaUpdate.setEnabled(true);
				}
			}

		});
		semanticTools.add(solrSchemaUpdate);
		semanticTools.add(new JLabel());
		setPreferredSize(new Dimension(463, 440));
		setResizable(false);
		pack();
	}

	/**
	 * Creates the rest client.
	 */
	private void createRestClient() {
		httpClient = new AbstractRESTClient();
		httpClient.setUseAuthentication(true);

		PropertyConfigsWrapper configsWrapper = PropertyConfigsWrapper.getInstance();
		String user = configsWrapper.getProperty("user");
		httpClient.setDefaultCredentials(configsWrapper.getProperty("host"),
				Integer.valueOf(configsWrapper.getProperty("port")), user, configsWrapper.getProperty("pass"));
		httpClient.setProtocol(configsWrapper.getProperty("protocol.dms", AbstractRESTClient.PROTOCOL_HTTP));
		info.setForeground(Color.WHITE);
		info.setText("User:   " + user + "@" + httpClient.getHost() + ":" + httpClient.getPort());

	}

	/**
	 * Creates the rest client.
	 *
	 * @param host
	 *            the host
	 * @return the abstract rest client
	 */
	private AbstractRESTClient createRestClient(String host) {
		String[] split = host.split(":");
		AbstractRESTClient client = new AbstractRESTClient();
		client.setUseAuthentication(true);
		client.setUseDmsServiceBase(false);
		PropertyConfigsWrapper configsWrapper = PropertyConfigsWrapper.getInstance();
		String user = configsWrapper.getProperty("user");
		client.setDefaultCredentials(split[0], Integer.valueOf(split[1]), user, configsWrapper.getProperty("pass"));
		client.setProtocol(configsWrapper.getProperty("protocol.sep", AbstractRESTClient.PROTOCOL_HTTP));
		return client;
	}

	/**
	 * Execute service call.
	 *
	 * @param configsWrapper
	 *            the configs wrapper
	 * @param service
	 *            the service
	 */
	private void executeServiceCall(final PropertyConfigsWrapper configsWrapper, final String service) {
		TaskThread taskThread = new UIThreadImpl() {

			@Override
			protected Void doActualWork(InitControler init) throws Exception {
				String site = JOptionPane.showInputDialog("Target EMF server:port?",
						configsWrapper.getProperty(CONFIG_INPUT_EMF_HOST, "localhost:8080"));
				if (site == null) {
					return null;
				}
				if (site.trim().isEmpty()) {
					throw new IllegalArgumentException(
							"Server information is invalid. Required format is -> host:port");
				}
				if (!isValidHostAndPort(site)) {
					throw new IllegalArgumentException(
							"Server information is invalid. Required format is -> host:port");
				}
				configsWrapper.put(CONFIG_INPUT_EMF_HOST, site);

				progress.setVisible(true);
				progress.setIndeterminate(false);

				init.setHttpClient(createRestClient(site));

				init.getRequest(service);
				return null;
			}

		};
		taskThread.addPropertyChangeListener(frame);
		toogleButtons(reloadDefinitions);
		taskThread.execute();
	}

	/**
	 * Execute solr call to the specified service configured in 'solr.requests'.Single request is separated with | to
	 * the others. Request uri is split from the body by #
	 *
	 * @param configsWrapper
	 *            the configs wrapper
	 */
	private void executeSolrCall(final PropertyConfigsWrapper configsWrapper) {
		TaskThread taskThread = new UIThreadImpl() {

			@Override
			protected Void doActualWork(InitControler init) throws Exception {
				SolrController solrController = new SolrController();
				String host = JOptionPane.showInputDialog("Target Solr server:port/solr/{corename}?",
						configsWrapper.getProperty(CONFIG_INPUT_SOLR_HOST, "http://localhost:8983/solr/ftsearch"));
				if (host == null) {
					return null;
				}
				if (host.trim().isEmpty()) {
					throw new IllegalArgumentException(
							"Server information is invalid. Required format is -> host:port");
				}
				// if (!isValidHostAndPort(host)) {
				// throw new IllegalArgumentException(
				// "Server information is invalid. Required format is -> host:port");
				// }
				progress.setVisible(true);
				progress.setIndeterminate(true);
				configsWrapper.put(CONFIG_INPUT_SOLR_HOST, host);
				solrController.executeSolrCall(CMFToolBaseFrame.this, host);
				return null;
			}

		};
		taskThread.addPropertyChangeListener(frame);
		toogleButtons(reloadDefinitions);
		taskThread.execute();
	}

	/**
	 * Checks if is valid host and port.
	 *
	 * @param site
	 *            the site
	 * @return true, if is valid host and port
	 */
	private boolean isValidHostAndPort(String site) {
		String[] split = site.split(":");
		if (split.length != 2) {
			return false;
		}
		if (split[0].trim().length() < 1) {
			return false;
		}
		try {
			Integer.valueOf(split[1]);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * Log.
	 *
	 * @param e
	 *            the error
	 */
	public void log(Exception e) {
		LOGGER.error(e);
		JOptionPane.showMessageDialog(this, e.getMessage());
	}

	/**
	 * Log.
	 *
	 * @param message
	 *            the message
	 */
	public void log(String message) {
		LOGGER.info(message);
		JOptionPane.showMessageDialog(this, message);
	}

	/*
	 * (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans. PropertyChangeEvent)
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progressDone = (Integer) evt.getNewValue();
			progress.setIndeterminate(false);
			progress.setValue(progressDone);
		}
	}

	/**
	 * Done.
	 */
	public void done() {
		progress.setVisible(false);
		toogleButtons(null);
	}
}
