package oopnet.chess.client;

import javax.swing.*;

public class GameSelectorDialog {

    private String serverInput;
    private String usernameInput;
    private String gameIdentifierInput;


    public GameSelectorDialog(ClientProperties properties, UserInputCallback callback) {
        JFrame frame = new JFrame();
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        Object[] options = {"Join server", "Cancel"};

        JTextField serverField = new JTextField(5);
        serverField.setText(properties.get(ClientProperties.SERVER_ADDRESS_KEY).toString());
        JTextField usernameField = new JTextField(5);
        usernameField.setText(properties.get(ClientProperties.NICKNAME_KEY).toString());
        JTextField gameIdentifierField = new JTextField(5);
        pane.add(new JLabel("Username"));
        pane.add(usernameField);
        pane.add(new JLabel("Game ID"));
        pane.add(gameIdentifierField);
        pane.add(new JLabel("Server"));
        pane.add(serverField);

        int option = JOptionPane.showOptionDialog(frame, pane, "Enter a username, game id and server to connect to", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (option == JOptionPane.YES_OPTION) {
            this.serverInput = serverField.getText();
            this.gameIdentifierInput = gameIdentifierField.getText();
            this.usernameInput = usernameField.getText();

            frame.dispose();
            if (!callback.onSubmit(serverInput, gameIdentifierInput, usernameInput)) {
                new GameSelectorDialog(properties, callback);
            }
        }
    }


    public interface UserInputCallback {

        boolean onSubmit(String server, String gameIdentifier, String username);

    }
}

