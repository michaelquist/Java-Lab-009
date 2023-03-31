/**
 * @author Trevor Hartman
 * @author Mike Quist
 * @date 03/29/2023
 */

import org.apache.commons.codec.digest.Crypt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Stream;

public class Crack {
    private final User[] users;
    private final String dictionary;

    public Crack(String shadowFile, String dictionary) throws FileNotFoundException {
        this.dictionary = dictionary;
        this.users = Crack.parseShadow(shadowFile);
    }

    public void crack() throws FileNotFoundException {
        try (InputStream in = new FileInputStream(dictionary); Scanner scanner = new Scanner(in)) {
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine();
                for (User user : users) {
                    String passHash = user.getPassHash();
                    if (passHash.contains("$")) {
                        String hash = Crypt.crypt(word, passHash);
                        if (hash.equals(passHash)) {
                            System.out.println("Found password " + word + " for user " + user.getUsername() + ".");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getLineCount(String path) {
        int lineCount = 0;
        try (Stream<String> stream = Files.lines(Path.of(path), StandardCharsets.UTF_8)) {
            lineCount = (int)stream.count();
        } catch(IOException ignored) {}
        return lineCount;
    }

    public static User[] parseShadow(String shadowFile) throws FileNotFoundException {
        int numLines = getLineCount(shadowFile);
        User[] users = new User[numLines];
        try (InputStream in = new FileInputStream(shadowFile); Scanner scanner = new Scanner(in)) {
            int i = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split(":");
                String username = tokens[0];
                String passHash = tokens[1];
                User user = new User(username, passHash);
                users[i++] = user;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Type the path to your shadow file: ");
        String shadowPath = sc.nextLine();
        System.out.print("Type the path to your dictionary file: ");
        String dictPath = sc.nextLine();

        Crack c = new Crack(shadowPath, dictPath);
        c.crack();
    }
}
