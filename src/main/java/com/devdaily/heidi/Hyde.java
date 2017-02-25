package com.devdaily.heidi;

import java.awt.Color;

import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import ch.randelshofer.quaqua.QuaquaManager;
import com.apple.eawt.Application;

public class Hyde
{
  private CurtainFrame desktopCurtainFrame;
  private static final String APP_NAME = "Hyde";
  private JPanel curtainPanel = new JPanel();
  
  private static final String DC_LICENSING_URL = "http://devdaily.com/hide-your-desktop";
  
  private static final String ABOUT_DIALOG_MESSAGE = "<html><center><p>Hyde, Version 1.1.0</p></center>\n\n"
    + "<center><p><a href=\"http://devdaily.com/hide-your-desktop\">http://devdaily.com/hide-your-desktop</a></p><center>\n";

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
  private static final String homeLibraryApplicationSupportDirname = "Library/Application Support/DevDaily/Hyde";


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
//    int r = preferences.getInt(CURTAIN_R, 55);
//    int g = preferences.getInt(CURTAIN_G, 55);
//    int b = preferences.getInt(CURTAIN_B, 55);
//    int a = preferences.getInt(CURTAIN_A, 255);
//    currentColor = new Color(r,g,b,a);
    // override preferences

    // THIS IS NO LONGER USED. SEE HYDE2.SCALA.
    currentColor = new Color(128,128,128, 255);
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









