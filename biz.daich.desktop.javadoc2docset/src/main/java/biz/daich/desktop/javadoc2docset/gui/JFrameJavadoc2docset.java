package biz.daich.desktop.javadoc2docset.gui;

import static biz.daich.desktop.javadoc2docset.CoreActions.docsetNameFromJavaDocJarName;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.adapter.SingleListSelectionAdapter;
import com.jgoodies.binding.adapter.TextComponentConnector;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.BindingConverter;
import com.jgoodies.binding.value.ConverterValueModel;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.common.collect.ArrayListModel;
import com.l2fprod.common.swing.JDirectoryChooser;

import biz.daich.desktop.javadoc2docset.CoreActions;
import biz.daich.desktop.javadoc2docset.CoreActions.O;
import biz.daich.desktop.javadoc2docset.DocsetTask;
import net.miginfocom.swing.MigLayout;

public class JFrameJavadoc2docset
{
	private static final String					LAMP_GIF					= "/lamp.gif";

	private static final String					CLEAR_GIF					= "/clear.gif";

	private static final Logger					l							= LogManager.getLogger(JFrameJavadoc2docset.class.getName());

	protected static final File2StringConverter	f2sConverter				= new File2StringConverter();
	protected static final File					defaultStartDir				= new File("c:/");
	protected static final BufferedImage		EmptyImage					= new BufferedImage(64, 64, BufferedImage.TYPE_3BYTE_BGR);

	protected JFrame							frame;
	protected JButton							btnTmpDir;
	protected JTextField						txtTmpDir;
	protected JTextField						txtJavadocJar;
	protected JButton							btnJavadocjarfile;
	protected JDirectoryChooser					dirChooser;
	protected JTextField						txtDocsetName;
	protected JTextField						txtDisplayName;
	protected JTextField						txtPrefix;
	protected JButton							btnSetNames;
	protected JTextField						txtNameFromJar;
	protected JCheckBox							chckbxVerbose;
	protected JCheckBox							chckbxKeepUnpackedJar;
	protected JTextField						txtOutDir;
	protected JButton							btnGo;

	protected JTextField						txtIconFile;
	protected JButton							btnIconfile;
	protected JFileChooser						fileChooser;

	protected ValueHolder						jarFileHolder				= new ValueHolder();
	protected ValueHolder						iconFileHolder				= new ValueHolder();
	protected ValueHolder						tmpFileHolder				= new ValueHolder();
	protected ValueHolder						outFileHolder				= new ValueHolder();
	protected ValueHolder						pauseHolder					= new ValueHolder();
	protected JButton							btnOutDir;

	protected FileNameExtensionFilter			jarFileNameExtensionFilter	= new FileNameExtensionFilter("Jar files", "jar");
	protected FileNameExtensionFilter			pngFileNameExtensionFilter	= new FileNameExtensionFilter("32x32 PNG files", "png");
	protected JTextArea							loggingConsole;
	protected JButton							btnClearDocsetName;
	protected JButton							btnClearDisplayName;
	protected JButton							btnClearTmpDir;
	protected JButton							btnClearOutDir;
	protected JButton							btnClearJarFile;
	protected JButton							btnClearPrefix;
	protected JButton							btnUpdateName;

	protected JScrollPane						scrollPaneTblTasks;
	protected JTable							tblTasks;
	protected JButton							btnAdd;
	protected JButton							btnRunAll;
	protected JScrollPane						scrollPaneStatusTestArea;

	protected PausableThreadPoolExecutor		pausableExecutorService;

	protected ArrayListModel<DocsetTask>		arrayListModel;
	protected SelectionInList<DocsetTask>		selectionInList;
	protected JLabel							lblIcon;

	protected ImageIcon							imageIcon;
	protected JButton							btnClearAll;
	protected JButton							btnClearDone;
	protected JButton							btnClearLog;

	static final String[]						columnNames					= new String[] { "jar", "display name", "docset Name", "icon File", "output dir", "javadoc Dir", "keep tmp", "icon",
			"isDone", "time", "delete" };
	protected JPanel							panel;
	protected JPanel							panelTmpDir;
	protected JPanel							panelJarFile;
	protected JPanel							panelNameFromJarFile;
	protected JPanel							panelIcon;
	protected JPanel							panelDocsetName;
	protected JPanel							panelDisplayName;
	protected JPanel							panelPrefix;
	protected JPanel							panelOptions;
	protected JPanel							panelDocsetTasksTable;
	protected JPanel							panelLogConsole;
	protected JPanel							panelDocsetTask;
	protected JButton							btnExit;

	/**
	 * Create the application.
	 *
	 * @wbp.parser.entryPoint
	 */
	public JFrameJavadoc2docset()
	{
		initialize();
		initBindings();
		tmpFileHolder.setValue(new File("c:/tmp/"));
		jarFileHolder.setValue(new File("c:/tmp/spring-security-data-4.1.0.RELEASE-javadoc.jar"));
		pauseHolder.setValue(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	protected void initialize()
	{
		frame = new JFrame();
		frame.setBounds(100, 100, 1095, 1138);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[][][grow][fill]", "[][][grow][grow][]"));
		frame.getContentPane().add(getPanelDocsetTask(), "cell 1 0,alignx left,growy");
		frame.getContentPane().add(getPanelDocsetTasksTable(), "cell 1 2 2 1,grow");
		frame.getContentPane().add(getPanelLogConsole(), "cell 1 3 2 1,grow");
		frame.getContentPane().add(getBtnExit(), "cell 2 4,alignx right");
	}

	protected void initBindings()
	{
		TextComponentConnector.connect(new ConverterValueModel(jarFileHolder, f2sConverter), getTxtJavadocJar());
		TextComponentConnector.connect(new ConverterValueModel(iconFileHolder, f2sConverter), getTxtIconFile());
		TextComponentConnector.connect(new ConverterValueModel(tmpFileHolder, f2sConverter), getTxtTmpDir());
		TextComponentConnector.connect(new ConverterValueModel(outFileHolder, f2sConverter), getTxtOutDir());
		tmpFileHolder.addValueChangeListener(e -> outFileHolder.setValue(tmpFileHolder.getValue()));

		jarFileHolder.addValueChangeListener(evt -> nameFromJarName());

		iconFileHolder.addValueChangeListener(e ->
			{
				File iconFile = (File) iconFileHolder.getValue();
				try
				{
					BufferedImage biIcon = ImageIO.read(iconFile);
					if (biIcon != null)
					{
						getLblIcon().setIcon(new ImageIcon(biIcon));
						l.debug("new icon selected " + iconFile.getAbsolutePath());
						return;
					}
				}
				catch (IOException e1)
				{
					l.error("FAILURE to load image " + iconFile.getAbsolutePath() + " REASON: " + e1.getMessage(), e1);
				}

				getLblIcon().setIcon(new ImageIcon(EmptyImage));
				l.warn("bad icon selected setting an empty one");
			});
		pauseHolder.addValueChangeListener(a ->
			{
				if ((Boolean) pauseHolder.getValue())
					getPausableExecutorService().pause();
				else
					getPausableExecutorService().resume();
			});
		pauseHolder.addValueChangeListener(a ->
			{
				if ((Boolean) pauseHolder.getValue())
					getBtnRunAll().setText("Resume Run");
				else
					getBtnRunAll().setText("Pause Run");
			});
	}

	public ArrayListModel<DocsetTask> getTasksListModel()
	{
		if (arrayListModel == null)
		{
			arrayListModel = new ArrayListModel<>();

		}
		return arrayListModel;
	}

	@SuppressWarnings("unchecked")
	public SelectionInList<DocsetTask> getSelectionInList()
	{
		if (selectionInList == null)
		{
			selectionInList = new SelectionInList<>((ListModel<DocsetTask>) getTasksListModel());
		}
		return selectionInList;
	}

	class DocsetTaskTableAdapter extends AbstractTableAdapter<DocsetTask>
	{

		public DocsetTaskTableAdapter(ListModel<DocsetTask> listModel)
		{
			super(listModel, columnNames);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			DocsetTask o = getRow(rowIndex);
			switch (columnIndex) {
				case 0:
					return o.jarFile;
				case 2:
					return o.displayName;
				case 1:
					return o.docsetName;
				case 3:
					return o.iconFile;
				case 4:
					return o.outputLocation;
				case 5:
					return o.javadocRoot;
				case 6:
					return o.keepTmp;
				case 7:
					ImageIcon ii = new ImageIcon(o.iconFile.getAbsolutePath());
					return ii;
				case 8:
					return o.isDone();
				case 9:
					return o.getTook();
				case 10:
					return "X";

				default:
					return "";
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			switch (columnIndex) {
				case 10:
					return true;
				default:
					return super.isCellEditable(rowIndex, columnIndex);
			}

		}

		protected final Action deleteAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				DocsetTask selection = getSelectionInList().getSelection();
				if (selection != null)
				{
					getTasksListModel().remove(selection);
				}
			}
		};

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			switch (columnIndex) {
				case 7:
					return Icon.class;
				default:
					return super.getColumnClass(columnIndex);
			}
		}

	}

	protected void nameFromJarName()
	{
		File jar = (File) jarFileHolder.getValue();
		String nameForJar = (jar == null) ? "----" : docsetNameFromJavaDocJarName(jar.getName());
		getTxtNameFromJar().setText(nameForJar);
	}

	public DocsetTask getValue()
	{
		DocsetTask o = new DocsetTask();

		o.displayName = getTxtDisplayName().getText();
		o.docsetName = getTxtDocsetName().getText();
		o.iconFile = (File) iconFileHolder.getValue();
		o.jarFile = (File) jarFileHolder.getValue();
		o.keepTmp = getChckbxKeepUnpackedJar().isSelected();

		File outdir = (File) outFileHolder.getValue();
		if (outdir != null)
		{
			o.outputLocation = outdir.toPath();
		}

		o.prefix = getTxtPrefix().getText();

		File tmpdir = (File) outFileHolder.getValue();
		if (tmpdir != null)
		{
			o.tmpDir = tmpdir.toPath();
		}

		o.verbose = getChckbxVerbose().isSelected();

		return o;
	}

	static class File2StringConverter implements BindingConverter<File, String>
	{

		@Override
		public String targetValue(File sourceValue)
		{
			return (sourceValue == null) ? null : sourceValue.getAbsolutePath();
		}

		@Override
		public File sourceValue(String targetValue)
		{
			return new File(targetValue);
		}
	}

	public JFileChooser getFileChooser()
	{

		if (fileChooser == null)
		{
			Stopwatch stopwatch = Stopwatch.createStarted();
			fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setPreferredSize(new Dimension(1000, 1000));
			stopwatch.stop();
			l.debug("fileChooser creation took  " + stopwatch);
		}

		return fileChooser;
	}

	public JDirectoryChooser getDirChooser()
	{
		if (dirChooser == null)
		{
			dirChooser = new JDirectoryChooser();
			dirChooser.setPreferredSize(new Dimension(1000, 1000));
		}
		return dirChooser;
	}

	public JButton getBtnTmpDir()
	{
		if (btnTmpDir == null)
		{
			btnTmpDir = new JButton("...");
			btnTmpDir.addActionListener(new DirSelectActionListener(tmpFileHolder));
		}
		return btnTmpDir;
	}

	class DirSelectActionListener implements ActionListener
	{
		final ValueHolder	fileHolder;
		JDirectoryChooser	chooser	= getDirChooser();

		public DirSelectActionListener(ValueHolder theFileHolder)
		{
			fileHolder = theFileHolder;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{

			File currentFile = (File) jarFileHolder.getValue();
			if (currentFile != null)
			{
				chooser.setSelectedFile(currentFile);
			}
			else
			{
				chooser.setSelectedFile(defaultStartDir);
			}
			int choice = chooser.showOpenDialog(frame);
			if (choice == JDirectoryChooser.CANCEL_OPTION)
			{
				l.debug("User Canceled");
			}
			else
			{
				File selectedDir = chooser.getSelectedFile();
				fileHolder.setValue(selectedDir);
				l.debug("Dialog Selection: " + selectedDir.getAbsolutePath());
			}

		}

	}

	protected class BtnSetNamesActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			String prefix = getTxtPrefix().getText();
			String name = getTxtNameFromJar().getText();

			String fullName = Strings.isNullOrEmpty(prefix) ? name : prefix.trim() + " " + name;

			String displayName = getTxtDisplayName().getText();
			String docksetName = getTxtDocsetName().getText();

			docksetName = Strings.isNullOrEmpty(docksetName) ? fullName : docksetName;
			displayName = Strings.isNullOrEmpty(displayName) ? docksetName : displayName;

			getTxtDisplayName().setText(displayName);
			getTxtDocsetName().setText(docksetName);
		}
	}

	protected class BtnFileSelectionActionListener implements ActionListener
	{
		final ValueHolder				fileHolder;
		final FileNameExtensionFilter	extFilter;

		public BtnFileSelectionActionListener(ValueHolder theFileHolder, FileNameExtensionFilter theExtFilter)
		{
			fileHolder = theFileHolder;
			extFilter = theExtFilter;
		}

		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JFileChooser chooser = getFileChooser();
			chooser.setFileFilter(extFilter);
			File jarFile = (File) jarFileHolder.getValue();
			if (jarFile != null)
			{
				chooser.setCurrentDirectory(jarFile);
			}
			else
			{
				chooser.setCurrentDirectory(defaultStartDir);
			}
			int returnVal = chooser.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File selectedFile = chooser.getSelectedFile();
				l.debug("You chose to open this file: " + selectedFile.getName());
				fileHolder.setValue(selectedFile);
			}
		}
	}

	protected class BtnGoActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			try
			{
				O o = getValue();
				getBtnGo().setText("Running...");
				getBtnGo().setEnabled(false);
				CoreActions.go(o);

				//
			}
			catch (IOException e1)
			{
				l.error("actionPerformed(ActionEvent)", e1); //$NON-NLS-1$
			}
			getBtnGo().setText("Run Now!");
			getBtnGo().setEnabled(true);
		}
	}

	protected class BtnAddActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			DocsetTask o = getValue();
			if (o != null)
			{

				try
				{
					CoreActions.validateInputAndSetDefaults(o);
					CoreActions.prepareWorkArea(o);
					Future<DocsetTask> docsetTaskFuture = getPausableExecutorService().submit(o);
					o.setFuture(docsetTaskFuture);
					getTasksListModel().add(o);
				}
				catch (IOException e1)
				{
					l.error("actionPerformed(ActionEvent)", e1); //$NON-NLS-1$
				}
			}
		}
	}

	public JTextField getTxtTmpDir()
	{
		if (txtTmpDir == null)
		{
			txtTmpDir = new JTextField();
			txtTmpDir.setName("txtTmpDir");
			txtTmpDir.setColumns(10);
		}
		return txtTmpDir;
	}

	public JTextField getTxtJavadocJar()
	{
		if (txtJavadocJar == null)
		{
			txtJavadocJar = new JTextField();
			txtJavadocJar.setColumns(10);
		}
		return txtJavadocJar;
	}

	public JButton getBtnJavadocjarfile()
	{
		if (btnJavadocjarfile == null)
		{
			btnJavadocjarfile = new JButton("...");
			btnJavadocjarfile.addActionListener(new BtnFileSelectionActionListener(jarFileHolder, jarFileNameExtensionFilter));

		}
		return btnJavadocjarfile;
	}

	public JTextField getTxtDocsetName()
	{
		if (txtDocsetName == null)
		{
			txtDocsetName = new JTextField();
			txtDocsetName.setColumns(10);
		}
		return txtDocsetName;
	}

	public JTextField getTxtDisplayName()
	{
		if (txtDisplayName == null)
		{
			txtDisplayName = new JTextField();
			txtDisplayName.setColumns(10);
		}
		return txtDisplayName;
	}

	public JTextField getTxtPrefix()
	{
		if (txtPrefix == null)
		{
			txtPrefix = new JTextField();
			txtPrefix.setColumns(10);
		}
		return txtPrefix;
	}

	public JButton getBtnSetNames()
	{
		if (btnSetNames == null)
		{
			btnSetNames = new JButton("set names");
			btnSetNames.addActionListener(new BtnSetNamesActionListener());
		}
		return btnSetNames;
	}

	public JTextField getTxtNameFromJar()
	{
		if (txtNameFromJar == null)
		{
			txtNameFromJar = new JTextField("");
			txtNameFromJar.setBorder(new LineBorder(new Color(0, 0, 0)));
		}
		return txtNameFromJar;
	}

	public JCheckBox getChckbxVerbose()
	{
		if (chckbxVerbose == null)
		{
			chckbxVerbose = new JCheckBox("verbose");
		}
		return chckbxVerbose;
	}

	public JCheckBox getChckbxKeepUnpackedJar()
	{
		if (chckbxKeepUnpackedJar == null)
		{
			chckbxKeepUnpackedJar = new JCheckBox("keep unpacked jar");
		}
		return chckbxKeepUnpackedJar;
	}

	public JTextField getTxtOutDir()
	{
		if (txtOutDir == null)
		{
			txtOutDir = new JTextField();
			txtOutDir.setColumns(10);
		}
		return txtOutDir;
	}

	public JButton getBtnGo()
	{
		if (btnGo == null)
		{
			btnGo = new JButton("Run Now!");
			btnGo.addActionListener(new BtnGoActionListener());
		}
		return btnGo;
	}

	public JTextField getTxtIconFile()
	{
		if (txtIconFile == null)
		{
			txtIconFile = new JTextField();
			txtIconFile.setName("txtIconFile");
			txtIconFile.setColumns(10);
		}
		return txtIconFile;
	}

	public JButton getBtnIconfile()
	{
		if (btnIconfile == null)
		{
			btnIconfile = new JButton("...");
			btnIconfile.setMargin(new Insets(2, 7, 2, 7));
			btnIconfile.addActionListener(new BtnFileSelectionActionListener(iconFileHolder, pngFileNameExtensionFilter));
			btnIconfile.setName("btnIconfile");
		}
		return btnIconfile;
	}

	public JButton getBtnOutDir()
	{
		if (btnOutDir == null)
		{
			btnOutDir = new JButton("...");
			btnOutDir.addActionListener(new DirSelectActionListener(outFileHolder));
			btnOutDir.setName("btnOutDir");
		}
		return btnOutDir;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(() ->
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					Stopwatch stopwatch = Stopwatch.createStarted();
					JFrameJavadoc2docset window = new JFrameJavadoc2docset();
					stopwatch.stop();
					l.debug(stopwatch);
					window.frame.setVisible(true);
				}
				catch (Exception e)
				{
					l.error("$Runnable.run()", e); //$NON-NLS-1$
					e.printStackTrace();
				}
			});
	}

	public JTextArea getLoggingConsole()
	{
		if (loggingConsole == null)
		{
			loggingConsole = new JTextArea();
			loggingConsole.setLineWrap(false);
			loggingConsole.setWrapStyleWord(true);
			loggingConsole.setEditable(false);
			loggingConsole.setFont(new Font("Courier", Font.PLAIN, 10));
			JTextAreaAppender.addTextArea(this.loggingConsole);
		}
		return loggingConsole;
	}

	public JButton getBtnClearDocsetName()
	{
		if (btnClearDocsetName == null)
		{
			btnClearDocsetName = new JButton();
			btnClearDocsetName.setMargin(new Insets(0, 0, 0, 0));
			btnClearDocsetName.setIconTextGap(0);
			btnClearDocsetName.addActionListener(x -> getTxtDocsetName().setText(""));
			btnClearDocsetName.setIcon(new ImageIcon(JFrameJavadoc2docset.class.getResource(CLEAR_GIF)));
			btnClearDocsetName.setName("btnClearDocsetName");
		}
		return btnClearDocsetName;
	}

	public JButton getBtnClearDisplayName()
	{
		if (btnClearDisplayName == null)
		{
			btnClearDisplayName = new JButton();
			btnClearDisplayName.setMargin(new Insets(0, 0, 0, 0));
			btnClearDisplayName.setIconTextGap(0);
			btnClearDisplayName.addActionListener(x -> getTxtDisplayName().setText(""));
			btnClearDisplayName.setIcon(new ImageIcon(JFrameJavadoc2docset.class.getResource(CLEAR_GIF)));
			btnClearDisplayName.setName("btnClearDisplaytName");
		}
		return btnClearDisplayName;
	}

	public JButton getBtnClearTmpDir()
	{
		if (btnClearTmpDir == null)
		{
			btnClearTmpDir = new JButton();
			btnClearTmpDir.setMargin(new Insets(0, 0, 0, 0));
			btnClearTmpDir.setIconTextGap(0);
			btnClearTmpDir.addActionListener(x -> tmpFileHolder.setValue(null));
			btnClearTmpDir.setIcon(new ImageIcon(JFrameJavadoc2docset.class.getResource(CLEAR_GIF)));

			btnClearTmpDir.setName("btnClearTmpDir");
		}
		return btnClearTmpDir;
	}

	public JButton getBtnClearOutDir()
	{
		if (btnClearOutDir == null)
		{
			btnClearOutDir = new JButton();
			btnClearOutDir.setMargin(new Insets(0, 0, 0, 0));
			btnClearOutDir.setIconTextGap(0);
			btnClearOutDir.addActionListener(x -> outFileHolder.setValue(null));
			btnClearOutDir.setIcon(new ImageIcon(JFrameJavadoc2docset.class.getResource(CLEAR_GIF)));

			btnClearOutDir.setName("btnClearOutDir");
		}
		return btnClearOutDir;
	}

	public JButton getBtnClearJarFile()
	{
		if (btnClearJarFile == null)
		{
			btnClearJarFile = new JButton();
			btnClearJarFile.setMargin(new Insets(0, 0, 0, 0));
			btnClearJarFile.setIconTextGap(0);
			btnClearJarFile.addActionListener(x -> jarFileHolder.setValue(null));
			btnClearJarFile.setIcon(new ImageIcon(JFrameJavadoc2docset.class.getResource(CLEAR_GIF)));

			btnClearJarFile.setName("btnClearJarFile");
		}
		return btnClearJarFile;
	}

	public JButton getBtnClearPrefix()
	{
		if (btnClearPrefix == null)
		{
			btnClearPrefix = new JButton();
			btnClearPrefix.setMargin(new Insets(0, 0, 0, 0));
			btnClearPrefix.setIconTextGap(0);
			btnClearPrefix.addActionListener(x -> getTxtPrefix().setText(""));
			btnClearPrefix.setIcon(new ImageIcon(JFrameJavadoc2docset.class.getResource(CLEAR_GIF)));

			btnClearPrefix.setName("btnClearPrefix");
		}
		return btnClearPrefix;
	}

	public JButton getBtnUpdateName()
	{
		if (btnUpdateName == null)
		{
			btnUpdateName = new JButton();
			btnUpdateName.setToolTipText("regenerate name from Jar file name");
			btnUpdateName.setMargin(new Insets(0, 0, 0, 0));
			btnUpdateName.setIconTextGap(0);
			btnUpdateName.addActionListener(x -> nameFromJarName());
			btnUpdateName.setIcon(new ImageIcon(JFrameJavadoc2docset.class.getResource(LAMP_GIF)));
			btnUpdateName.setName("btnUpdateName");
		}
		return btnUpdateName;
	}

	public JScrollPane getScrollPaneTblTasks()
	{
		if (scrollPaneTblTasks == null)
		{
			scrollPaneTblTasks = new JScrollPane();
			scrollPaneTblTasks.setName("scrollPaneTblTasks");
			scrollPaneTblTasks.setViewportView(getTblTasks());
			scrollPaneTblTasks.setPreferredSize(getTblTasks().getPreferredSize());
		}
		return scrollPaneTblTasks;
	}

	public JTable getTblTasks()
	{
		if (tblTasks == null)
		{
			@SuppressWarnings("unchecked") DocsetTaskTableAdapter docsetTaskTableAdapter = new DocsetTaskTableAdapter(getSelectionInList());
			tblTasks = new JTable(docsetTaskTableAdapter);
			tblTasks.setFont(new Font("Arial Narrow", Font.PLAIN, 11));
			tblTasks.setName("tblTasks");
			tblTasks.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			new ButtonColumn(tblTasks, docsetTaskTableAdapter.deleteAction, 10);
			tblTasks.setSelectionModel(new SingleListSelectionAdapter(getSelectionInList().getSelectionIndexHolder()));
		}
		return tblTasks;
	}

	public JButton getBtnAdd()
	{
		if (btnAdd == null)
		{
			btnAdd = new JButton("Add");
			btnAdd.addActionListener(new BtnAddActionListener());
			btnAdd.setName("btnAdd");
		}
		return btnAdd;
	}

	public JButton getBtnRunAll()
	{
		if (btnRunAll == null)
		{
			btnRunAll = new JButton("Run...");
			btnRunAll.addActionListener(e ->
				{
					Boolean v = (Boolean) pauseHolder.getValue();
					pauseHolder.setValue(!v);
				});
			btnRunAll.setName("btnRunAll");
		}
		return btnRunAll;
	}

	public JScrollPane getScrollPaneStatusTestArea()
	{
		if (scrollPaneStatusTestArea == null)
		{
			scrollPaneStatusTestArea = new JScrollPane();
			scrollPaneStatusTestArea.setName("scrollPaneStatusTestArea");
			scrollPaneStatusTestArea.setViewportView(getLoggingConsole());
			scrollPaneStatusTestArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPaneStatusTestArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		return scrollPaneStatusTestArea;
	}

	public PausableThreadPoolExecutor getPausableExecutorService()
	{
		if (pausableExecutorService == null)
		{
			BlockingDeque<Runnable> bq = new LinkedBlockingDeque<>();
			pausableExecutorService = new PausableThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS, bq);
			pausableExecutorService.pause();
		}
		return pausableExecutorService;

	}

	static class PausableThreadPoolExecutor extends ThreadPoolExecutor
	{
		private boolean			isPaused;

		private ReentrantLock	pauseLock	= new ReentrantLock();
		private Condition		unpaused	= pauseLock.newCondition();

		public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
		{
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r)
		{
			super.beforeExecute(t, r);
			pauseLock.lock();
			try
			{
				while (isPaused)
					unpaused.await();
			}
			catch (InterruptedException ie)
			{
				l.error("beforeExecute(Thread, Runnable)", ie); //$NON-NLS-1$

				t.interrupt();
			}
			finally
			{
				pauseLock.unlock();
			}
		}

		public void pause()
		{
			pauseLock.lock();
			try
			{
				isPaused = true;
			}
			finally
			{
				pauseLock.unlock();
			}
		}

		public void resume()
		{
			pauseLock.lock();
			try
			{
				isPaused = false;
				unpaused.signalAll();
			}
			finally
			{
				pauseLock.unlock();
			}
		}
	}

	public ImageIcon getImageIcon()
	{
		if (imageIcon == null)
		{
			imageIcon = new ImageIcon(EmptyImage);
		}
		return imageIcon;
	}

	public JLabel getLblIcon()
	{
		if (lblIcon == null)
		{
			lblIcon = new JLabel(new ImageIcon(EmptyImage));
			lblIcon.setPreferredSize(new Dimension(70, 70));
			lblIcon.setName("lblIcon");

		}
		return lblIcon;
	}

	public JButton getBtnClearAll()
	{
		if (btnClearAll == null)
		{
			btnClearAll = new JButton("Clear All");
			btnClearAll.addActionListener(e ->
				{
					getPausableExecutorService().pause();
					getTasksListModel().stream().map(a ->
						{
							if (a.getFuture() != null)
							{
								a.getFuture().cancel(false);
							}
							return a;
						});
					getPausableExecutorService().purge();
				});
		}
		return btnClearAll;
	}

	public JButton getBtnClearDone()
	{
		if (btnClearDone == null)
		{
			btnClearDone = new JButton("Clear Done");
			btnClearDone.addActionListener(e ->
				{
					final List<DocsetTask> doneItems = getTasksListModel().stream().filter(DocsetTask::isDone).collect(Collectors.toList());
					getTasksListModel().removeAll(doneItems);
				});
			btnClearDone.setName("btnClearDone");
		}
		return btnClearDone;
	}

	public JButton getBtnClearLog()
	{
		if (btnClearLog == null)
		{
			btnClearLog = new JButton("");
			btnClearLog.setMargin(new Insets(0, 0, 0, 0));
			btnClearLog.addActionListener(e -> loggingConsole.setText(""));
			btnClearLog.setIcon(new ImageIcon(JFrameJavadoc2docset.class.getResource(CLEAR_GIF)));
			btnClearLog.setName("btnClearLog");
		}
		return btnClearLog;
	}

	public JPanel getPanel()
	{
		if (panel == null)
		{
			panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "Out Dir:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panel.setName("panel");
			panel.setLayout(new MigLayout("", "[grow][][]", "[]"));
			panel.add(getTxtOutDir(), "cell 0 0,growx,aligny center");
			panel.add(getBtnClearOutDir(), "cell 1 0,aligny center");
			panel.add(getBtnOutDir(), "cell 2 0,aligny center");
		}
		return panel;
	}

	public JPanel getPanelTmpDir()
	{
		if (panelTmpDir == null)
		{
			panelTmpDir = new JPanel();
			panelTmpDir.setBorder(new TitledBorder(null, "Temp Dir:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panelTmpDir.setName("panelTmpDir");
			panelTmpDir.setLayout(new MigLayout("", "[grow][][]", "[]"));
			panelTmpDir.add(getTxtTmpDir(), "cell 0 0,growx,aligny center");
			panelTmpDir.add(getBtnClearTmpDir(), "cell 1 0,aligny center");
			panelTmpDir.add(getBtnTmpDir(), "cell 2 0,aligny center");
		}
		return panelTmpDir;
	}

	public JPanel getPanelJarFile()
	{
		if (panelJarFile == null)
		{
			panelJarFile = new JPanel();
			panelJarFile.setBorder(new TitledBorder(null, "Javadoc Jar File:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panelJarFile.setName("panelJarFile");
			panelJarFile.setLayout(new MigLayout("", "[grow][][]", "[]"));
			panelJarFile.add(getTxtJavadocJar(), "cell 0 0,growx");
			panelJarFile.add(getBtnClearJarFile(), "cell 1 0");
			panelJarFile.add(getBtnJavadocjarfile(), "cell 2 0");
		}
		return panelJarFile;
	}

	public JPanel getPanelNameFromJarFile()
	{
		if (panelNameFromJarFile == null)
		{
			panelNameFromJarFile = new JPanel();
			panelNameFromJarFile.setBorder(new TitledBorder(null, "Name from the Jar File:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panelNameFromJarFile.setName("panelNameFromJarFile");
			panelNameFromJarFile.setLayout(new MigLayout("", "[grow][]", "[]"));
			panelNameFromJarFile.add(getTxtNameFromJar(), "cell 0 0,growx,aligny center");
			panelNameFromJarFile.add(getBtnUpdateName(), "cell 1 0");
		}
		return panelNameFromJarFile;
	}

	public JPanel getPanelIcon()
	{
		if (panelIcon == null)
		{
			panelIcon = new JPanel();
			panelIcon.setBorder(new TitledBorder(null, "Icon", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panelIcon.setName("panelIcon");
			panelIcon.setLayout(new MigLayout("", "[][grow][]", "[]"));
			panelIcon.add(getLblIcon(), "cell 0 0");
			panelIcon.add(getTxtIconFile(), "cell 1 0,growx,aligny top");
			panelIcon.add(getBtnIconfile(), "cell 2 0,aligny top");
		}
		return panelIcon;
	}

	public JPanel getPanelDocsetName()
	{
		if (panelDocsetName == null)
		{
			panelDocsetName = new JPanel();
			panelDocsetName.setBorder(new TitledBorder(null, "Docset Name:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panelDocsetName.setName("panelDocsetName");
			panelDocsetName.setLayout(new MigLayout("", "[grow][]", "[]"));
			panelDocsetName.add(getTxtDocsetName(), "cell 0 0,growx,aligny center");
			panelDocsetName.add(getBtnClearDocsetName(), "cell 1 0,aligny center");
		}
		return panelDocsetName;
	}

	public JPanel getPanelDisplayName()
	{
		if (panelDisplayName == null)
		{
			panelDisplayName = new JPanel();
			panelDisplayName.setBorder(new TitledBorder(null, "Display Name:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panelDisplayName.setName("panelDisplayName");
			panelDisplayName.setLayout(new MigLayout("", "[grow][]", "[]"));
			panelDisplayName.add(getTxtDisplayName(), "cell 0 0,growx,aligny center");
			panelDisplayName.add(getBtnClearDisplayName(), "cell 1 0,alignx left,aligny center");
		}
		return panelDisplayName;
	}

	public JPanel getPanelPrefix()
	{
		if (panelPrefix == null)
		{
			panelPrefix = new JPanel();
			panelPrefix.setBorder(new TitledBorder(null, "Prefix:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panelPrefix.setName("panelPrefix");
			panelPrefix.setLayout(new MigLayout("", "[grow][][]", "[]"));
			panelPrefix.add(getTxtPrefix(), "cell 0 0,growx,aligny center");
			panelPrefix.add(getBtnClearPrefix(), "cell 1 0,aligny center");
			panelPrefix.add(getBtnSetNames(), "cell 2 0,alignx left,aligny center");
		}
		return panelPrefix;
	}

	public JPanel getPanelOptions()
	{
		if (panelOptions == null)
		{
			panelOptions = new JPanel();
			panelOptions.setName("panelOptions");
			panelOptions.setLayout(new MigLayout("", "[][]", "[]"));
			panelOptions.add(getChckbxVerbose(), "cell 0 0,alignx left,aligny center");
			panelOptions.add(getChckbxKeepUnpackedJar(), "cell 1 0,alignx left,aligny center");
		}
		return panelOptions;
	}

	public JPanel getPanelDocsetTasksTable()
	{
		if (panelDocsetTasksTable == null)
		{
			panelDocsetTasksTable = new JPanel();
			panelDocsetTasksTable.setBorder(new TitledBorder(null, "Task List:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panelDocsetTasksTable.setName("panelDocsetTasksTable");
			panelDocsetTasksTable.setLayout(new MigLayout("", "[][grow][][]", "[grow][]"));
			panelDocsetTasksTable.add(getScrollPaneTblTasks(), "cell 0 0 4 1,grow");
			panelDocsetTasksTable.add(getBtnRunAll(), "cell 0 1,alignx center,aligny top");
			panelDocsetTasksTable.add(getBtnClearDone(), "flowx,cell 2 1");
			panelDocsetTasksTable.add(getBtnClearAll(), "cell 3 1");
		}
		return panelDocsetTasksTable;
	}

	public JPanel getPanelLogConsole()
	{
		if (panelLogConsole == null)
		{
			panelLogConsole = new JPanel();
			panelLogConsole.setBorder(new TitledBorder(null, "Log Console:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panelLogConsole.setName("panelLogConsole");
			panelLogConsole.setLayout(new MigLayout("", "[grow][]", "[][grow]"));
			panelLogConsole.add(getScrollPaneStatusTestArea(), "cell 0 0 1 2,grow");
			panelLogConsole.add(getBtnClearLog(), "cell 1 0,alignx left,aligny top");
		}
		return panelLogConsole;
	}

	public JPanel getPanelDocsetTask()
	{
		if (panelDocsetTask == null)
		{
			panelDocsetTask = new JPanel();
			panelDocsetTask.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Create Docset Task:", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
			panelDocsetTask.setName("panelDocsetTask");
			panelDocsetTask.setLayout(new MigLayout("", "[][]", "[][][][][][][]"));
			panelDocsetTask.add(getPanelTmpDir(), "cell 0 0,growx,aligny center");
			panelDocsetTask.add(getPanel(), "cell 1 0,growx,aligny center");
			panelDocsetTask.add(getPanelNameFromJarFile(), "cell 0 1,growx,aligny center");
			panelDocsetTask.add(getPanelJarFile(), "cell 1 1,growx,aligny center");
			panelDocsetTask.add(getPanelPrefix(), "cell 0 2,growx,aligny center");
			panelDocsetTask.add(getPanelDocsetName(), "cell 0 3,growx,aligny center");
			panelDocsetTask.add(getPanelDisplayName(), "cell 1 3,growx,aligny center");
			panelDocsetTask.add(getPanelIcon(), "cell 0 4 2 1,growx,aligny center");
			panelDocsetTask.add(getPanelOptions(), "cell 0 5,alignx left,aligny center");
			panelDocsetTask.add(getBtnAdd(), "cell 0 6");
			panelDocsetTask.add(getBtnGo(), "cell 1 6,alignx right");
		}
		return panelDocsetTask;
	}

	public JButton getBtnExit()
	{
		if (btnExit == null)
		{
			btnExit = new JButton("Exit");
			btnExit.addActionListener(e -> frame.dispose());
			btnExit.setName("btnExit");
		}
		return btnExit;
	}
}
