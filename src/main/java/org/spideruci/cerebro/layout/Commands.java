package org.spideruci.cerebro.layout;

import com.google.common.base.Preconditions;

/**
 * @author vpalepu
 * @since 6/1/16
 */
public enum Commands {
  SNAP,
  HIDE_EDGES,
  SHOW_EDGES,
  EXPAND_NODES,
  SHRINK_NODES,
  CLUSTER,
  COLOR_BY_METHOD,
  COLOR_BY_CLASS,
  REMOVE_COLOR,
  DONE,
  HELP;

  public static final String mainMenu = genMainMenu();

  private static String genMainMenu() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Available Commands:");
    for (Commands command : Commands.values()) {
      buffer.append("\n ").append(getText(command));
    }

    return buffer.toString();
  }

  public static String getText(Commands command) {
    String commandName = command.name();
    return commandName.trim().toLowerCase().replace('_', '-');
  }

  public static Commands getCommand(String commandText) {
    Preconditions.checkNotNull(commandText);
    Preconditions.checkArgument(!commandText.isEmpty(), "commandText is empty");
    String name = commandText.trim().toUpperCase().replace('-', '_');
    Commands cmd = Commands.valueOf(name);
    return cmd;
  }

  public static boolean handleInput(DynamicDisplay display,
                                     String option,
                                     String... params) {
    Commands cmd = Commands.getCommand(option);

    switch (cmd) {
      case SNAP:
        System.out.println("The [snap] command is currently not working."
            + "\n Open this URL in a browser to open an issue on project's issue "
            + "\n tracker to lobby for this feature -- "
            + "\nhttps://github.com/spideruci/cerebro-layout/issues/new?body=save%20layout%20as%20a%20png%20image%20using%20a%20'snap'%20command&title=snap%20command&labels=enhancement");
        break;
      case HIDE_EDGES:
        display.hideEdges();
        break;
      case SHOW_EDGES:
        display.showEdges();
        break;
      case EXPAND_NODES:
        display.expandNodes();
        break;
      case SHRINK_NODES:
        display.shrinkNodes();
        break;
      case CLUSTER:
        display.colorNodes();
        break;
      case REMOVE_COLOR:
        display.decolorNodes();
        break;
      case COLOR_BY_METHOD:
        display.colorNodesByMethod();
        break;
      case COLOR_BY_CLASS:
        display.colorNodesByClass();
        break;
      case HELP:
        System.out.println(Commands.mainMenu);
        break;
      case DONE:
        return false;
    }

    return true;
  }

}
