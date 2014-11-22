package com.devdaily.heidi;

import java.awt.Color;

import java.awt.FileDialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import ch.randelshofer.quaqua.QuaquaManager;
import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;

public class Hyde
{
  private CurtainFrame desktopCurtainFrame;
  private static final String APP_NAME = "Hyde";
  private JPanel curtainPanel = new JPanel();
  
  private static final String DC_LICENSING_URL = "http://devdaily.com/hide-your-desktop";
  
  private static final String ABOUT_DIALOG_MESSAGE = "<html><center><p>Hyde, Version 1.1.0</p></center>\n\n"
    + "<center><p><a href=\"http://devdaily.com/hide-your-desktop\">http://devdaily.com/hide-your-desktop</a></p><center>\n";

  // "please license" dialog stuff
  private static final String LICENSE_DIALOG_TITLE = "Hyde";
  
  // so we can share our events with other controllers
  public static final int SHOW_CURTAIN_EVENT   = 1;
  public static final int QUIT_CURTAIN_EVENT   = 2;
  public static final int REFILL_CURTAIN_EVENT = 3;
  
  // preferences stuff
  private Preferences preferences;
  private static String CURTAIN_R = "CURTAIN_R";
  private static String CURTAIN_G = "CURTAIN_G";
  private static String CURTAIN_B = "CURTAIN_B";
  private static String CURTAIN_A = "CURTAIN_A";
  private Color currentColor;
  
  // prefs - how many times the app has been started.
  // (changed this to a one-letter name b/c it shows up as text in the plist file)
  private static String NUM_USES_PREF = "C";
  
  // TODO move this to a properties file or class
  private static final String FILE_PATH_SEPARATOR = System.getProperty("file.separator");
  private static final String USER_HOME_DIR = System.getProperty("user.home");

  private static final String RELATIVE_SOUNDS_DIR_NAME = "Sounds";
  private static final String homeLibraryApplicationSupportDirname = "Library/Application Support/DevDaily/Hyde";
  public  static final String CANON_SOUNDS_DIR = USER_HOME_DIR + FILE_PATH_SEPARATOR + homeLibraryApplicationSupportDirname 
                                               + FILE_PATH_SEPARATOR + RELATIVE_SOUNDS_DIR_NAME;

  // ------------------------------ LOGGING ---------------------------------//
  // TODO move this to a properties file or class (though USER_HOME_DIR is dynamic)
  //      these also vary by windows/mac
  private static final String REL_LOGFILE_DIRECTORY = "Library/Logs/DevDaily/Hyde";
  private static final String CANON_LOGFILE_DIR     = USER_HOME_DIR + FILE_PATH_SEPARATOR + REL_LOGFILE_DIRECTORY; 
  private static final String CANON_DEBUG_FILENAME  = CANON_LOGFILE_DIR + FILE_PATH_SEPARATOR + "Hyde.log";
  
  // user can "touch" these files to force logging at these levels (only checks at startup)
  private static final String DEBUG_LOG_FILENAME   = CANON_LOGFILE_DIR + FILE_PATH_SEPARATOR + "DEBUG";
  private static final String WARNING_LOG_FILENAME = CANON_LOGFILE_DIR + FILE_PATH_SEPARATOR + "WARNING";
  private static final String ERROR_LOG_FILENAME   = CANON_LOGFILE_DIR + FILE_PATH_SEPARATOR + "ERROR";


  public static void main(String[] args)
  {
    new Hyde();
  }

  public Hyde()
  {
    dieIfNotRunningOnMacOsX();
    
    configureForMacOSX();
    
    // get default color from preferences
    connectToPreferences();
    getDefaultColor();
    
    desktopCurtainFrame = new CurtainFrame(this, curtainPanel, currentColor, null);
    DragWindowListener mml = new DragWindowListener(curtainPanel);
    curtainPanel.addMouseListener(mml);
    curtainPanel.addMouseMotionListener(mml);

    // display the jframe
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        try
        {
          // did this to get the quaqua jcolorchooser, which looks much more
          // mac-like; if it creates a problem, switch back
          UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
        }
        catch (Exception e)
        {
          // TODO log this? where does system err output on mac os x go?
        }
        
        desktopCurtainFrame.display();
      }
    });
    
    // verify the license.
    // this makes a callback to handleVerifyLicenseFailedEvent() if the verification fails.
    // commenting this out on August 5, 2010. App is now free.
    //licenseController.verifyLicense();
    this.giveFocusBackToCurtain();
  }
  
  /**
   * If the app is not running on mac os x, die right away.
   */
  private void dieIfNotRunningOnMacOsX()
  {
//    boolean mrjVersionExists = System.getProperty("mrj.version") != null;
//    boolean osNameExists = System.getProperty("os.name").startsWith("Mac OS");
//    
//    if ( !mrjVersionExists || !osNameExists)
//    {
//      System.err.println("Not running on a Mac OS X system.");
//      System.exit(1);
//    }
  }
  

  /**
   * Do everything we need to configure the app for Mac OS X systems.
   */
  private void configureForMacOSX()
  {
    // set some mac-specific properties; helps when i don't use ant to build the code
    System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);

    // create an instance of the Mac Application class, so i can handle the 
    // mac quit event with the Mac ApplicationAdapter
    Application macApplication = Application.getApplication();
    MyApplicationAdapter macAdapter = new MyApplicationAdapter(this);
    macApplication.addApplicationListener(macAdapter);
    
    // must enable the preferences option manually
    macApplication.setEnabledPreferencesMenu(true);
    
    // use these quaqua components
    Set includes = new HashSet();
    includes.add("ColorChooser");
    //includes.add("Table");
    QuaquaManager.setIncludedUIs(includes);
    
    // this did not work to get tables striped
    //UIManager.put("Table.alternateRowColor", UIManager.getColor("Table.focusCellForeground"));

  }
  
  private int getAndUpdateUsageCounter()
  {
    // get the number of times the app has been accessed already
    int numUses = preferences.getInt(NUM_USES_PREF, 0);
    
    // update the number of uses by 1
    preferences.putInt(NUM_USES_PREF, numUses+1);
    
    return numUses;
  }

  private void connectToPreferences()
  {
    preferences = Preferences.userNodeForPackage(this.getClass());
  }
  
  /**
   * Get the default color from the Preferences, or default to black.
   */
  private void getDefaultColor()
  {
    int r = preferences.getInt(CURTAIN_R, 0);
    int g = preferences.getInt(CURTAIN_G, 0);
    int b = preferences.getInt(CURTAIN_B, 0);
    int a = preferences.getInt(CURTAIN_A, 255);
    currentColor = new Color(r,g,b,a);
  }
  
  public void updateCurrentColor(Color newColor)
  {
    currentColor = newColor;

    // update the preferences
    preferences.putInt(CURTAIN_R, currentColor.getRed());
    preferences.putInt(CURTAIN_G, currentColor.getGreen());
    preferences.putInt(CURTAIN_B, currentColor.getBlue());
    preferences.putInt(CURTAIN_A, currentColor.getAlpha());
  }
  
  public Color getCurrentColor()
  {
    return currentColor;
  }

  public Class getClassToLicense()
  {
    return this.getClass();
  }

  public String getApplicationName()
  {
    return APP_NAME;
  }

  public void doQuitAction()
  {
    desktopCurtainFrame.doQuitAnimationAndQuit();
  }
  
  public CurtainFrame getDesktopCurtainFrame()
  {
    return this.desktopCurtainFrame;
  }
  
  
  //---------------------------------------------------------------------------//
  //                              SOUND STUFF                                  //
  //---------------------------------------------------------------------------//
  
  public static boolean makeDirectory(String directoryName)
  {
    try
    {
      boolean result = (new File(directoryName)).mkdir();
      return result;
    }
    catch (RuntimeException re)
    {
      // ignore exception
      return false;
    }
  }

  // TODO this was another late addition, primarily to support creation of the
  //      new Sounds folder location
  public static boolean makeDirectories(String directoryName)
  {
    try
    {
      boolean result = (new File(directoryName)).mkdirs();
      return result;
    }
    catch (RuntimeException re)
    {
      // ignore exception
      return false;
    }
  }


  /**
   * TODO If I port this to Windows, I need to use the different file chooser over there.
   * TODO Also, see if Quaqua has a file chooser to use instead.
   * 
   * @param frame - parent frame
   * @param dialogTitle - dialog title
   * @param defaultDirectory - default directory
   * @param fileType - something like "*.lic"
   * @return Returns null if the user selected nothing, otherwise returns the canonical filename (directory + fileSep + filename).
   */
  String promptForFilenameWithFileDialog (Frame frame, String dialogTitle, String defaultDirectory, String fileType) 
  {
    FileDialog fd = new FileDialog(frame, dialogTitle, FileDialog.LOAD);
    fd.setFile(fileType);
    fd.setDirectory(defaultDirectory);
    fd.setLocationRelativeTo(frame);
    fd.setVisible(true);
    String directory = fd.getDirectory();
    String filename = fd.getFile();
    if (directory == null || filename == null || directory.trim().equals("") || filename.trim().equals(""))
    {
      return null;
    }
    else
    {
      // this was not needed on mac os x:
      //return directory + System.getProperty("file.separator") + filename;
      return directory + filename;
    }
  }
  
  private void openUrlInBrowser(String url)
  {
    Runtime runtime = Runtime.getRuntime();
    String[] args = { "osascript", "-e", "open location \"" + url + "\"" };
    try
    {
      Process process = runtime.exec(args);
    }
    catch (IOException e)
    {
      // ignore this
    }
  }
  

  /**
   * A simple method others can use to return focus back to the curtain
   * after they have done something like displaying a dialog.
   */
  public void giveFocusBackToCurtain()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        desktopCurtainFrame.transferFocus();
      }
    });
  }

  public void handleAboutAction()
  {
    // create an html editor/renderer
    JEditorPane editor = new JEditorPane();
    editor.setContentType("text/html");
    editor.setEditable(false);
    editor.setSize(new Dimension(400,300));
    editor.setFont(UIManager.getFont("EditorPane.font"));
    // note: had to include this line to get it to use my font
    editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editor.setMargin(new Insets(5,15,25,15));
    editor.setText(ABOUT_DIALOG_MESSAGE);
    editor.setCaretPosition(0);
    JScrollPane scrollPane = new JScrollPane(editor);

    // add the hyperlink listener so the user can click my link
    // and go right to the website
    editor.addHyperlinkListener(new HyperlinkListener() {
    public void hyperlinkUpdate(HyperlinkEvent hev) 
    {
      if (hev.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
      {
        Runtime runtime = Runtime.getRuntime();
        String[] args = { "osascript", "-e", "open location \"" + DC_LICENSING_URL + "\"" };
        try
        {
          Process process = runtime.exec(args);
        }
        catch (IOException e)
        {
          // ignore this
        }
      }
    }});

    // display our message
    JOptionPane.showMessageDialog(desktopCurtainFrame, scrollPane,
        "About Hyde", JOptionPane.INFORMATION_MESSAGE);
  }

}









