package base;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Application class.
 *
 * [] For GUI applications, supports running applications as applets,
 * with option for displaying program inapplet's location on the web page, or
 * using that space to display a button that when pressed causes the program to
 * appear in a frame.
 *
 * To get console features, use the EmApplication subclass instead.
 */
public abstract class Application
    extends JApplet {

  private static boolean dba = false;

  /**
   * Start a console application, or an applet simulation of one
   * @param args String[]
   */
  protected void doMain(String[] args) {
    // We must call this in case we are not running as an applet
    Streams.loadResources(this);
  }

  /**
   * Start a GUI application.  Should be called by doMain() if it's
   * supposed to be a GUI.
   */
  protected void doMainGUI(String[] args) {
    Streams.loadResources(this);

    // Schedule a job for the event-dispatching thread:
    // calling an application object's run() method.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        doInit();
      }
    });
  }

  /**
   * Determine title of application or applet.
   * This is displayed as the title of the frame, or as the label of
   * the appletTitleLabel, if one is defined.
   *
   * @return String
   */
  protected String title() {
    String s = launchLabel;
    if (s == null) {
      s = this.getClass().getName();
      s = s.substring(s.lastIndexOf('.') + 1);
    }

    return s;
  }

  /**
   * Determine extended title of application/applet.  This is the base title
   * with optional extra information.  If none has been defined, uses default
   * title().
   *
   * @return String
   */
  protected String extendedTitle() {
  String s = extTitle;
  if (s == null)
    s = title();
  return s;
}

/**
 * Set extended title of application/applet.
 * @param t String
 */
protected void setExtendedTitle(String t) {
    extTitle = t;
  }
  // extended title
  private String extTitle;

  /**
   * Override this method to change preferred size of application frame.
   * @return Dimension
   */
  public Dimension getPreferredSize() {
    return new Dimension(1024, 768);
  }

  protected void setFrameOptions(JFrame f) {
    f.addWindowListener(new WindowAdapter() {
      /**
       * Invoked when the user attempts to close the window
       * from the window's system menu.
       */
      public void windowClosing(WindowEvent e) {
        exitProgram();
      }
    });
    f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  }

  public static boolean isApplet() {
    return theApplet != null;
  }

  // ---------------------------------------------------

  /****************************************************
   *  Applet code
   ****************************************************/

  private static boolean usesLauncher() {
    return inFrameFlag && launchLabel != null;
  }

  /**
   * Initialize the applet.
   * This sets up the applet launch button, if necessary.
   * Don't use init() for an applet; instead, use doInit().
   */
  public void init() {
    if (dba) {
      Streams.out.println("Application.init()");
    }

    Streams.appletFlag = true;
    inFrameFlag = false;
    theApplet = this;
    processAppletParameters();

    if (usesLauncher()) {
      JPanel launchPanel = new JPanel(new BorderLayout());
      getContentPane().add(launchPanel);

//      if (launcher == null)
      {
        launcher = new JButton(launchLabel);

        launcher.addActionListener(new ActionListener() {
          // this is called from the event dispatch thread, so no need to
          // call invokeAndWait().
          public void actionPerformed(ActionEvent actionEvent) {
            launcher.setEnabled(false);
            doInit();
          }
        });
      }

      launchPanel.add(launcher, BorderLayout.CENTER);
    }
    else {
      doInit();
    }
  }

  public void destroy() {
    if (dba) {
      Streams.out.println("Application.destroy()");
    }
    if (usesLauncher()) {
      getContentPane().removeAll();
    }
  }

  public void start() {
    if (dba) {
      Streams.out.println("Application.start()");
    }
  }

  public void stop() {
    if (dba) {
      Streams.out.println("Application.stop()");
    }
  }

  /**
   * Get an applet parameter.
   * @param name : name of parameter (case insensitive)
   * @param defaultValue : value to return if no parameter found
   * @return boolean : default value, or true if value (an integer) > 0
   */
  public static boolean getAppletParameter(String name, boolean defaultValue) {
    boolean out = defaultValue;
    String n = getAppletParameter(name, null);
    if (n != null) {
      try {
        out = Integer.parseInt(n) > 0;
      }
      catch (NumberFormatException e) {}
    }
    return out;
  }

  /**
   * Get an applet parameter.
   * @param name : name of parameter (case insensitive)
   * @param defaultValue : value to return if no parameter found
   * @return String : default value, or value read from tag
   */
  public static String getAppletParameter(String name, String defaultValue) {
//    Streams2.ensureApplet();

    String out = defaultValue;
    String val = theApplet.getParameter(name);
    if (val != null) {
      out = val;
    }
    return out;
  }

  /**
   * Process applet parameter tags
   */
  protected void processAppletParameters() {
    launchLabel = getAppletParameter("launch", null);
    autoFrame = getAppletParameter("frame", false);
    inFrameFlag = autoFrame || launchLabel != null;
  }

  protected void exitProgram() {
    final boolean db = false;
    if (db) {
      System.out.println("App.exitProg");
    }
    if (inFrameFlag) {
      if (db) {
        System.out.println("make invis");
      }
      appFrame.setVisible(false);
      appFrame.dispose();
      if (db) {
        System.out.println("dispose");
      }

      if (launcher != null) {
        if (Streams.isApplet()) {
          launcher.setEnabled(true);
        }
      }
      if (db) {
        System.out.println("done");
      }
    }
    if (db) {
      Streams.out.println(" setting exited = true");
    }
  }

  protected void updateTitle() {
//    Streams.out.println(Tools.tr() + ": " + "updateTitle, appFrame="+appFrame+"\ntitle="+title());

    if (appFrame != null) {
      appFrame.setTitle(extendedTitle());
    }
  }

  /**
   */
  protected void doInit() {
    Streams.loadResources(this);

    if (inFrameFlag) {
      if (!Streams.isApplet()) {
        // Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
      }
      // Create and set up the window.
      appFrame = new ApplicationJFrame(this);
      setFrameOptions(appFrame);
    }
    updateTitle();
  }

  protected void showApp() {
    if (inFrameFlag) {
      appFrame.pack();
      appFrame.setLocationRelativeTo(null);
      appFrame.setVisible(true);
    }
  }

  /**
   * Get content pane of application (or applet)
   * @return JComponent
   */
  protected static JComponent getAppContentPane() {
    return inFrameFlag ? ( (JComponent) appFrame.getContentPane())
        : (JComponent) theApplet.getContentPane();
  }

  /**
   * Get outermost application or applet container.
   * For an applet, this is the JApplet; otherwise, it's the
   * JFrame.
   *
   * @return Component
   */
  public static Component getAppContainer() {
    Component c = theApplet;
    if (c == null) {
      c = appFrame;
    }
    return c;
  }

  /**
   * Customized JFrame for application frame
   */
  private static class ApplicationJFrame
      extends JFrame {
    public ApplicationJFrame(Application app) {
      this.app = app;
    }

    private Application app;
    public Dimension getPreferredSize() {
      return app.getPreferredSize();
    }
  }

  /**
   * Get the JFrame containing the application or applet
   * @return JFrame, or null if applet that is not 'launched'
   */
  protected static JFrame appFrame() {
    return appFrame;
  }

  // string read from "LAUNCH" applet parameter
  private static String launchLabel;

  // true if "FRAME" parameter was nonzero
  private static boolean autoFrame;

  // true if application, or applet running in frame
  protected static boolean inFrameFlag = true;

  // JFrame of application or applet (null if not open yet)
  protected static JFrame appFrame;

  // if not null, label to associate with launch button
  private static JButton launcher;

  // the applet associated with the launcher
  private static JApplet theApplet;
}
