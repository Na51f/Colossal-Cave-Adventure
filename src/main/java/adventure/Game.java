package adventure;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Main class of this game.
 * @author Nasif Mauthoor
 * ID: 1083611
 */
public final class Game {
    private final Parser parser = new Parser();
    private final Scanner scanner = new Scanner(System.in);

    private Adventure adventure;
    private String playerName;

    /**
     * Main method.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        Game theGame = new Game();

        theGame.setAdventure(args); // Creates adventure
        if (theGame.adventure == null) {
            System.out.println("Invalid JSON file.");
            return;
        }

        boolean isQuit = false; // Checks if the player wants to quit
        while (!isQuit) {
            System.out.println("\nCurrent room: " + theGame.adventure.getCurrentRoom().getName()
                    + theGame.adventure.getCurrentRoom().displayItems());
            try {
                Command command = theGame.getInputCommand(theGame.getLine());
                isQuit = !theGame.followCommand(command);
            } catch (InvalidCommandException e) {
                System.out.println("Invalid command.");
            }
        }

        theGame.promptSave();
    }

    /**
     * Loading json from default file in jar.
     * @param inputStream File input stream.
     * @return JSONObject of entire adventure.
     */
    public JSONObject loadAdventureJson(InputStream inputStream) {
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            System.out.println("File does not exist.");
            return null;
        }
    }

    /**
     * Loading json file from chosen file.
     * @param input File name.
     * @return JSONObject of entire adventure.
     */
    public JSONObject loadAdventureJson(String input) {
        try (BufferedReader reader = new BufferedReader(new FileReader(input))) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Generates an adventure from the adventure tag in the game's JSON.
     * @param obj JSONObject of the adventure.
     * @return The parsed adventure.
     */
    public Adventure generateAdventure(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        playerName = promptUsername();
        return new Adventure(playerName, (JSONObject) obj.get("adventure"));
    }

    /**
     * Gets adventure from command line arguments.
     * Will take a flag and a filename or neither.
     * If no flag is entered, the default file will load.
     * Flags:
     *      -a: Loads adventure from a save file.
     *      -l: Loads adventure from a json file.
     *      No flag: Loads adventure from default.json in the resources directory.
     * @param args Arguments from main.
     */
    public void setAdventure(String[] args) {
        if (args.length < 2) {
            InputStream stream = Game.class.getResourceAsStream("/default.json");
            adventure = generateAdventure(loadAdventureJson(stream));
        } else {
            switch (args[0]) {
                case "-l":
                    adventure = deserializeAdventure(args[1]);
                    break;
                case "-a":
                    adventure = createNewAdventure(args[1]);
                    break;
                default:
            }
        }
    }

    // Creates a new adventure from given json file.
    private Adventure createNewAdventure(String fileName) {
        Adventure newAdventure;
        newAdventure = generateAdventure(loadAdventureJson(fileName));
        if (newAdventure == null) {
            return null;
        }
        newAdventure.setPlayerName(playerName);
        return newAdventure;
    }

    // Follows the command from user input
    // Returns false if player wants to quit
    private boolean followCommand(Command command) {
        switch (command.getActionWord()) {
            case "go":
                adventure.movePlayer(command.getNoun());
                break;
            case "look":
                System.out.println(adventure.lookAt(command.getNoun()));
                break;
            case "take":
                System.out.println(adventure.takeItem(command.getNoun()));
                break;
            case "inventory":
                System.out.println(adventure.checkInventory());
                break;
            case "quit":
                return false;
            default:
        }
        return true;
    }

    private Command getInputCommand(String input) throws InvalidCommandException {
        try {
            return parser.parseUserCommand(input);
        } catch (InvalidCommandException e) {
            throw new InvalidCommandException();
        }
    }

    private String promptUsername() {
        System.out.println("What is your name?");
        String name = getLine();
        System.out.printf("Your name is %s.\n", name);
        return name;
    }

    // Gets line from user input.
    private String getLine() {
        return scanner.nextLine();
    }

    private void promptSave() {
        System.out.println("Would you like to save? (y/n)");

        while (true) {
            String input = getLine();
            switch (input) {
                case "n":
                    return;
                case "y":
                    if (!adventure.getSaveName().isEmpty()) {
                        serializeAdventure(adventure.getSaveName());
                        return;
                    }

                    System.out.println("What would like to name your save file?");
                    serializeAdventure(getLine());
                    return;
                default:
                    System.out.println("Invalid input. (y/n)");
            }
        }
    }

    // Creates a save file for the adventure
    private void serializeAdventure(String saveName) {
        try {
            FileOutputStream outputStream = new FileOutputStream(saveName);
            ObjectOutputStream outputDest = new ObjectOutputStream(outputStream);
            outputDest.writeObject(adventure);
            outputDest.close();
            outputStream.close();

            System.out.println("File was saved.");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private Adventure deserializeAdventure(String saveName) {
        Adventure adventureObj;
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(saveName))) {
            adventureObj = (Adventure) input.readObject();

            System.out.println("Welcome back " + adventureObj.getPlayerName() + ".");
        } catch (Exception e) {
            adventureObj = null;
        }

        return adventureObj;
    }

    // Keeps asking user for inputs until they quit
    private void loopInput() {
    }

    @Override
    public String toString() {
        return adventure.toString();
    }
}
