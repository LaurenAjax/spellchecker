package edu.grinnell.csc207.spellchecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A spellchecker maintains an efficient representation of a dictionary for
 * the purposes of checking spelling and provided suggested corrections.
 */
public class SpellChecker {
    /** The number of letters in the alphabet. */
    private static final int NUM_LETTERS = 26;

    /** The path to the dictionary file. */
    private static final String DICT_PATH = "words_alpha.txt";

    /**
     * @param filename the path to the dictionary file
     * @return a SpellChecker over the words found in the given file.
     * @throws java.io.IOException if filename is invalid
     */
    public static SpellChecker fromFile(String filename) throws IOException {
        return new SpellChecker(Files.readAllLines(Paths.get(filename)));
    }

    /** A Node of the SpellChecker structure. */
    private class Node {
        
        Node[] nodes;
        boolean stop;
        
        public Node() {
            nodes = new Node[NUM_LETTERS];
            stop = false;
        }
    }

    /** The root of the SpellChecker */
    private Node root;

    /**
     * Creates a new spellchecker with the given list of words
     * 
     * @param dict all the words that are "correct" within this spellchecker
     */
    public SpellChecker(List<String> dict) {
        root = new Node();
        for (int i = 0; i < dict.size(); i++) {
            add(dict.get(i));
        }
    }
    
    /**
     * Adds a new word to the spellchecker
     * 
     * @param word a new "correct" word
     */
    public void add(String word) {
        Node cur = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (cur.nodes[ch - 'a'] == null) {
                cur.nodes[ch - 'a'] = new Node();
            }
            cur = cur.nodes[ch - 'a'];
        }
        cur.stop = true;
    }
    
    /**
     * Checks to see if the given word is found in the spellchecker
     * 
     * @param word the word being checked for "correctness"
     * @return whether or not the spellchecker contains the word
     */
    public boolean isWord(String word) {
        Node cur = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (cur.nodes[ch - 'a'] == null) {
                return false;
            }
            cur = cur.nodes[ch - 'a'];
        }
        return cur != null && cur.stop;
    }
    
    /**
     * Finds all the possible one letter completions for the given word
     * 
     * @param word a incomplete word
     * @return the word's completions
     */
    public List<String> getOneCharCompletions(String word) {
        Node cur = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (cur.nodes[ch - 'a'] == null) {
                return null;
            }
            cur = cur.nodes[ch - 'a'];
        }
        List<String> list = new ArrayList<>();
        String str;
        for (int i = 0; i < cur.nodes.length; i++) {
            if (cur.nodes[i] != null) {
                if (cur.nodes[i].stop) {
                    str = word + (char) (i + 'a');
                    list.add(str);
                }
            }
        }
        return list;
    }

    /**
     * Corrects the last letter of the word
     * 
     * @param word a mispelled word
     * @return a list of possible correct spelling of the word
     */
    public List<String> getOneCharEndCorrections(String word) {
        String str = word.substring(0, word.length() - 1);
        return getOneCharCompletions(str);
    }
    
    /**
     * Corrects the mispelled word
     * 
     * @param word a mispelled word
     * @return a list of possible correct spelling of the word
     */
    public List<String> getOneCharCorrections(String word) {
        Node cur = root;
        List<String> list = getOneCharEndCorrections(word);
        String str;
        for (int i = 0; i < word.length() - 1; i++) {
            char ch = word.charAt(i);
            if (cur.nodes[ch - 'a'] == null || cur.nodes[ch - 'a'].nodes[word.charAt(i + 1) - 'a'] == null) {
                for (int j = 0; j < cur.nodes.length; j++) {
                    if (i == word.length() - 1) {
                        return getOneCharEndCorrections(word);
                    } else {
                        str = word.substring(0, i) + (char) (j + 'a') + word.substring(i + 1);
                        if (isWord(str)) {
                            list.add(str);
                        }
                    }
                }
                return list;
            }
            cur = cur.nodes[ch - 'a'];
        }
        return list;
    }

    /**
     * Determines what function will be applied to a word
     * 
     * @param args the inputs for the system
     * @throws IOException if filename is invalid
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java SpellChecker <command> <word>");
            System.exit(1);
        } else {
            String command = args[0];
            String word = args[1];
            SpellChecker checker = SpellChecker.fromFile(DICT_PATH);
            switch (command) {
                case "check": {
                    System.out.println(checker.isWord(word) ? "correct" : "incorrect");
                    System.exit(0);
                }

                case "complete": {
                    List<String> completions = checker.getOneCharCompletions(word);
                    for (String completion : completions) {
                        System.out.println(completion);
                    }
                    System.exit(0);
                }

                case "correct": {
                    List<String> corrections = checker.getOneCharEndCorrections(word);
                    for (String correction : corrections) {
                        System.out.println(correction);
                    }
                    System.exit(0);
                }

                default: {
                    System.err.println("Unknown command: " + command);
                    System.exit(1);
                }
            }
        }
    }
}
